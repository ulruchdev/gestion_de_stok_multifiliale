package com.stockmaster.utilisateur.service;

import com.stockmaster.shared.config.PaginationProperties;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.repository.UtilisateurRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.dto.response.UtilisateurListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

/**
 * US-023 — lister les utilisateurs du périmètre de l'admin connecté.
 *
 * <p><b>Périmètre Groupe↔Filiale</b> (critères d'acceptation US-023) :</p>
 * <ul>
 *   <li>Admin Groupe → tous les utilisateurs de toutes les filiales de SON
 *       groupe (filtre {@code group_id}) ; peut resserrer sur une filiale du
 *       groupe via {@code filialeId} ;</li>
 *   <li>Admin Filiale → uniquement les utilisateurs de SA filiale
 *       ({@code entreprise_id} du JWT) ; un {@code filialeId} explicite autre
 *       que le sien est refusé {@code 403 SEC_ACCESS_DENIED}.</li>
 * </ul>
 *
 * <p>Les 4 rôles métier (CAISSIER, COMMERCIAL, RESP_ACHATS, GESTIONNAIRE_STOCK)
 * sont refusés — la liste des comptes est une fonction d'administration
 * (le périmètre fonctionnel d'un employé est déjà borné par son scope) :</p>
 * <ul>
 *   <li>contrôle RBAC principal : {@code @PreAuthorize} sur le contrôleur ;</li>
 *   <li>garde service (défense en profondeur) : tout rôle hors
 *       {@code ADMIN_FILIALE}/{@code ADMIN_GROUPE} → {@code SEC_ACCESS_DENIED}.</li>
 * </ul>
 *
 * <p>Défense en profondeur supplémentaire : toute ligne échappée au filtre
 * SQL (incohérence éventuelle de données) est écartée et journalisée —
 * jamais servie hors périmètre.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurListService {

    /** Rôles autorisés à lister — fonctions d'administration uniquement. */
    private static final Set<RoleUtilisateur> ROLES_ADMIN_AUTORISES = Set.of(
            RoleUtilisateur.ADMIN_FILIALE,
            RoleUtilisateur.ADMIN_GROUPE
    );

    private final UtilisateurRepository utilisateurRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final TenantGroupRepository groupeRepository;
    private final PaginationProperties paginationProperties;
    private final PerimetreAdminGuard perimetreAdminGuard;

    /**
     * Liste paginée et filtrée des utilisateurs du périmètre.
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal
     *         ne porte pas de groupId (token corrompu / Super Admin) ;
     *         {@code SEC_ACCESS_DENIED} si le rôle n'est pas ADMIN_FILIALE/ADMIN_GROUPE,
     *         si un Admin Filiale demande une autre filiale, ou si le {@code filialeId}
     *         demandé est hors groupe (jamais 404 : on refuse le contenu sans
     *         confirmer l'existence) ;
     *         {@code RES_ENTITY_NOT_FOUND} si le groupe du JWT est introuvable
     *         ou soft-supprimé.
     */
    @Transactional(readOnly = true)
    public PageResponse<UtilisateurListResponse> lister(StockMasterPrincipal principal,
            RoleUtilisateur role, Boolean actif, Long filialeId, Pageable pageable) {

        // Garde-fous fail-fast — avant toute I/O
        Long groupId = perimetreAdminGuard.groupIdDuPrincipal(principal);
        RoleUtilisateur rolePrincipal = perimetreAdminGuard.roleDuPrincipal(principal);
        if (!ROLES_ADMIN_AUTORISES.contains(rolePrincipal)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        TenantGroup groupe = groupeRepository.findById(groupId)
                .filter(g -> !Boolean.TRUE.equals(g.getSupprime()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        // Résolution du périmètre
        Long entrepriseIdEffective = resoudrePerimetre(principal, groupId, filialeId);

        // Pagination bornée (convention US-017)
        Pageable pageableBorne = bornerTaillePage(pageable);

        Page<Utilisateur> page = (entrepriseIdEffective != null)
                ? utilisateurRepository.findByEntrepriseId(entrepriseIdEffective, role, actif, pageableBorne)
                : utilisateurRepository.findByEntrepriseGroupeId(groupId, null, role, actif, pageableBorne);

        // Défense en profondeur : une ligne hors groupe échappée au filtre est écartée, pas servie
        Page<UtilisateurListResponse> reponse = page.map(u -> {
            if (u.getEntreprise() == null || u.getEntreprise().getGroupe() == null
                    || !groupId.equals(u.getEntreprise().getGroupe().getId())) {
                log.warn("US-023 : ligne utilisateur id={} hors groupe {} écartée (défense en profondeur)",
                        u.getId(), groupId);
                return null;
            }
            return UtilisateurListResponse.de(u);
        });
        java.util.List<UtilisateurListResponse> contenu = reponse.getContent().stream()
                .filter(Objects::nonNull)
                .toList();

        return PageResponse.<UtilisateurListResponse>builder()
                .content(contenu)
                .page(reponse.getNumber())
                .size(reponse.getSize())
                .totalElements(reponse.getTotalElements())
                .totalPages(reponse.getTotalPages())
                .build();
    }

    // ─── helpers privés ─────────────────────────────────────────────────────

    /**
     * Résout le périmètre effectif de lecture :
     * <ul>
     *   <li>Admin Filiale : SA filiale (JWT) — tout {@code filialeId} explicite
     *       différent → 403 (pas 404 : refus de contenu, pas d'absence) ;</li>
     *   <li>Admin Groupe : {@code filialeId} s'il est fourni et appartient au
     *       groupe, sinon tout le groupe.</li>
     * </ul>
     */
    private Long resoudrePerimetre(StockMasterPrincipal principal, Long groupId, Long filialeId) {
        Long entrepriseIdJwt = principal.getEntrepriseId();

        if (RoleUtilisateur.ADMIN_FILIALE == perimetreAdminGuard.roleDuPrincipal(principal)) {
            if (filialeId != null && !filialeId.equals(entrepriseIdJwt)) {
                // même filiale ≠ même périmètre : refus de contenu sans révéler l'existence des données
                throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
            }
            return entrepriseIdJwt;
        }

        // Admin Groupe : filialeId optionnel, doit appartenir à SON groupe
        if (filialeId != null) {
            Entreprise filiale = entrepriseRepository.findById(filialeId)
                    .filter(f -> f.getGroupe() != null && groupId.equals(f.getGroupe().getId()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.SEC_ACCESS_DENIED));
            return filiale.getId();
        }
        return null;
    }

    /** Protection contre les abus : jamais plus que {@code stockmaster.pagination.max-page-size}. */
    private Pageable bornerTaillePage(Pageable pageable) {
        int taille = Math.min(pageable.getPageSize(), paginationProperties.getMaxPageSize());
        return org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), taille, pageable.getSort());
    }

}
