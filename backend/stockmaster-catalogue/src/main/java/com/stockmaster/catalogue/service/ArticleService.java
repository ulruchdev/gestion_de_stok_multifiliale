package com.stockmaster.catalogue.service;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.catalogue.domain.entity.Categorie;
import com.stockmaster.catalogue.dto.request.ArticleCreateRequest;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * Opérations articles group-scopées (DEC-002, V5 §6).
 *
 * <ul>
 *   <li>US-031 — POST /api/v1/articles : créer un article</li>
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
