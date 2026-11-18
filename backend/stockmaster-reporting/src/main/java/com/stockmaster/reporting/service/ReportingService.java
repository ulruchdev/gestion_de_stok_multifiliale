package com.stockmaster.reporting.service;

import com.stockmaster.reporting.dto.response.ComparaisonFilialesResponse;
import com.stockmaster.reporting.dto.response.ComparaisonFilialesResponse.LigneFiliale;
import com.stockmaster.reporting.service.CalculCaService.LigneReport;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.vente.port.LectureVentePort;
import com.stockmaster.vente.port.LectureVentePort.LigneVenteAgregat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestration des rapports — socle GS-1251 + comparaison inter-filiales GS-1255.
 *
 * <p>Périmètre multi-tenant : {@code groupId} et {@code entrepriseId} viennent
 * du JWT, jamais d'un paramètre HTTP. Un principal sans groupe → {@code GRP_002}
 * (403), sans entreprise → {@code SEC_001} (403), groupe inexistant ou inactif
 * → {@code GRP_002}.</p>
 *
 * <p>Les calculs de CA déléguent à {@link CalculCaService} ({@code DEC-020}) :
 * aucune formule dupliquée dans le service ni dans le contrôleur.</p>
 */
@Service
@RequiredArgsConstructor
public class ReportingService {

    private static final String ROLE_ADMIN_GROUPE = "ADMIN_GROUPE";

    private final LectureVentePort lectureVentePort;
    private final TenantGroupRepository tenantGroupRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final CalculCaService calculCaService;

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

    /**
     * US-F092 — comparaison inter-filiales du groupe du JWT.
     *
     * <p>Toutes les entreprises non supprimées du groupe apparaissent, y compris
     * celles sans vente (ligne à 0, aucune omission). Tri par CA HT décroissant
     * puis nom, pour que l'écran R5 n'ait pas à re-trier.</p>
     *
     * @param principal identité du JWT — rôle {@code ADMIN_GROUPE} exigé
     * @param debut     premier jour inclus ({@code null} = début du mois courant)
     * @param fin       dernier jour inclus ({@code null} = fin du mois courant)
     * @throws BusinessException {@code SEC_001} si le rôle n'est pas ADMIN_GROUPE,
     *                           {@code GRP_002} hors périmètre groupe, {@code SYS_002}
     *                           si la période est invalide
     */
    @Transactional(readOnly = true)
    public ComparaisonFilialesResponse comparerFiliales(StockMasterPrincipal principal,
                                                        LocalDate debut, LocalDate fin) {
        verifierRoleAdminGroupe(principal);
        Long groupId = groupIdDuPrincipal(principal);
        verifierGroupeActif(groupId);

        PeriodeReporting periode = PeriodeReporting.resoudre(debut, fin);
        List<Entreprise> entreprises = entrepriseRepository
                .findAllByGroupeIdAndSupprimeFalseOrderByNomAsc(groupId);
        List<Long> entrepriseIds = entreprises.stream().map(Entreprise::getId).toList();
        List<LigneVenteAgregat> lignes = entrepriseIds.isEmpty()
                ? List.of()
                : lectureVentePort.lignesVenduesGroupe(
                        entrepriseIds, periode.debut(), periode.fin());

        Map<Long, List<LigneVenteAgregat>> lignesParEntreprise = lignes.stream()
                .collect(Collectors.groupingBy(LigneVenteAgregat::entrepriseId));

        List<LigneFiliale> filiales = entreprises.stream()
                .map(entreprise -> versLigneFiliale(
                        entreprise, lignesParEntreprise.getOrDefault(entreprise.getId(), List.of())))
                .sorted(Comparator.comparingLong(LigneFiliale::getCaHt).reversed()
                        .thenComparing(LigneFiliale::getNom))
                .toList();

        return ComparaisonFilialesResponse.builder().filiales(filiales).build();
    }

    /** Agrégats d'un site : CA HT, ventes distinctes et panier moyen TTC. */
    private LigneFiliale versLigneFiliale(Entreprise entreprise, List<LigneVenteAgregat> lignes) {
        List<LigneReport> lignesReport = lignes.stream()
                .map(ligne -> new LigneReport(ligne.articleId(), ligne.quantite(),
                        ligne.prixUnitaireHt(), ligne.tauxTva()))
                .toList();

        long caHt = calculCaService.caHtFacture(lignesReport);
        long nbVentes = lignes.stream()
                .map(LigneVenteAgregat::venteId)
                .distinct()
                .count();
        long panierMoyen = nbVentes == 0
                ? 0L
                : BigDecimal.valueOf(calculCaService.caTtcFacture(lignesReport))
                        .divide(BigDecimal.valueOf(nbVentes), 0, RoundingMode.HALF_UP)
                        .longValueExact();

        return LigneFiliale.builder()
                .entrepriseId(entreprise.getId())
                .nom(entreprise.getNom())
                .caHt(caHt)
                .nbVentes(nbVentes)
                .panierMoyen(panierMoyen)
                .build();
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

    /** Défense en profondeur après le {@code @PreAuthorize} du contrôleur. */
    private void verifierRoleAdminGroupe(StockMasterPrincipal principal) {
        String role = principal != null ? principal.getRole() : null;
        if (!ROLE_ADMIN_GROUPE.equals(role)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
    }

    private void verifierGroupeActif(Long groupId) {
        tenantGroupRepository.findByIdAndSupprimeFalse(groupId)
                .filter(groupe -> Boolean.TRUE.equals(groupe.getActif()))
                .orElseThrow(() -> new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN));
    }
}
