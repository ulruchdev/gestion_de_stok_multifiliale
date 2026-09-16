package com.stockmaster.utilisateur.service;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.UtilisateurRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.shared.security.TokenRevocationPort;
import com.stockmaster.utilisateur.dto.response.UtilisateurListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * US-025 — activer/désactiver un employé ({@code PATCH /{id}/statut}).
 *
 * <p><b>Périmètre</b> : cible inexistante / soft-supprimée / hors périmètre →
 * {@code 404 RES_001} uniforme (jamais révéler l'existence — miroir US-024) ;
 * cible {@code ADMIN_*} → {@code 403 SEC_001} : un admin ne peut pas être
 * désactivé via cet endpoint (les comptes admin relèvent d'un autre cycle de vie).</p>
 *
 * <p><b>Révocation immédiate</b> (critère d'acceptation US-025) : {@code actif=false}
 * → {@link TokenRevocationPort#revoquerSessionsDe(Long)} — les tokens JWT en cours
 * de la cible sont refusés dès la prochaine requête et le refresh est supprimé.
 * La révocation est volontairement <b>après</b> le {@code save} : en cas d'incident
 * Redis, la désactivation en base est conservée (barrière login/refresh
 * {@code actif=false}) et les tokens expireront naturellement (≤ 15 min).</p>
 *
 * <p><b>Historique conservé</b> (critère d'acceptation US-025) : la cible est
 * modifiée, jamais supprimée ni recréée — ses mouvements de stock restent
 * rattachés à son {@code utilisateur_id}.</p>
 *
 * <p><b>Réactivation</b> : {@code actif=true} → aucune révocation (il n'y a rien
 * à révoquer ; la cible peut se reconnecter normalement).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurStatutService {

    private final UtilisateurRepository utilisateurRepository;
    private final TokenRevocationPort tokenRevocationPort;

    /**
     * Bascule le statut actif d'un utilisateur du périmètre.
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal
     *         ne porte pas de groupId ; {@code SEC_ACCESS_DENIED} si le rôle du
     *         principal n'est pas ADMIN_* ou si la cible est un ADMIN_* ;
     *         {@code RES_ENTITY_NOT_FOUND} si la cible est inexistante,
     *         soft-supprimée ou hors périmètre.
     */
    @Transactional
    public UtilisateurListResponse changerStatut(StockMasterPrincipal principal, Long cibleId,
            boolean actif) {

        // Garde-fous fail-fast — avant toute I/O (pattern US-023/024)
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
        if (RoleUtilisateur.ADMIN_GROUPE == cible.getRole()
                || RoleUtilisateur.ADMIN_FILIALE == cible.getRole()) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }

        cible.setActif(actif);
        Utilisateur modifie = utilisateurRepository.save(cible);

        // Critère d'acceptation US-025 : révocation immédiate des tokens, mais
        // uniquement à la désactivation — APRÈS le save (voir Javadoc de classe).
        if (!actif) {
            tokenRevocationPort.revoquerSessionsDe(modifie.getId());
        }

        log.info("US-025 : utilisateur {} {} par l'admin {}",
                modifie.getId(), Boolean.TRUE.equals(actif) ? "réactivé" : "désactivé",
                principal.getUserId());

        return toResponse(modifie);
    }

    // ─── helpers privés (miroir US-024) ─────────────────────────────────────

    /**
     * Vérifie que la cible est dans le périmètre de l'admin connecté :
     * Admin Filiale → entreprise du JWT ; Admin Groupe → filiale de SON groupe.
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
