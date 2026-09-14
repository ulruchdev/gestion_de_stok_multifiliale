package com.stockmaster.groupe.service;

import com.stockmaster.groupe.dto.response.GroupeResponse;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * US-015 — Consulter les informations du groupe et le plan d'abonnement actif.
 *
 * <p>Isolation multi-tenant structurelle : le groupe consulté est toujours celui
 * du JWT ({@code groupId} du principal) — l'endpoint ne prend aucun identifiant,
 * donc aucun risque de consulter le groupe d'un autre tenant.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupeService {

    private final TenantGroupRepository groupeRepository;
    private final EntrepriseRepository entrepriseRepository;

    /**
     * Consulte le groupe du principal connecté.
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal
     *         ne porte pas de {@code groupId} (utilisateur de scope entreprise) ;
     *         {@code RES_ENTITY_NOT_FOUND} si le groupe n'existe pas ou est
     *         soft-supprimé (jamais révéler l'existence).
     */
    public GroupeResponse consulter(StockMasterPrincipal principal) {
        Long groupId = principal != null ? principal.getGroupId() : null;
        if (groupId == null) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }

        TenantGroup groupe = groupeRepository.findById(groupId)
                .filter(g -> !Boolean.TRUE.equals(g.getSupprime()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        long nombreFiliales = entrepriseRepository.countByGroupeIdAndTypeEntrepriseAndActifTrueAndSupprimeFalse(
                groupId, TypeEntreprise.FILIALE);

        return GroupeResponse.builder()
                .id(groupe.getId())
                .nomGroupe(groupe.getNomGroupe())
                .planAbonnement(groupe.getPlanAbonnement())
                .limiteFiliales(groupe.getLimiteFiliales())
                .nombreFiliales(nombreFiliales)
                .dateExpirationPlan(groupe.getDateExpirationPlan())
                .build();
    }
}
