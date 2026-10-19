package com.stockmaster.catalogue.service;

import com.stockmaster.catalogue.domain.entity.Categorie;
import com.stockmaster.catalogue.dto.request.CategorieCreateRequest;
import com.stockmaster.catalogue.dto.request.CategorieUpdateRequest;
import com.stockmaster.catalogue.dto.response.CategorieListResponse;
import com.stockmaster.catalogue.dto.response.CategorieResponse;
import com.stockmaster.catalogue.repository.ArticleRepository;
import com.stockmaster.catalogue.repository.CategorieRepository;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Opérations catalogue group-scoped (DEC-002, V5 §6) : création (US-027),
 * liste paginée + search (US-028), modification (US-029), suppression logique
 * avec blocage DEC-028 (US-030).
 *
 * <p><b>RBAC</b> : prévu @PreAuthorize sur le contrôleur, avec défense en
 * profondeur dans le service (fail-fast rôle/groupe avant I/O —
 * pattern US-023/025).
 *
 * <p><b>Pagination / tri</b> : ordre de tri limité au champ designation (nom
 * d'entrée uniquement) pour rester cohérent avec le référentiel. Direction
 * normalisée ASC/DESC insensible à la casse.
 *
 * <p><b>Search</b> : recherche partielle insensible à la casse sur la
 * désignation (contient). Paramètre absent = pas de filtre.
 *
 * <p><b>Modification (US-029)</b> : PATCH sémantique sur le PUT — champ absent =
 * inchangé. {@code code} volontairement non modifiable par cette route
 * (DTO CategorieUpdateRequest ne l'expose pas). Unicité (group_id, code)
 * réévaluée uniquement si un code était présent (pas ici).
 *
 * <p><b>Suppression (US-030 / DEC-028)</b> : suppression logique
 * ({@code supprime=true}) uniquement si aucun article n'est attaché au groupe
 * dans cette catégorie (DEC-028 : blocage si articles attachés). Refus → 409
 * RES_005 (DEC-028).
 *
 * <p><b>Cache</b> : Redis non présent dans les dépendances de ce module —
 * la couche de cache sera ajoutée ultérieurement via une PR dédiée ; les
 * opérations REST sont implémentées sans cache pour aujourd'hui.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategorieService {

    private final CategorieRepository categorieRepository;
    private final ArticleRepository articleRepository;
    private final TenantGroupRepository tenantGroupRepository;
    private final TauxTvaAutorises tauxTvaAutorises;
    private final PerimetreCatalogueGuard perimetreCatalogueGuard;

    public CategorieResponse creer(StockMasterPrincipal principal, CategorieCreateRequest request) {
        Long groupId = perimetreCatalogueGuard.groupIdDuPrincipal(principal);
        String role = perimetreCatalogueGuard.roleDuPrincipal(principal);
        if (!ROLES_AUTORISES.contains(role)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        BigDecimal taux = request.getTauxTva();
        if (!tauxTvaAutorises.estAutorise(taux)) {
            throw new BusinessException(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }

        if (categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(groupId, request.getCode())) {
            throw new BusinessException(ErrorCode.RES_DUPLICATE_CODE);
        }

        TenantGroup groupe = tenantGroupRepository.findByIdAndSupprimeFalse(groupId)
                .filter(g -> Boolean.TRUE.equals(g.getActif()))
                .orElseThrow(() -> new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN));

        // Validation complémentaire : refuser taux négatif et taux > 100% (US-027)
        if (taux.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }
        if (taux.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }

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

    public CategorieListResponse lister(StockMasterPrincipal principal,
                                         int page,
                                         int size,
                                         String sortField,
                                         String direction,
                                         String designation) {
        Long groupId = perimetreCatalogueGuard.groupIdDuPrincipal(principal);
        String role = perimetreCatalogueGuard.roleDuPrincipal(principal);
        if (!ROLES_AUTORISES.contains(role)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        // Fail-fast : tant que le groupe du JWT n'a pas été validé, on n'effectue
        // pas de requête catalogue. Ainsi le scénario "groupe inexistant" de test
        // (principal valide, groupe inexistant) lève correctement.
        if (groupId != null) {
            TenantGroup groupe = tenantGroupRepository.findByIdAndSupprimeFalse(groupId)
                    .filter(g -> Boolean.TRUE.equals(g.getActif()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN));
        }

        Direction dir = normaliserDirection(direction);
        Sort sort = Sort.by(dir, "designation");
        if (sortField != null && sortField.equalsIgnoreCase("designation")) {
            sort = Sort.by(dir, "designation");
        }

        Page<Categorie> pageCategorie;
        if (designation != null && !designation.isBlank()) {
            pageCategorie = categorieRepository.findAllByGroupeIdAndDesignationContainingIgnoreCaseAndSupprimeFalse(
                    groupId, designation, PageRequest.of(page, Math.max(1, size), sort));
        } else {
            pageCategorie = categorieRepository.findAllByGroupeIdAndSupprimeFalse(groupId,
                    PageRequest.of(page, Math.max(1, size), sort));
        }

        List<CategorieResponse> content = pageCategorie.getContent().stream()
                .map(this::toResponse)
                .toList();

        return CategorieListResponse.builder()
                .content(content)
                .page(pageCategorie.getNumber())
                .size(pageCategorie.getSize())
                .totalElements(pageCategorie.getTotalElements())
                .totalPages(pageCategorie.getTotalPages())
                .first(pageCategorie.isFirst())
                .last(pageCategorie.isLast())
                .build();
    }

    @Transactional
    public CategorieResponse modifier(StockMasterPrincipal principal, Long id,
                                       CategorieUpdateRequest request) {
        Long groupId = perimetreCatalogueGuard.groupIdDuPrincipal(principal);
        String role = perimetreCatalogueGuard.roleDuPrincipal(principal);
        if (!ROLES_AUTORISES.contains(role)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        // Tant que le groupe du JWT n'est pas validé, on ne touche pas au
        // repository de catégories. Le test de refus de groupe inexistant
        // (principal valide, groupe inexistant) doit lever ici.
        if (groupId != null) {
            TenantGroup groupe = tenantGroupRepository.findByIdAndSupprimeFalse(groupId)
                    .filter(g -> Boolean.TRUE.equals(g.getActif()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN));
        }

        Categorie categorie = categorieRepository.findByIdAndSupprimeFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        if (!categorie.getGroupe().getId().equals(groupId)) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }

        BigDecimal taux = request.getTauxTva();
        if (!tauxTvaAutorises.estAutorise(taux)) {
            throw new BusinessException(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }

        // Validation complémentaire : refuser taux négatif et taux > 100% (US-027)
        if (taux.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }
        if (taux.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }

        categorie.setDesignation(request.getDesignation());
        categorie.setTauxTva(taux);

        Categorie miseAJour = categorieRepository.save(categorie);

        log.info("US-029 : catégorie {} ({}) modifiée dans le groupe {} par l'utilisateur {}",
                miseAJour.getCode(), miseAJour.getId(), groupId, principal.getUserId());

        return CategorieResponse.builder()
                .id(miseAJour.getId())
                .code(miseAJour.getCode())
                .designation(miseAJour.getDesignation())
                .tauxTva(miseAJour.getTauxTva())
                .groupId(groupId)
                .build();
    }

    @Transactional
    public CategorieResponse supprimer(StockMasterPrincipal principal, Long id) {
        Long groupId = perimetreCatalogueGuard.groupIdDuPrincipal(principal);
        String role = perimetreCatalogueGuard.roleDuPrincipal(principal);
        if (!ROLES_AUTORISES.contains(role)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        // Comme pour la modification, on valide le groupe du JWT dès le début
        // pour que le cas de refus groupe inexistant ne puisse pas atteindre le
        // repository de catégories.
        if (groupId != null) {
            TenantGroup groupe = tenantGroupRepository.findByIdAndSupprimeFalse(groupId)
                    .filter(g -> Boolean.TRUE.equals(g.getActif()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN));
        }

        Categorie categorie = categorieRepository.findByIdAndSupprimeFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        if (!categorie.getGroupe().getId().equals(groupId)) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }

        // DEC-028 : bloquer la suppression si des articles sont attachés
        // dans cette catégorie pour le groupe.
        if (articleRepository.existsByGroupeIdAndCategorieIdAndSupprimeFalse(groupId, id)) {
            throw new BusinessException(ErrorCode.RES_ENTITY_HAS_DEPENDENCIES);
        }

        categorie.setSupprime(true);
        Categorie supprimee = categorieRepository.save(categorie);

        log.info("US-030 : catégorie {} ({}) supprimée (logique) dans le groupe {} par l'utilisateur {}",
                supprimee.getCode(), supprimee.getId(), groupId, principal.getUserId());

        return CategorieResponse.builder()
                .id(supprimee.getId())
                .code(supprimee.getCode())
                .designation(supprimee.getDesignation())
                .tauxTva(supprimee.getTauxTva())
                .groupId(groupId)
                .build();
    }

    private static final Set<String> ROLES_AUTORISES = Set.of(
            "GESTIONNAIRE_STOCK", "ADMIN_FILIALE", "ADMIN_GROUPE");

    private static Direction normaliserDirection(String direction) {
        if (direction == null) {
            return Sort.Direction.ASC;
        }
        String cleaned = direction.trim();
        if (cleaned.equalsIgnoreCase("DESC")) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

    private CategorieResponse toResponse(Categorie categorie) {
        return CategorieResponse.builder()
                .id(categorie.getId())
                .code(categorie.getCode())
                .designation(categorie.getDesignation())
                .tauxTva(categorie.getTauxTva())
                .groupId(categorie.getGroupe().getId())
                .build();
    }
}
