package com.stockmaster.reporting.service;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.reporting.dto.response.ComparaisonFilialesResponse;
import com.stockmaster.vente.port.LectureVentePort;
import com.stockmaster.vente.port.LectureVentePort.LigneVenteAgregat;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests du socle reporting — GS-1251 (R0).
 *
 * <p>Couvre les deux responsabilités du squelette : le périmètre multi-tenant
 * (JWT uniquement, groupe actif) et la résolution de période (défaut mois
 * courant, bornes semi-ouvertes, refus des périodes incohérentes ou trop
 * longues). L'agrégation métier est testée dans les tickets R1/R3/R4.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportingService — GS-1251 : socle des rapports F090/F092/F093")
class ReportingServiceTest {

    @Mock
    private LectureVentePort lectureVentePort;

    @Mock
    private TenantGroupRepository tenantGroupRepository;

    @Mock
    private EntrepriseRepository entrepriseRepository;

    private final CalculCaService calculCaService = new CalculCaService();
    private ReportingService reportingService;

    private static final Long GROUP_ID = 1L;
    private static final Long ENTREPRISE_ID = 10L;

    @BeforeEach
    void setUp() {
        reportingService = new ReportingService(
                lectureVentePort, tenantGroupRepository, entrepriseRepository, calculCaService);
    }

    // ─── fixtures ───────────────────────────────────────────────────────────

    private StockMasterPrincipal principal(Long groupId, Long entrepriseId) {
        return principal("GESTIONNAIRE_STOCK", groupId, entrepriseId);
    }

    private StockMasterPrincipal principal(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        lenient().when(claims.get("groupId", Long.class)).thenReturn(groupId);
        lenient().when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        lenient().when(claims.get("role", String.class)).thenReturn(role);
        return new StockMasterPrincipal(99L, claims);
    }

    private Entreprise entreprise(long id, String nom) {
        Entreprise entreprise = Entreprise.builder().nom(nom).build();
        entreprise.setId(id);
        return entreprise;
    }

    private TenantGroup groupe(boolean actif) {
        TenantGroup groupe = TenantGroup.builder().nomGroupe("Boutique CM").build();
        groupe.setId(GROUP_ID);
        groupe.setActif(actif);
        groupe.setSupprime(false);
        return groupe;
    }

    private void groupeActif() {
        when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                .thenReturn(Optional.of(groupe(true)));
    }

    private LigneVenteAgregat ligne(long articleId, String libelle, String quantite) {
        return ligne(500L, ENTREPRISE_ID, articleId, libelle, quantite, 12000, "19.25");
    }

    private LigneVenteAgregat ligne(long venteId, long entrepriseId, long articleId,
                                    String libelle, String quantite,
                                    int prixUnitaire, String tauxTva) {
        return new LigneVenteAgregat(venteId, entrepriseId, articleId, libelle, 7L, "Alice",
                new BigDecimal(quantite), prixUnitaire, new BigDecimal(tauxTva),
                Instant.parse("2026-11-12T09:30:00Z"));
    }

    // ─── délégation au port ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Lecture des ventes de la filiale du JWT")
    class LignesVendues {

        @Test
        @DisplayName("✅ délègue au port avec des bornes UTC semi-ouvertes (fin exclue)")
        void shouldDeleguerAvecBornesSemiOuvertes() {
            groupeActif();
            when(lectureVentePort.lignesVendues(
                    ENTREPRISE_ID,
                    Instant.parse("2026-11-01T00:00:00Z"),
                    Instant.parse("2026-12-01T00:00:00Z")))
                    .thenReturn(List.of(ligne(5L, "Riz sac 25 kg", "2.500")));

            List<LigneVenteAgregat> lignes = reportingService.lignesVendues(
                    principal(GROUP_ID, ENTREPRISE_ID),
                    LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

            assertThat(lignes).hasSize(1);
            assertThat(lignes.get(0).articleId()).isEqualTo(5L);
            verify(lectureVentePort).lignesVendues(
                    ENTREPRISE_ID,
                    Instant.parse("2026-11-01T00:00:00Z"),
                    Instant.parse("2026-12-01T00:00:00Z"));
        }

        @Test
        @DisplayName("✅ sans dates : mois courant complet, bornes calculées en UTC")
        void shouldUtiliserLeMoisCourantParDefaut() {
            groupeActif();
            LocalDate aujourdHui = LocalDate.now(ZoneOffset.UTC);
            Instant attenduDebut = aujourdHui.withDayOfMonth(1)
                    .atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant attenduFin = aujourdHui.withDayOfMonth(1).plusMonths(1)
                    .atStartOfDay(ZoneOffset.UTC).toInstant();
            when(lectureVentePort.lignesVendues(ENTREPRISE_ID, attenduDebut, attenduFin))
                    .thenReturn(List.of());

            assertThat(reportingService.lignesVendues(
                    principal(GROUP_ID, ENTREPRISE_ID), null, null)).isEmpty();

            verify(lectureVentePort).lignesVendues(ENTREPRISE_ID, attenduDebut, attenduFin);
        }

        @Test
        @DisplayName("✅ période sans vente : liste vide, aucun repli silencieux")
        void shouldRetournerListeVide() {
            groupeActif();
            when(lectureVentePort.lignesVendues(anyLong(), any(), any())).thenReturn(List.of());

            assertThat(reportingService.lignesVendues(
                    principal(GROUP_ID, ENTREPRISE_ID),
                    LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30))).isEmpty();
        }
    }

    // ─── garde-fous ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Périmètre et période")
    class GardeFous {

        @Test
        @DisplayName("❌ 400 SYS_002 — fin antérieure au début, avant toute I/O")
        void shouldRefuserPeriodeInversee() {
            groupeActif();

            var ex = catchThrowableOfType(
                    () -> reportingService.lignesVendues(
                            principal(GROUP_ID, ENTREPRISE_ID),
                            LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1)),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
            verify(lectureVentePort, never()).lignesVendues(any(), any(), any());
        }

        @Test
        @DisplayName("❌ 400 SYS_002 — période de plus de 366 jours")
        void shouldRefuserPeriodeTropLongue() {
            groupeActif();

            var ex = catchThrowableOfType(
                    () -> reportingService.lignesVendues(
                            principal(GROUP_ID, ENTREPRISE_ID),
                            LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 2)),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
            verify(lectureVentePort, never()).lignesVendues(any(), any(), any());
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupId absent du JWT, avant toute I/O")
        void shouldRefuserPrincipalSansGroupId() {
            var ex = catchThrowableOfType(
                    () -> reportingService.lignesVendues(
                            principal(null, ENTREPRISE_ID),
                            LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verify(tenantGroupRepository, never()).findByIdAndSupprimeFalse(any());
            verify(lectureVentePort, never()).lignesVendues(any(), any(), any());
        }

        @Test
        @DisplayName("❌ 403 SEC_001 — entrepriseId absent du JWT, avant toute I/O")
        void shouldRefuserPrincipalSansEntrepriseId() {
            var ex = catchThrowableOfType(
                    () -> reportingService.lignesVendues(
                            principal(GROUP_ID, null),
                            LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
            verify(tenantGroupRepository, never()).findByIdAndSupprimeFalse(any());
            verify(lectureVentePort, never()).lignesVendues(any(), any(), any());
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupe inexistant ou supprimé")
        void shouldRefuserGroupeInexistant() {
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.empty());

            var ex = catchThrowableOfType(
                    () -> reportingService.lignesVendues(
                            principal(GROUP_ID, ENTREPRISE_ID),
                            LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verify(lectureVentePort, never()).lignesVendues(any(), any(), any());
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupe inactif (suspension / plan expiré)")
        void shouldRefuserGroupeInactif() {
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupe(false)));

            var ex = catchThrowableOfType(
                    () -> reportingService.lignesVendues(
                            principal(GROUP_ID, ENTREPRISE_ID),
                            LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verify(lectureVentePort, never()).lignesVendues(any(), any(), any());
        }
    }

    // ─── US-F092 — comparaison inter-filiales ────────────────────────────────

    @Nested
    @DisplayName("US-F092 — GET /api/v1/reporting/filiales")
    class ComparaisonFiliales {

        private final LocalDate debut = LocalDate.of(2026, 11, 1);
        private final LocalDate fin = LocalDate.of(2026, 11, 30);

        @Test
        @DisplayName("✅ une ligne par site, tri CA décroissant, panier moyen TTC HALF_UP")
        void shouldComparerLesFiliales() {
            groupeActif();
            List<Entreprise> entreprises = List.of(
                    entreprise(10L, "Douala"), entreprise(11L, "Bafoussam"), entreprise(12L, "Yaoundé"));
            when(entrepriseRepository.findAllByGroupeIdAndSupprimeFalseOrderByNomAsc(GROUP_ID))
                    .thenReturn(entreprises);
            when(lectureVentePort.lignesVenduesGroupe(anyList(), any(), any()))
                    .thenReturn(List.of(
                            ligne(501L, 10L, 5L, "Riz", "2.000", 1000, "19.25"),
                            ligne(502L, 10L, 6L, "Huile", "1.000", 500, "19.25"),
                            ligne(503L, 12L, 5L, "Riz", "3.000", 1000, "0")));

            ComparaisonFilialesResponse reponse = reportingService.comparerFiliales(
                    principal("ADMIN_GROUPE", GROUP_ID, ENTREPRISE_ID), debut, fin);

            assertThat(reponse.getFiliales()).hasSize(3);
            // Yaoundé 3000 > Douala 2500 > Bafoussam 0
            assertThat(reponse.getFiliales()).extracting(ComparaisonFilialesResponse.LigneFiliale::getNom)
                    .containsExactly("Yaoundé", "Douala", "Bafoussam");

            ComparaisonFilialesResponse.LigneFiliale douala = reponse.getFiliales().get(1);
            assertThat(douala.getEntrepriseId()).isEqualTo(10L);
            assertThat(douala.getCaHt()).isEqualTo(2500L);
            assertThat(douala.getNbVentes()).isEqualTo(2L);
            // CA TTC = 2981, /2 ventes = 1490,5 → 1491 (HALF_UP)
            assertThat(douala.getPanierMoyen()).isEqualTo(1491L);

            ComparaisonFilialesResponse.LigneFiliale yaounde = reponse.getFiliales().get(0);
            assertThat(yaounde.getCaHt()).isEqualTo(3000L);
            assertThat(yaounde.getNbVentes()).isEqualTo(1L);
            assertThat(yaounde.getPanierMoyen()).isEqualTo(3000L);

            ComparaisonFilialesResponse.LigneFiliale inactive = reponse.getFiliales().get(2);
            assertThat(inactive.getEntrepriseId()).isEqualTo(11L);
            assertThat(inactive.getCaHt()).isZero();
            assertThat(inactive.getNbVentes()).isZero();
            assertThat(inactive.getPanierMoyen()).isZero();

            verify(lectureVentePort).lignesVenduesGroupe(
                    List.of(10L, 11L, 12L),
                    Instant.parse("2026-11-01T00:00:00Z"),
                    Instant.parse("2026-12-01T00:00:00Z"));
        }

        @Test
        @DisplayName("✅ groupe sans entreprise : réponse vide, aucun appel au port")
        void shouldRetournerVideSansEntreprise() {
            groupeActif();
            when(entrepriseRepository.findAllByGroupeIdAndSupprimeFalseOrderByNomAsc(GROUP_ID))
                    .thenReturn(List.of());

            ComparaisonFilialesResponse reponse = reportingService.comparerFiliales(
                    principal("ADMIN_GROUPE", GROUP_ID, ENTREPRISE_ID), debut, fin);

            assertThat(reponse.getFiliales()).isEmpty();
            verify(lectureVentePort, never()).lignesVenduesGroupe(anyList(), any(), any());
        }

        @Test
        @DisplayName("❌ 403 SEC_001 — ADMIN_FILIALE refusé avant toute I/O")
        void shouldRefuserRoleNonGroupe() {
            var ex = catchThrowableOfType(
                    () -> reportingService.comparerFiliales(
                            principal("ADMIN_FILIALE", GROUP_ID, ENTREPRISE_ID), debut, fin),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
            verify(tenantGroupRepository, never()).findByIdAndSupprimeFalse(any());
            verify(entrepriseRepository, never()).findAllByGroupeIdAndSupprimeFalseOrderByNomAsc(any());
            verify(lectureVentePort, never()).lignesVenduesGroupe(anyList(), any(), any());
        }

        @Test
        @DisplayName("❌ 400 SYS_002 — période invalide, aucun accès aux filiales")
        void shouldRefuserPeriodeInvalide() {
            groupeActif();

            var ex = catchThrowableOfType(
                    () -> reportingService.comparerFiliales(
                            principal("ADMIN_GROUPE", GROUP_ID, ENTREPRISE_ID), fin, debut),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
            verify(entrepriseRepository, never()).findAllByGroupeIdAndSupprimeFalseOrderByNomAsc(any());
            verify(lectureVentePort, never()).lignesVenduesGroupe(anyList(), any(), any());
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupe inactif")
        void shouldRefuserGroupeInactif() {
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupe(false)));

            var ex = catchThrowableOfType(
                    () -> reportingService.comparerFiliales(
                            principal("ADMIN_GROUPE", GROUP_ID, ENTREPRISE_ID), debut, fin),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verify(entrepriseRepository, never()).findAllByGroupeIdAndSupprimeFalseOrderByNomAsc(any());
        }
    }
}
