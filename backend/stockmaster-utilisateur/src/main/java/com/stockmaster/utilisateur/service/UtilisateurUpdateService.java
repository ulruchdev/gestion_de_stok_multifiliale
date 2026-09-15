package com.stockmaster.utilisateur.service;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.UtilisateurRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.dto.request.UtilisateurUpdateRequest;
import com.stockmaster.utilisateur.dto.response.UtilisateurListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * US-024 — modifier nom, prénom, email ou rôle d'un utilisateur.
 *
 * <p><b>Périmètre</b> (critère d'acceptation US-024) : la cible doit appartenir
 * au périmètre de l'admin connecté — Admin Filiale : sa filiale (JWT) ;
 * Admin Groupe : ses filiales. Hors périmètre, inexistante ou soft-supprimée
 * → {@code 404 RES_001} uniforme (jamais révéler l'existence, critère
 * partagé avec US-018).</p>
 *
 * <p><b>Classe modifiable</b> : la cible ET le nouveau rôle sont limités aux
 * 4 rôles métier — jamais un {@code ADMIN_*} via cet endpoint (les admins se
 * créent par US-021, l'auto-promotion est impossible) → {@code 403 SEC_001}.</p>
 *
 * <p><b>Email</b> : unicité plateforme hors la cible ({@code 409 AUTH_008}) ;
 * {@code emailVerifie} est volontairement inchangé — le flux de vérification
 * arrive avec l'EPIC 12, le réinitialiser verrouillerait le compte sans recours.</p>
 *
 * <p><b>Journalisation</b> (critère d'acceptation US-024) : tout changement
 * de rôle est journalisé en INFO avec l'ancien et le nouveau rôle.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurUpdateService {

    /** Classe modifiable via US-024 — employés métier uniquement. */
    private static final Set<RoleUtilisateur> ROLES_METIER = Set.of(
            RoleUtilisateur.GESTIONNAIRE_STOCK,
            RoleUtilisateur.RESP_ACHATS,
            RoleUtilisateur.COMMERCIAL,
            RoleUtilisateur.CAISSIER
    );

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Modifie partiellement (sémantique PATCH) un utilisateur du périmètre.
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal
     *         ne porte pas de groupId ; {@code SEC_ACCESS_DENIED} si le rôle du
     *         principal n'est pas ADMIN_*, si la cible est un ADMIN_*, ou si le
     *         nouveau rôle est un ADMIN_* ; {@code RES_ENTITY_NOT_FOUND} si la
     *         cible est inexistante, soft-supprimée ou hors périmètre ;
     *         {@code AUTH_EMAIL_ALREADY_EXISTS} si le nouvel email est pris par
     *         un autre utilisateur.
     */
    @Transactional
    public UtilisateurListResponse modifier(StockMasterPrincipal principal, Long cibleId,
            UtilisateurUpdateRequest request) {

        // Garde-fous fail-fast — avant toute I/O
        Long groupId = groupIdDuPrincipal(principal);
        RoleUtilisateur rolePrincipal = roleDuPrincipal(principal);
        if (RoleUtilisateur.ADMIN_FILIALE != rolePrincipal && RoleUtilisateur.ADMIN_GROUPE != rolePrincipal) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        // Chargement + périmètre + classe (404 uniforme / 403 classe)
        Utilisateur cible = utilisateurRepository.findById(cibleId)
                .filter(u -> !Boolean.TRUE.equals(u.getSupprime()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));
        verifierPerimetre(cible, groupId, rolePrincipal, principal.getEntrepriseId());
        if (!ROLES_METIER.contains(cible.getRole())) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        // Nouveau rôle : seulement s'il est fourni, et uniquement vers la classe métier
        if (request.getRole() != null && !ROLES_METIER.contains(request.getRole())) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        // Email : unicité hors la cible (miroir contrainte DB, DEC-008)
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(cible.getEmail())
                && utilisateurRepository.existsByEmailAndIdNot(request.getEmail(), cibleId)) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }

        // Application PATCH — champ null = inchangé ; mot de passe jamais touché
        RoleUtilisateur ancienRole = cible.getRole();
        if (request.getPrenom() != null) cible.setPrenom(request.getPrenom());
        if (request.getNom() != null) cible.setNom(request.getNom());
        if (request.getEmail() != null && !request.getEmail().isBlank()) cible.setEmail(request.getEmail());
        if (request.getRole() != null) cible.setRole(request.getRole());

        Utilisateur modifie = utilisateurRepository.save(cible);

        // Critère d'acceptation US-024 : changement de rôle journalisé
        if (request.getRole() != null && ancienRole != modifie.getRole()) {
            log.info("US-024 : changement de rôle pour l'utilisateur {} — {} → {} (admin={})",
                    modifie.getId(), ancienRole, modifie.getRole(), principal.getUserId());
        }

        return toResponse(modifie);
    }

    // ─── helpers privés ─────────────────────────────────────────────────────

    /**
     * Vérifie que la cible est dans le périmètre de l'admin connecté :
     * <ul>
     *   <li>Admin Filiale : entreprise du JWT uniquement ;</li>
     *   <li>Admin Groupe : n'importe quelle filiale de SON groupe.</li>
     * </ul>
     * Sinon → 404 uniforme (jamais révéler l'existence).
     */
    private void verifierPerimetre(Utilisateur cible, Long groupId,
            RoleUtilisateur rolePrincipal, Long entrepriseIdJwt) {
        Entreprise entrepriseCible = cible.getEntreprise();
        boolean dansPerimetre;
        if (RoleUtilisateur.ADMIN_FILIALE == rolePrincipal) {
            dansPerimetre = entrepriseCible != null && entrepriseIdJwt.equals(entrepriseCible.getId());
        } else {
            dansPerimetre = entrepriseCible != null && entrepriseCible.getGroupe() != null
                    && groupId.equals(entrepriseCible.getGroupe().getId());
        }
        if (!dansPerimetre) {
            throw new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND);
        }
    }

    private Long groupIdDuPrincipal(StockMasterPrincipal principal) {
        Long groupId = principal != null ? principal.getGroupId() : null;
        if (groupId == null) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
        return groupId;
    }

    private RoleUtilisateur roleDuPrincipal(StockMasterPrincipal principal) {
        String role = principal.getRole();
        if (role == null || role.isBlank()) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
        return RoleUtilisateur.valueOf(role);
    }

    private UtilisateurListResponse toResponse(Utilisateur u) {
        return UtilisateurListResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .prenom(u.getPrenom())
                .nom(u.getNom())
                .role(u.getRole())
                .actif(u.getActif())
                .entrepriseId(u.getEntreprise() != null ? u.getEntreprise().getId() : null)
                .build();
    }
}
