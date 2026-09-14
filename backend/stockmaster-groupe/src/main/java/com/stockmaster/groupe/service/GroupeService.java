package com.stockmaster.groupe.service;

import com.stockmaster.groupe.dto.request.GroupeUpdateRequest;
import com.stockmaster.groupe.dto.response.GroupeResponse;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.shared.storage.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * US-015 — Consulter les informations du groupe et le plan d'abonnement actif.
 * US-014 — Modifier le nom, le logo et les informations fiscales du groupe.
 *
 * <p>Isolation multi-tenant structurelle : le groupe consulté/modifié est toujours
 * celui du JWT ({@code groupId} du principal) — les endpoints ne prennent aucun
 * identifiant, donc aucun risque d'agir sur le groupe d'un autre tenant.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupeService {

    private final TenantGroupRepository groupeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final MinioService minioService;

    /**
     * Consulte le groupe du principal connecté.
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal
     *         ne porte pas de {@code groupId} (utilisateur de scope entreprise) ;
     *         {@code RES_ENTITY_NOT_FOUND} si le groupe n'existe pas ou est
     *         soft-supprimé (jamais révéler l'existence).
     */
    public GroupeResponse consulter(StockMasterPrincipal principal) {
        TenantGroup groupe = chargerGroupeDuPrincipal(principal);
        long nombreFiliales = compterFilialesActives(groupe.getId());
        return toResponse(groupe, nombreFiliales);
    }

    /**
     * Modifie partiellement (sémantique PATCH) le groupe du principal connecté.
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal
     *         ne porte pas de {@code groupId} ;
     *         {@code RES_ENTITY_NOT_FOUND} si le groupe n'existe pas ou est soft-supprimé ;
     *         {@code GRP_DUPLICATE_NOM_GROUPE} si le nouveau nom est déjà pris par un autre groupe ;
     *         {@code RES_DUPLICATE_NIF} si le nouveau NIF est déjà pris par un autre groupe ;
     *         propage les erreurs de {@link MinioService} si le logo est refusé/échoue.
     */
    @Transactional
    public GroupeResponse modifier(StockMasterPrincipal principal, GroupeUpdateRequest request) {
        TenantGroup groupe = chargerGroupeDuPrincipal(principal);

        if (StringUtils.hasText(request.getNomGroupe())) {
            if (groupeRepository.existsByNomGroupeAndSupprimeFalseAndIdNot(request.getNomGroupe(), groupe.getId())) {
                throw new BusinessException(ErrorCode.GRP_DUPLICATE_NOM_GROUPE);
            }
            groupe.setNomGroupe(request.getNomGroupe());
        }

        if (StringUtils.hasText(request.getRaisonSociale())) {
            groupe.setRaisonSociale(request.getRaisonSociale());
        }

        if (StringUtils.hasText(request.getNif())) {
            if (groupeRepository.existsByNifAndSupprimeFalseAndIdNot(request.getNif(), groupe.getId())) {
                throw new BusinessException(ErrorCode.RES_DUPLICATE_NIF);
            }
            groupe.setNif(request.getNif());
        }

        if (request.getLogo() != null && !request.getLogo().isEmpty()) {
            String url = minioService.uploadImage("groupe/" + groupe.getId(), request.getLogo());
            groupe.setLogo(url);
        }

        TenantGroup sauvegarde = groupeRepository.save(groupe);
        long nombreFiliales = compterFilialesActives(sauvegarde.getId());
        return toResponse(sauvegarde, nombreFiliales);
    }

    private TenantGroup chargerGroupeDuPrincipal(StockMasterPrincipal principal) {
        Long groupId = principal != null ? principal.getGroupId() : null;
        if (groupId == null) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
        return groupeRepository.findById(groupId)
                .filter(g -> !Boolean.TRUE.equals(g.getSupprime()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));
    }

    private long compterFilialesActives(Long groupId) {
        return entrepriseRepository.countByGroupeIdAndTypeEntrepriseAndActifTrueAndSupprimeFalse(
                groupId, TypeEntreprise.FILIALE);
    }

    private GroupeResponse toResponse(TenantGroup groupe, long nombreFiliales) {
        return GroupeResponse.builder()
                .id(groupe.getId())
                .nomGroupe(groupe.getNomGroupe())
                .planAbonnement(groupe.getPlanAbonnement())
                .limiteFiliales(groupe.getLimiteFiliales())
                .nombreFiliales(nombreFiliales)
                .dateExpirationPlan(groupe.getDateExpirationPlan())
                .logo(groupe.getLogo())
                .nif(groupe.getNif())
                .raisonSociale(groupe.getRaisonSociale())
                .build();
    }
}
