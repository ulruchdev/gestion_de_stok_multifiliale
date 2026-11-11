package com.stockmaster.tiers.service;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Garde-fous tenant du module {@code tiers} — un seul exemplaire pour
 * fournisseurs (US-036→039) et clients (US-040→043), sur le modèle de
 * {@code PerimetreAdminGuard} (utilisateur) et {@code PerimetreCatalogueGuard}.
 *
 * <p>Le module tiers est <b>entreprise-scoped</b> (REF §6.2) : l'ancre tenant
 * est l'{@code entreprise_id} du JWT, jamais le corps de la requête. Tous les
 * refus sont <b>fail-fast</b>, avant toute I/O métier :</p>
 * <ul>
 *   <li>rôle absent ou hors du jeu autorisé → {@code 403 SEC_001} ;</li>
 *   <li>JWT sans {@code groupId} → {@code 403 GRP_002} ;</li>
 *   <li>JWT sans {@code entrepriseId} → {@code 403 SEC_001} ;</li>
 *   <li>entreprise introuvable, soft-supprimée ou d'un autre groupe →
 *       {@code 404 RES_001} uniforme (jamais révéler l'existence).</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class TiersPerimetreGuard {

    private final EntrepriseRepository entrepriseRepository;

    /**
     * Refuse tout rôle hors du jeu autorisé — défense en profondeur derrière le
     * {@code @PreAuthorize} du contrôleur.
     *
     * @throws BusinessException {@code SEC_001} si le rôle est absent ou refusé
     */
    public void verifierRole(StockMasterPrincipal principal, Set<String> rolesAutorises) {
        String role = principal != null ? principal.getRole() : null;
        if (role == null || role.isBlank() || !rolesAutorises.contains(role)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
    }

    /**
     * Charge l'entreprise portée par le JWT avec isolation groupe.
     *
     * @throws BusinessException {@code GRP_002} si le JWT n'a pas de groupe ;
     *         {@code SEC_001} s'il n'a pas d'entreprise ; {@code RES_001} si
     *         l'entreprise est introuvable, soft-supprimée ou hors groupe
     */
    public Entreprise chargerEntrepriseDuPrincipal(StockMasterPrincipal principal) {
        Long groupId = principal != null ? principal.getGroupId() : null;
        if (groupId == null) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
        Long entrepriseId = principal.getEntrepriseId();
        if (entrepriseId == null) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
        return entrepriseRepository.findById(entrepriseId)
                .filter(e -> !Boolean.TRUE.equals(e.getSupprime()))
                .filter(e -> e.getGroupe() != null && groupId.equals(e.getGroupe().getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));
    }
}
