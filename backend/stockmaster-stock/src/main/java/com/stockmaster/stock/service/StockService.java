package com.stockmaster.stock.service;

import com.stockmaster.shared.config.PaginationProperties;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.stock.domain.enums.OrigineType;
import com.stockmaster.stock.domain.enums.StatutAlerte;
import com.stockmaster.stock.domain.enums.TypeMouvement;
import com.stockmaster.stock.dto.response.MouvementStockResponse;
import com.stockmaster.stock.dto.response.StockArticleResponse;
import com.stockmaster.stock.repository.MouvementStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

/**
 * Consultation du stock — US-051 (stock réel par article) et US-052
 * (historique des mouvements).
 *
 * <p><b>Isolation multi-tenant (règle n°1)</b> : {@code groupId} et
 * {@code entrepriseId} proviennent du JWT — jamais d'un paramètre de requête
 * ni du corps. La liste des articles est scoping au groupe ; l'agrégation du
 * stock est bornée à l'entreprise.</p>
 *
 * <p><b>Statut d'alerte (REF §4.2)</b> : la dérivation existe en deux endroits
 * alignés — {@link StatutAlerte#de} (DTO) et le CASE SQL de
 * {@code listerStockParArticle} (filtre paginé). Le filtre est appliqué en SQL
 * sinon la pagination serait faussée.</p>
 *
 * <p><b>Décision GO utilisateur (US-051)</b> : un article sans aucun mouvement
 * (stock 0) remonte {@code RUPTURE} — lecture littérale du REF §4.2.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final MouvementStockRepository mouvementStockRepository;
    private final TenantGroupRepository tenantGroupRepository;
    private final PaginationProperties paginationProperties;

    /** US-052 — RBAC (défense en profondeur après le @PreAuthorize). */
    private static final Set<String> ROLES_MOUVEMENTS = Set.of(
            "GESTIONNAIRE_STOCK", "ADMIN_FILIALE", "ADMIN_GROUPE");

    /** Sentinelles non nulles : aucun paramètre NULL côté JDBC (US-052). */
    private static final String AUCUN_FILTRE = "";
    private static final Instant DEBUT_MIN = Instant.EPOCH;
    private static final Instant FIN_MAX = Instant.parse("2100-01-01T00:00:00Z");

    // ─── US-051 — GET /api/v1/stock ─────────────────────────────────────────

    /**
     * Liste paginée du stock réel de chaque article du groupe, agrégé sur
     * l'entreprise du JWT (REF §4.2 — somme signée, jamais dénormalisée).
     *
     * <p>Aucune restriction de rôle : « tout utilisateur authentifié »
     * (critère US-051).</p>
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal
     *         ne porte pas de {@code groupId} ou si le groupe est absent/inactif ;
     *         {@code SEC_ACCESS_DENIED} si le principal ne porte pas d'{@code entrepriseId} ;
     *         {@code SYS_BAD_REQUEST} si {@code statutAlerte} n'est pas un statut valide.
     */
    @Transactional(readOnly = true)
    public PageResponse<StockArticleResponse> listerStock(StockMasterPrincipal principal,
                                                          String statutAlerte,
                                                          int page, int size) {
        Long groupId = groupIdDuPrincipal(principal);
        Long entrepriseId = entrepriseIdDuPrincipal(principal);
        verifierGroupeActif(groupId);
        String statut = normaliserStatut(statutAlerte);

        Page<Object[]> lignes = mouvementStockRepository.listerStockParArticle(
                groupId, entrepriseId, statut, pageable(page, size));

        List<StockArticleResponse> contenu = lignes.getContent().stream()
                .map(this::versStockArticle)
                .toList();

        log.debug("US-051 : {} article(s) du groupe {} pour l'entreprise {} (statut='{}')",
                lignes.getTotalElements(), groupId, entrepriseId, statut);

        return PageResponse.<StockArticleResponse>builder()
                .content(contenu)
                .page(lignes.getNumber())
                .size(lignes.getSize())
                .totalElements(lignes.getTotalElements())
                .totalPages(lignes.getTotalPages())
                .build();
    }

    // ─── US-052 — GET /api/v1/stock/articles/{id}/mouvements ───────────────

    /**
     * Historique paginé des mouvements d'un article dans l'entreprise du JWT,
     * tri date DESC (critère US-052), filtrable par type et plage de dates.
     *
     * @throws BusinessException mêmes codes que {@link #listerStock} pour le
     *         périmètre, plus {@code SEC_ACCESS_DENIED} si le rôle n'est pas du
     *         trio stock, {@code SYS_BAD_REQUEST} pour un type ou une plage de
     *         dates invalide, {@code RES_ENTITY_NOT_FOUND} (404 uniforme) si
     *         l'article n'existe pas ou n'appartient pas au groupe du JWT.
     */
    @Transactional(readOnly = true)
    public PageResponse<MouvementStockResponse> listerMouvements(StockMasterPrincipal principal,
                                                                 Long articleId,
                                                                 String typeMouvement,
                                                                 LocalDate dateDebut,
                                                                 LocalDate dateFin,
                                                                 int page, int size) {
        Long groupId = groupIdDuPrincipal(principal);
        Long entrepriseId = entrepriseIdDuPrincipal(principal);
        verifierRoleMouvements(principal);
        verifierGroupeActif(groupId);
        String type = normaliserTypeMouvement(typeMouvement);

        if (dateDebut != null && dateFin != null && dateDebut.isAfter(dateFin)) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST);
        }
        Instant debut = dateDebut != null
                ? dateDebut.atStartOfDay(ZoneOffset.UTC).toInstant() : DEBUT_MIN;
        // dateFin inclusive : borne haute = lendemain 00:00Z (intervalle [debut, fin[)
        Instant fin = dateFin != null
                ? dateFin.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : FIN_MAX;

        if (!mouvementStockRepository.articleExisteDansGroupe(articleId, groupId)) {
            // 404 uniforme : jamais révéler l'existence d'un article d'un autre groupe
            throw new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        Page<Object[]> lignes = mouvementStockRepository.listerMouvementsParArticle(
                entrepriseId, articleId, type, debut, fin, pageable(page, size));

        List<MouvementStockResponse> contenu = lignes.getContent().stream()
                .map(this::versMouvement)
                .toList();

        log.debug("US-052 : {} mouvement(s) de l'article {} (entreprise {}, type='{}')",
                lignes.getTotalElements(), articleId, entrepriseId, type);

        return PageResponse.<MouvementStockResponse>builder()
                .content(contenu)
                .page(lignes.getNumber())
                .size(lignes.getSize())
                .totalElements(lignes.getTotalElements())
                .totalPages(lignes.getTotalPages())
                .build();
    }

    // ─── garde-fous (fail-fast avant toute I/O) ─────────────────────────────

    private Long groupIdDuPrincipal(StockMasterPrincipal principal) {
        Long groupId = principal != null ? principal.getGroupId() : null;
        if (groupId == null) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
        return groupId;
    }

    private Long entrepriseIdDuPrincipal(StockMasterPrincipal principal) {
        Long entrepriseId = principal != null ? principal.getEntrepriseId() : null;
        if (entrepriseId == null) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
        return entrepriseId;
    }

    private void verifierRoleMouvements(StockMasterPrincipal principal) {
        String role = principal != null ? principal.getRole() : null;
        if (role == null || role.isBlank() || !ROLES_MOUVEMENTS.contains(role)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
    }

    private void verifierGroupeActif(Long groupId) {
        tenantGroupRepository.findByIdAndSupprimeFalse(groupId)
                .filter(groupe -> Boolean.TRUE.equals(groupe.getActif()))
                .orElseThrow(() -> new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN));
    }

    // ─── normalisation des filtres ──────────────────────────────────────────

    /** {@code null}/vide = pas de filtre ; sinon nom d'énumeration valide ou 400. */
    private String normaliserStatut(String statutAlerte) {
        if (statutAlerte == null || statutAlerte.isBlank()) {
            return AUCUN_FILTRE;
        }
        try {
            return StatutAlerte.valueOf(statutAlerte.trim().toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST);
        }
    }

    private String normaliserTypeMouvement(String typeMouvement) {
        if (typeMouvement == null || typeMouvement.isBlank()) {
            return AUCUN_FILTRE;
        }
        try {
            return TypeMouvement.valueOf(typeMouvement.trim().toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SYS_BAD_REQUEST);
        }
    }

    /** Page 0-based non négative, taille bornée à {@code stockmaster.pagination.max-page-size}. */
    private Pageable pageable(int page, int size) {
        int taille = Math.min(Math.max(1, size), paginationProperties.getMaxPageSize());
        return PageRequest.of(Math.max(0, page), taille);
    }

    // ─── mapping des lignes du repository ───────────────────────────────────

    /** [articleId, codeArticle, designation, seuilAlerte, stockActuel] */
    private StockArticleResponse versStockArticle(Object[] ligne) {
        BigDecimal stock = (BigDecimal) ligne[4];
        BigDecimal seuil = (BigDecimal) ligne[3];
        return StockArticleResponse.builder()
                .articleId((Long) ligne[0])
                .codeArticle((String) ligne[1])
                .designation((String) ligne[2])
                .seuilAlerte(seuil)
                .stockActuel(stock)
                .statutAlerte(StatutAlerte.de(stock, seuil))
                .build();
    }

    /** [id, type, quantite, date, utilisateurId, prenom, nom, origine, origineId, motif] */
    private MouvementStockResponse versMouvement(Object[] ligne) {
        return MouvementStockResponse.builder()
                .id((Long) ligne[0])
                .typeMouvement(TypeMouvement.valueOf((String) ligne[1]))
                .quantite((BigDecimal) ligne[2])
                .dateMouvement(versInstant(ligne[3]))
                .utilisateurId((Long) ligne[4])
                .utilisateurNom(versNom((String) ligne[5], (String) ligne[6]))
                .origineType(ligne[7] != null ? OrigineType.valueOf((String) ligne[7]) : null)
                .origineId((Long) ligne[8])
                .motif((String) ligne[9])
                .build();
    }

    /** La requête native renvoie un {@code Timestamp} (TIMESTAMPTZ) ou un {@code Instant}. */
    private Instant versInstant(Object valeur) {
        if (valeur == null) {
            return null;
        }
        if (valeur instanceof Instant instant) {
            return instant;
        }
        if (valeur instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        throw new IllegalStateException("Type de date inattendu : " + valeur.getClass());
    }

    private String versNom(String prenom, String nom) {
        if (prenom == null && nom == null) {
            return null;
        }
        return String.join(" ",
                List.of(prenom == null ? "" : prenom.trim(),
                        nom == null ? "" : nom.trim())
                        .stream().filter(morceau -> !morceau.isEmpty()).toList());
    }
}
