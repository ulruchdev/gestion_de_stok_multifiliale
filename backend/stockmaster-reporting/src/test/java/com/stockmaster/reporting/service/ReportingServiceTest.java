package com.stockmaster.reporting.service;

import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
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

    private ReportingService reportingService;

    private static final Long GROUP_ID = 1L;
    private static final Long ENTREPRISE_ID = 10L;

    @BeforeEach
    void setUp() {
        reportingService = new ReportingService(lectureVentePort, tenantGroupRepository);
    }

    // ─── fixtures ───────────────────────────────────────────────────────────

    private StockMasterPrincipal principal(Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        lenient().when(claims.get("groupId", Long.class)).thenReturn(groupId);
        lenient().when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        return new StockMasterPrincipal(99L, claims);
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
        return new LigneVenteAgregat(ENTREPRISE_ID, articleId, libelle, 7L, "Alice",
                new BigDecimal(quantite), 12000, new BigDecimal("19.25"),
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
}
