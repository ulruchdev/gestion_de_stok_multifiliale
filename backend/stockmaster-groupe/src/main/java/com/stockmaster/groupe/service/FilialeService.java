package com.stockmaster.groupe.service;

import com.stockmaster.groupe.dto.request.FilialeCreateRequest;
import com.stockmaster.groupe.dto.response.FilialeResponse;
import com.stockmaster.shared.domain.entity.Entreprise;
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
 * US-016 — Créer une filiale dans le groupe (P0).
 *
 * <p>Isolation multi-tenant structurelle : la filiale est toujours créée dans
 * le groupe du JWT ({@code groupId} du principal) — aucun {@code groupId}
 * n'est accepté en entrée.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FilialeService {

    private final TenantGroupRepository groupeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final ControleLimiteFilialesService controleLimiteFilialesService;

    /**
     * Crée une filiale dans le groupe du principal connecté.
     *
     * @throws BusinessException {@code GRP_CROSS_GROUP_FORBIDDEN} si le principal
     *         ne porte pas de {@code groupId} ;
     *         {@code RES_ENTITY_NOT_FOUND} si le groupe n'existe pas ou est soft-supprimé ;
     *         {@code GRP_FILIALE_LIMIT_REACHED} si la limite du plan (DEC-015) est atteinte ;
     *         {@code RES_DUPLICATE_FILIALE_CODE} si {@code codeFiliale} existe déjà dans le groupe ;
     *         {@code SYS_INTERNAL_ERROR} si la maison mère du groupe est introuvable (invariant violé).
     */
    public FilialeResponse creer(StockMasterPrincipal principal, FilialeCreateRequest request) {
        Long groupId = principal != null ? principal.getGroupId() : null;
        if (groupId == null) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }

        TenantGroup groupe = groupeRepository.findById(groupId)
                .filter(g -> !Boolean.TRUE.equals(g.getSupprime()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        long sitesOperationnels = entrepriseRepository
                .countByGroupeIdAndSiteOperationnelTrueAndActifTrueAndSupprimeFalse(groupId);
        if (!controleLimiteFilialesService.peutAjouterFiliale(sitesOperationnels, groupe.getLimiteFiliales())) {
            throw new BusinessException(ErrorCode.GRP_FILIALE_LIMIT_REACHED);
        }

        if (entrepriseRepository.existsByGroupeIdAndCodeFiliale(groupId, request.getCodeFiliale())) {
            throw new BusinessException(ErrorCode.RES_DUPLICATE_FILIALE_CODE);
        }

        Entreprise maisonMere = entrepriseRepository.findFirstByGroupeIdAndTypeEntreprise(groupId, TypeEntreprise.MERE)
                .orElseThrow(() -> new BusinessException(ErrorCode.SYS_INTERNAL_ERROR,
                        "Maison mère introuvable pour le groupe " + groupId));

        Entreprise filiale = Entreprise.builder()
                .groupe(groupe)
                .parent(maisonMere)
                .typeEntreprise(TypeEntreprise.FILIALE)
                .nom(request.getNom())
                .codeFiliale(request.getCodeFiliale())
                .adresseVille(request.getVille())
                .adresseQuartier(request.getQuartier())
                .build();

        Entreprise sauvegardee = entrepriseRepository.save(filiale);

        return FilialeResponse.builder()
                .id(sauvegardee.getId())
                .nom(sauvegardee.getNom())
                .codeFiliale(sauvegardee.getCodeFiliale())
                .ville(sauvegardee.getAdresseVille())
                .quartier(sauvegardee.getAdresseQuartier())
                .actif(sauvegardee.getActif())
                .siteOperationnel(sauvegardee.getSiteOperationnel())
                .parentId(maisonMere.getId())
                .build();
    }
}
