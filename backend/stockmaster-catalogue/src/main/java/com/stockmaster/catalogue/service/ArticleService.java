package com.stockmaster.catalogue.service;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.catalogue.domain.entity.Categorie;
import com.stockmaster.catalogue.dto.request.ArticleCreateRequest;
import com.stockmaster.catalogue.dto.request.ArticleUpdateRequest;
import com.stockmaster.catalogue.dto.response.ArticleListResponse;
import com.stockmaster.catalogue.dto.response.ArticleResponse;
import com.stockmaster.catalogue.repository.ArticleRepository;
import com.stockmaster.catalogue.repository.CategorieRepository;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.shared.service.CalculTvaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * Opérations articles group-scopées (DEC-002, V5 §6).
 *
 * <ul>
 *   <li>US-031 — POST   /api/v1/articles : créer un article</li>
 *   <li>US-032 — GET    /api/v1/articles : liste paginée + search + filtres</li>
 *   <li>US-033 — GET    /api/v1/articles/{id} : fiche article (404 hors groupe)</li>
 *   <li>US-034 — PUT    /api/v1/articles/{id} : modifier (TTC recalculé, DEC-003)</li>
 *   <li>US-035 — DELETE /api/v1/articles/{id} : suppression logique</li>
 * </ul>
 *
 * <p><b>RBAC</b> : défense en profondeur fail-fast (rôles GESTIONNAIRE_STOCK /
 * ADMIN_FILIALE / ADMIN_GROUPE), en miroir exact de {@code CategorieService}.
 * L'unicité (group_id, code_article) est miroir de la contrainte DB (V5).</p>
 *
 * <p><b>DEC-003</b> : {@code prixVenteTtc} est calculé côté serveur via
 * {@code CalculTvaService.calculerTtc} — jamais saisi par le client. Le taux
 * TVA hérite de la catégorie s'il n'est pas fourni ; s'il est fourni, il doit
 * être supporté ({@code TauxTvaAutorises}, bornes [0;100] → 400 CAT_001).</p>
 *
 * <p><b>DEC-029</b> : « prix d'achat ≥ prix de vente » n'est pas bloquant —
 * un warning est journalisé, la marge affichée est alors négative ou nulle.</p>
 *
 * <p>Les champs conditionnement (DEC-013 : unités, facteur) et lot
 * (DEC-012) ne sont pas exposés par cet endpoint : ils relèvent de US-103 /
 * US-104 ; les défauts {@code @PrePersist} de l'entité s'appliquent.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategorieRepository categorieRepository;
    private final TenantGroupRepository tenantGroupRepository;
    private final TauxTvaAutorises tauxTvaAutorises;
    private final PerimetreCatalogueGuard perimetreCatalogueGuard;
    private final CalculTvaService calculTvaService;

    private static final Set<String> ROLES_AUTORISES = Set.of(
            "GESTIONNAIRE_STOCK", "ADMIN_FILIALE", "ADMIN_GROUPE");

    @Transactional(rollbackFor = Exception.class)
    public ArticleResponse creer(StockMasterPrincipal principal, ArticleCreateRequest request) {
        // Fail-fast : garde-fous tenant/rôle avant toute I/O (pattern US-027).
        Long groupId = perimetreCatalogueGuard.groupIdDuPrincipal(principal);
        String role = perimetreCatalogueGuard.roleDuPrincipal(principal);
        if (!ROLES_AUTORISES.contains(role)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        // Unicité (group_id, code_article) — miroir contrainte DB, short-circuit.
        if (articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(
                groupId, request.getCodeArticle())) {
            throw new BusinessException(ErrorCode.RES_DUPLICATE_CODE);
        }

        // Catégorie : doit exister et appartenir au groupe du JWT (404 uniforme,
        // jamais révéler l'existence d'une catégorie hors périmètre).
        Categorie categorie = categorieRepository
                .findByIdAndSupprimeFalse(request.getCategorieId())
                .filter(c -> c.getGroupe() != null && groupId.equals(c.getGroupe().getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        // Taux TVA : hérité de la catégorie si non fourni, sinon bornes + support.
        BigDecimal taux = request.getTauxTva() != null
                ? request.getTauxTva()
                : categorie.getTauxTva();
        if (taux.signum() < 0
                || taux.compareTo(BigDecimal.valueOf(100)) > 0
                || !tauxTvaAutorises.estAutorise(taux)) {
            throw new BusinessException(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }

        TenantGroup groupe = tenantGroupRepository.findByIdAndSupprimeFalse(groupId)
                .filter(g -> Boolean.TRUE.equals(g.getActif()))
                .orElseThrow(() -> new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN));

        // DEC-003 : TTC toujours calculé côté serveur.
        Integer prixVenteTtc = calculTvaService.calculerTtc(request.getPrixVenteHt(), taux);

        // DEC-029 : avertissement non bloquant si achat ≥ vente.
        if (request.getPrixAchatHt() >= request.getPrixVenteHt()) {
            log.warn("US-031 / DEC-029 : prix d'achat ({}) >= prix de vente ({}) pour l'article {} — avertissement non bloquant",
                    request.getPrixAchatHt(), request.getPrixVenteHt(), request.getCodeArticle());
        }

        // DEC-023 : seuil 0 = alerte désactivée.
        BigDecimal seuilAlerte = request.getSeuilAlerte() != null
                ? request.getSeuilAlerte()
                : BigDecimal.ZERO;

        Article article = Article.builder()
                .groupe(groupe)
                .categorie(categorie)
                .codeArticle(request.getCodeArticle())
                .designation(request.getDesignation())
                .prixAchatHt(request.getPrixAchatHt())
                .prixVenteHt(request.getPrixVenteHt())
                .tauxTva(taux)
                .prixVenteTtc(prixVenteTtc)
                .seuilAlerte(seuilAlerte)
                .build();
        Article cree = articleRepository.save(article);

        log.info("US-031 : article {} ({}) créé dans le groupe {} par l'utilisateur {}",
                cree.getCodeArticle(), cree.getId(), groupId, principal.getUserId());

        return ArticleResponse.builder()
                .id(cree.getId())
                .codeArticle(cree.getCodeArticle())
                .designation(cree.getDesignation())
                .categorieId(categorie.getId())
                .prixAchatHt(cree.getPrixAchatHt())
                .prixVenteHt(cree.getPrixVenteHt())
                .tauxTva(cree.getTauxTva())
                .prixVenteTtc(cree.getPrixVenteTtc())
                .margeBrutePct(margeBrutePct(cree.getPrixAchatHt(), cree.getPrixVenteHt()))
                .seuilAlerte(cree.getSeuilAlerte())
                .actif(cree.getActif())
                .groupId(groupId)
                .build();
    }

    // ===== US-032 — lire (paginé + search) ==================================

    /**
     * US-032 — « tout utilisateur authentifié » : aucun filtre de rôle métier,
     * seulement le principal groupé (fail-fast via {@code PerimetreCatalogueGuard}).
     * Transaction en lecture seule ; le scoping est (group_id, supprime=false).
     */
    @Transactional(readOnly = true)
    public ArticleListResponse lister(
            StockMasterPrincipal principal,
            int page, int size, String sortField, String direction, String search) {
        Long groupId = perimetreCatalogueGuard.groupIdDuPrincipal(principal);

        Direction dir = normaliserDirection(direction);
        Sort sort = Sort.by(dir, "designation");
        if (sortField != null && sortField.equalsIgnoreCase("codeArticle")) {
            sort = Sort.by(dir, "codeArticle");
        }

        // Pagination obligatoire — bornes disciplinées : max 100 par page.
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), sort);

        Page<Article> pageArticle;
        if (search != null && !search.isBlank()) {
            pageArticle = articleRepository.rechercherParGroupeEtTexte(
                    groupId, search.trim(), pageable);
        } else {
            pageArticle = articleRepository.findAllByGroupeIdAndSupprimeFalse(groupId, pageable);
        }

        Page<ArticleResponse> mappee = pageArticle.map(this::toResponse);
        return ArticleListResponse.from(mappee);
    }

    // ===== US-033 — consulter ===============================================

    /**
     * US-033 — fiche article group-scopée ; 404 uniforme si l'article est
     * inconnu ou hors groupe (ne jamais révéler l'existence).
     */
    @Transactional(readOnly = true)
    public ArticleResponse consulter(StockMasterPrincipal principal, Long id) {
        Long groupId = perimetreCatalogueGuard.groupIdDuPrincipal(principal);

        Article article = articleRepository.findByIdAndSupprimeFalse(id)
                .filter(a -> a.getGroupe() != null && groupId.equals(a.getGroupe().getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        return toResponse(article);
    }

    // ===== US-034 — modifier ================================================

    /**
     * US-034 — mise à jour complète (PUT). TTC recalculé côté serveur
     * (DEC-003) si {@code prixVenteHt} ou {@code tauxTva} change ; le taux
     * hérite de la (nouvelle) catégorie s'il n'est pas fourni. Les documents
     * existants figent leur snapshot de prix — la modification n'impacte
     * jamais les lignes déjà enregistrées. Pas de cache article à évicter
     * en V1 (aucun cache Redis branché sur le catalogue ; l'éviction future
     * est documentée).
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponse modifier(StockMasterPrincipal principal, Long id, ArticleUpdateRequest request) {
        // Fail-fast — pattern US-027 identique (rôles d'écriture).
        Long groupId = perimetreCatalogueGuard.groupIdDuPrincipal(principal);
        String role = perimetreCatalogueGuard.roleDuPrincipal(principal);
        if (!ROLES_AUTORISES.contains(role)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        // Article cible : 404 uniforme (inconnu ou hors groupe).
        Article original = articleRepository.findByIdAndSupprimeFalse(id)
                .filter(a -> a.getGroupe() != null && groupId.equals(a.getGroupe().getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        // Unicité (group_id, code_article) : exists() inclut l'article cible
        // lui-même → doublon réel seulement si le code a changé.
        if (articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(
                groupId, request.getCodeArticle())
                && !request.getCodeArticle().equals(original.getCodeArticle())) {
            throw new BusinessException(ErrorCode.RES_DUPLICATE_CODE);
        }

        // Catégorie : 404 uniforme (inconnue ou hors groupe).
        Categorie categorie = categorieRepository
                .findByIdAndSupprimeFalse(request.getCategorieId())
                .filter(c -> c.getGroupe() != null && groupId.equals(c.getGroupe().getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        // Taux TVA : hérité de la (nouvelle) catégorie si non fourni, sinon
        // bornes + support (400 CAT_001).
        BigDecimal taux = request.getTauxTva() != null
                ? request.getTauxTva()
                : categorie.getTauxTva();
        if (taux.signum() < 0
                || taux.compareTo(BigDecimal.valueOf(100)) > 0
                || !tauxTvaAutorises.estAutorise(taux)) {
            throw new BusinessException(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }

        // DEC-003 : TTC toujours recalculé côté serveur.
        Integer prixVenteTtc = calculTvaService.calculerTtc(request.getPrixVenteHt(), taux);

        // DEC-029 : avertissement non bloquant si achat ≥ vente.
        if (request.getPrixAchatHt() >= request.getPrixVenteHt()) {
            log.warn("US-034 / DEC-029 : prix d'achat ({}) >= prix de vente ({}) pour l'article {} — avertissement non bloquant",
                    request.getPrixAchatHt(), request.getPrixVenteHt(), request.getCodeArticle());
        }

        // DEC-023 : seuil 0 = alerte désactivée.
        BigDecimal seuilAlerte = request.getSeuilAlerte() != null
                ? request.getSeuilAlerte()
                : BigDecimal.ZERO;

        original.setCodeArticle(request.getCodeArticle());
        original.setDesignation(request.getDesignation());
        original.setCategorie(categorie);
        original.setPrixAchatHt(request.getPrixAchatHt());
        original.setPrixVenteHt(request.getPrixVenteHt());
        original.setTauxTva(taux);
        original.setPrixVenteTtc(prixVenteTtc);
        original.setSeuilAlerte(seuilAlerte);
        Article modifie = articleRepository.save(original);

        log.info("US-034 : article {} ({}) modifié dans le groupe {} par l'utilisateur {}",
                modifie.getCodeArticle(), modifie.getId(), groupId, principal.getUserId());

        return toResponse(modifie);
    }

    // ===== US-035 — supprimer (archivage logique) ===========================

    /**
     * US-035 — suppression logique. Article inconnu ou hors groupe → 404
     * uniforme (ne jamais révéler l'existence). Un article référencé par une
     * commande ou une vente serait interdit (409 RES_005, « Archivez
     * l'article plutôt que de le supprimer ») — les modules achat/vente
     * n'existent pas encore : la garde devient opérationnelle avec EPIC 7/9 ;
     * le scoping groupe ci-dessous reste seul en vigueur en V1.
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponse supprimer(StockMasterPrincipal principal, Long id) {
        Long groupId = perimetreCatalogueGuard.groupIdDuPrincipal(principal);
        String role = perimetreCatalogueGuard.roleDuPrincipal(principal);
        if (!ROLES_AUTORISES.contains(role)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        Article article = articleRepository.findByIdAndSupprimeFalse(id)
                .filter(a -> a.getGroupe() != null && groupId.equals(a.getGroupe().getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        // Soft delete (AbstractEntity) — historique conservé, références sûres.
        article.setSupprime(true);
        article.setActif(false);
        Article supprime = articleRepository.save(article);

        log.info("US-035 : article {} ({}) supprimé (logique) dans le groupe {} par l'utilisateur {}",
                supprime.getCodeArticle(), supprime.getId(), groupId, principal.getUserId());

        return toResponse(supprime);
    }

    // ===== helpers partagés =================================================

    private ArticleResponse toResponse(Article a) {
        return ArticleResponse.builder()
                .id(a.getId())
                .codeArticle(a.getCodeArticle())
                .designation(a.getDesignation())
                .categorieId(a.getCategorie() != null ? a.getCategorie().getId() : null)
                .prixAchatHt(a.getPrixAchatHt())
                .prixVenteHt(a.getPrixVenteHt())
                .tauxTva(a.getTauxTva())
                .prixVenteTtc(a.getPrixVenteTtc())
                .margeBrutePct(margeBrutePct(a.getPrixAchatHt(), a.getPrixVenteHt()))
                .seuilAlerte(a.getSeuilAlerte())
                .actif(a.getActif())
                .groupId(a.getGroupe() != null ? a.getGroupe().getId() : null)
                // stockActuel / statutAlerte : null en V1 (EPIC 8 pas encore livré,
                // voir javadoc de ArticleResponse).
                .stockActuel(null)
                .statutAlerte(null)
                .build();
    }

    /** Miroir CategorieService : renvoie Direction.DESC si « DESC », sinon ASC. */
    private static Direction normaliserDirection(String direction) {
        if (direction != null && direction.trim().equalsIgnoreCase("DESC")) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

    /**
     * Marge brute en % — calculée à la volée, jamais persistée :
     * {@code (prixVenteHt − prixAchatHt) / prixAchatHt × 100}, HALF_UP à 2
     * décimales ; {@code null} si le prix d'achat est 0 (division par zéro).
     * Négative possible (DEC-029 non bloquant).
     */
    private static BigDecimal margeBrutePct(Integer prixAchatHt, Integer prixVenteHt) {
        if (prixAchatHt == null || prixAchatHt == 0 || prixVenteHt == null) {
            return null;
        }
        return BigDecimal.valueOf((long) prixVenteHt - prixAchatHt)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(prixAchatHt), 2, RoundingMode.HALF_UP);
    }
}
