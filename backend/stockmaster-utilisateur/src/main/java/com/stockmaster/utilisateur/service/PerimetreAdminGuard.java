package com.stockmaster.utilisateur.service;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.security.StockMasterPrincipal;
import org.springframework.stereotype.Component;

/**
 * Garde-fous d'administration — un seul exemplaire pour tout le module
 * {@code utilisateur} (US-021/022/023/024/025), remplaçant les copies privées
 * qui s'étaient accumulées dans chaque service (duplication Sonar).
 *
 * <p>Contrats inchangés, identiques d'un endpoint à l'autre :</p>
 * <ul>
 *   <li>{@link #groupIdDuPrincipal} → {@code 403 GRP_CROSS_GROUP_FORBIDDEN} si le
 *       JWT ne porte pas de groupId ;</li>
 *   <li>{@link #roleDuPrincipal} → {@code 403 SEC_001} si le rôle est absent ou
 *       inconnu ;</li>
 *   <li>{@link #verifierPerimetre} → {@code 404 RES_001} uniforme si la cible est
 *       hors périmètre (jamais révéler l'existence — Admin Filiale : entreprise du
 *       JWT ; Admin Groupe : filiale de SON groupe).</li>
 * </ul>
 */
@Component
public class PerimetreAdminGuard {

    public Long groupIdDuPrincipal(StockMasterPrincipal principal) {
        Long groupId = principal != null ? principal.getGroupId() : null;
        if (groupId == null) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
        return groupId;
    }

    public RoleUtilisateur roleDuPrincipal(StockMasterPrincipal principal) {
        String role = principal != null ? principal.getRole() : null;
        if (role == null || role.isBlank()) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
        return RoleUtilisateur.valueOf(role);
    }

    /**
     * Vérifie que la cible est dans le périmètre de l'admin connecté :
     * Admin Filiale → entreprise du JWT ; Admin Groupe → filiale de SON groupe.
     * Sinon → 404 uniforme (jamais révéler l'existence).
     */
    public void verifierPerimetre(Utilisateur cible, Long groupId,
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
}
