package com.stockmaster.reporting.service;

import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.vente.port.LectureVentePort;
import com.stockmaster.vente.port.LectureVentePort.LigneVenteAgregat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Orchestration des rapports — GS-1251 (socle R0).
 *
 * <p>Squelette commun aux rapports F090 / F092 / F093 : résolution du périmètre
 * depuis le JWT (jamais depuis un paramètre), vérification du groupe, résolution
 * de la période ({@link PeriodeReporting}) puis lecture via
 * {@link LectureVentePort}. Aucune agrégation ici — les rapports (top articles,
 * top clients, comparaison inter-filiales) l'ajoutent dans leurs tickets
 * respectifs (#1252, #1253, #1255) et délèguent les calculs de CA à
 * {@code CalculCaService} (DEC-020).</p>
 *
 * <p><b>Isolation multi-tenant</b> : {@code groupId} et {@code entrepriseId}
 * viennent du JWT ; un principal sans groupe → {@code GRP_002} (403), sans
 * entreprise → {@code SEC_001} (403), groupe inexistant ou inactif →
 * {@code GRP_002}. Ces garde-fous s'exécutent avant toute I/O, comme dans
 * {@code StockService}.</p>
 */
@Service
@RequiredArgsConstructor
public class ReportingService {

    private final LectureVentePort lectureVentePort;
    private final TenantGroupRepository tenantGroupRepository;

    /**
     * Lignes de vente de la filiale du JWT sur la période demandée.
     *
     * @param principal identité du JWT (entreprise + groupe)
     * @param debut     premier jour inclus ({@code null} = début du mois courant)
     * @param fin       dernier jour inclus ({@code null} = fin du mois courant)
     * @throws BusinessException {@code GRP_002}/{@code SEC_001} hors périmètre,
     *                           {@code SYS_002} si la période est invalide
     */
    @Transactional(readOnly = true)
    public List<LigneVenteAgregat> lignesVendues(StockMasterPrincipal principal,
                                                 LocalDate debut, LocalDate fin) {
        Long groupId = groupIdDuPrincipal(principal);
        Long entrepriseId = entrepriseIdDuPrincipal(principal);
        verifierGroupeActif(groupId);

        PeriodeReporting periode = PeriodeReporting.resoudre(debut, fin);
        return lectureVentePort.lignesVendues(entrepriseId, periode.debut(), periode.fin());
    }

    // ─── garde-fous (fail-fast avant toute I/O) ─────────────────────────────

    private Long groupIdDuPrincipal(StockMasterPrincipal principal) {
        Long groupId = principal != null ? principal.getGroupId() : null;
        if (groupId == null) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
        return groupId;
    }

    private Long entrepriseIdDuPrincipal(StockMasterPrincipal principal) {
        Long entrepriseId = principal != null ? principal.getEntrepriseId() : null;
        if (entrepriseId == null) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
        return entrepriseId;
    }

    private void verifierGroupeActif(Long groupId) {
        tenantGroupRepository.findByIdAndSupprimeFalse(groupId)
                .filter(groupe -> Boolean.TRUE.equals(groupe.getActif()))
                .orElseThrow(() -> new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN));
    }
}
