package com.stockmaster.catalogue.service;

import com.stockmaster.catalogue.domain.entity.Categorie;
import com.stockmaster.catalogue.dto.request.CategorieCreateRequest;
import com.stockmaster.catalogue.dto.response.CategorieResponse;
import com.stockmaster.catalogue.repository.CategorieRepository;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

/**
 * US-027 — créer une catégorie ({@code POST /api/v1/categories}).
 *
 * <p><b>Périmètre</b> : le catalogue est <b>group-scoped</b> ({@code DEC-002},
 * V5 §6) — l'ancre tenant est le {@code groupId} du JWT, jamais un champ du
 * corps (critère US-027 « isolation tenant » ; le backlog parlait
 * d'{@code entreprise_id}, la V5 a remplacé cette colonne par {@code group_id}).</p>
 *
 * <p><b>Unicité</b> : {@code (group_id, code)} → 409 {@code RES_002}
 * (contrainte DB V5 §6, pré-vérifiée — course éventuelle → violation
 * contrainte, cas limite assumé comme US-021/022).</p>
 *
 * <p><b>TVA</b> (critère US-027) : taux restreint aux valeurs supportées →
 * 400 {@code CAT_001} (colonne V1 : {@code CHECK 0..100} — le service durcit
 * la contrainte base par la liste configurable).</p>
 *
 * <p><b>RBAC</b> : {@code @PreAuthorize} GESTIONNAIRE_STOCK / ADMIN_FILIALE /
 * ADMIN_GROUPE + défense en profondeur dans le service (fail-fast avant
 * toute I/O — pattern US-023/025).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategorieService {

    private final CategorieRepository categorieRepository;
    private final TenantGroupRepository tenantGroupRepository;
    private final TauxTvaAutorises tauxTvaAutorises;
    private final PerimetreCatalogueGuard perimetreCatalogueGuard;

    /**
     * Crée une catégorie dans le groupe du principal connecté.
     *
     * @throws BusinessException {@code SEC_001} si le rôle du principal n'est
     *         pas autorisé ; {@code GRP_002} si le principal ne porte pas de
     *         groupId ou si le groupe hôte est inexistant / soft-supprimé /
     *         désactivé ; {@code RES_002} si le code existe déjà dans le
     *         groupe ; {@code CAT_001} si le taux de TVA n'est pas supporté.
     */
    @Transactional
    public CategorieResponse creer(StockMasterPrincipal principal, CategorieCreateRequest request) {
        // Garde-fous fail-fast — avant toute I/O (pattern US-023/025) :
        // rôle, puis validation d'entrée pure (TVA), puis I/O.
        Long groupId = perimetreCatalogueGuard.groupIdDuPrincipal(principal);
        String role = perimetreCatalogueGuard.roleDuPrincipal(principal);
        if (!ROLES_AUTORISES.contains(role)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        // Taux de TVA parmi les valeurs supportées (critère US-027) —
        // validation sans I/O, donc avant unicité et chargement du groupe
        BigDecimal taux = request.getTauxTva();
        if (!tauxTvaAutorises.estAutorise(taux)) {
            throw new BusinessException(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }

        // Unicité (group_id, code) — court-circuite avant le chargement du groupe
        if (categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(groupId, request.getCode())) {
            throw new BusinessException(ErrorCode.RES_DUPLICATE_CODE);
        }

        // Groupe hôte : existant, non soft-supprimé, actif (suspension/plan expiré)
        TenantGroup groupe = tenantGroupRepository.findByIdAndSupprimeFalse(groupId)
                .filter(g -> Boolean.TRUE.equals(g.getActif()))
                .orElseThrow(() -> new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN));

        Categorie categorie = Categorie.builder()
                .groupe(groupe)
                .code(request.getCode())
                .designation(request.getDesignation())
                .tauxTva(taux)
                .build();
        Categorie creee = categorieRepository.save(categorie);

        log.info("US-027 : catégorie {} ({}) créée dans le groupe {} par l'utilisateur {}",
                creee.getCode(), creee.getId(), groupId, principal.getUserId());

        return CategorieResponse.builder()
                .id(creee.getId())
                .code(creee.getCode())
                .designation(creee.getDesignation())
                .tauxTva(creee.getTauxTva())
                .groupId(groupId)
                .build();
    }

    private static final Set<String> ROLES_AUTORISES = Set.of(
            "GESTIONNAIRE_STOCK", "ADMIN_FILIALE", "ADMIN_GROUPE");
}
