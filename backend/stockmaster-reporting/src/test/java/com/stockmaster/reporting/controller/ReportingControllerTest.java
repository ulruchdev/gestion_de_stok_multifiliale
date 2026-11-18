package com.stockmaster.reporting.controller;

import com.stockmaster.reporting.ReportingTestApplication;
import com.stockmaster.reporting.dto.response.ComparaisonFilialesResponse;
import com.stockmaster.reporting.dto.response.ComparaisonFilialesResponse.LigneFiliale;
import com.stockmaster.reporting.service.ReportingService;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.security.StockMasterPrincipal;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrôleur reporting — US-F092 / GS-1255 : {@code GET /api/v1/reporting/filiales}.
 *
 * <p>Vérifie le RBAC (ADMIN_GROUPE uniquement, {@code 403} sinon sans appeler le
 * service), l'enveloppe {@code ApiResponse} et la propagation des bornes de
 * période. Contenu mocké au niveau service (pattern {@code StockControllerTest}).</p>
 */
@WebMvcTest
@ContextConfiguration(classes = ReportingTestApplication.class)
@Import(ReportingController.class)
@DisplayName("ReportingController — US-F092 : /api/v1/reporting/filiales")
class ReportingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportingService reportingService;

    private TestingAuthenticationToken asAuthentication(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        when(claims.get("role", String.class)).thenReturn(role);
        return new TestingAuthenticationToken(
                new StockMasterPrincipal(99L, claims), null, "ROLE_" + role);
    }

    private ComparaisonFilialesResponse reponse() {
        return ComparaisonFilialesResponse.builder()
                .filiales(List.of(
                        LigneFiliale.builder()
                                .entrepriseId(11L).nom("Douala").caHt(3000L)
                                .nbVentes(1L).panierMoyen(3000L).build(),
                        LigneFiliale.builder()
                                .entrepriseId(12L).nom("Yaoundé").caHt(0L)
                                .nbVentes(0L).panierMoyen(0L).build()))
                .build();
    }

    @Nested
    @DisplayName("Accès")
    class Acces {

        @Test
        @DisplayName("✅ 200 — ADMIN_GROUPE reçoit les indicateurs du groupe")
        void shouldRetourner200PourAdminGroupe() throws Exception {
            when(reportingService.comparerFiliales(any(), any(), any())).thenReturn(reponse());

            mockMvc.perform(get("/api/v1/reporting/filiales")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_GROUPE", 1L, 10L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.filiales[0].entrepriseId").value(11))
                    .andExpect(jsonPath("$.data.filiales[0].nom").value("Douala"))
                    .andExpect(jsonPath("$.data.filiales[0].caHt").value(3000))
                    .andExpect(jsonPath("$.data.filiales[0].nbVentes").value(1))
                    .andExpect(jsonPath("$.data.filiales[0].panierMoyen").value(3000))
                    .andExpect(jsonPath("$.data.filiales[1].caHt").value(0));
        }

        @Test
        @DisplayName("❌ 403 — ADMIN_FILIALE refusé par @PreAuthorize, service jamais appelé")
        void shouldRefuserAdminFiliale() throws Exception {
            mockMvc.perform(get("/api/v1/reporting/filiales")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L))))
                    .andExpect(status().isForbidden());

            verify(reportingService, never()).comparerFiliales(any(), any(), any());
        }

        @Test
        @DisplayName("❌ 403 — CAISSIER refusé par @PreAuthorize")
        void shouldRefuserCaissier() throws Exception {
            mockMvc.perform(get("/api/v1/reporting/filiales")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("CAISSIER", 1L, 10L))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Paramètres")
    class Parametres {

        @Test
        @DisplayName("✅ bornes de période transmises au service (ISO-8601)")
        void shouldTransmettreLaPeriode() throws Exception {
            when(reportingService.comparerFiliales(any(),
                    eq(LocalDate.of(2026, 11, 1)), eq(LocalDate.of(2026, 11, 30))))
                    .thenReturn(reponse());

            mockMvc.perform(get("/api/v1/reporting/filiales")
                            .param("debut", "2026-11-01")
                            .param("fin", "2026-11-30")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_GROUPE", 1L, 10L))))
                    .andExpect(status().isOk());

            verify(reportingService).comparerFiliales(any(),
                    eq(LocalDate.of(2026, 11, 1)), eq(LocalDate.of(2026, 11, 30)));
        }

        @Test
        @DisplayName("❌ 400 — période invalide remontée par le service (SYS_002)")
        void shouldRetourner400SurPeriodeInvalide() throws Exception {
            when(reportingService.comparerFiliales(any(), any(), any()))
                    .thenThrow(new BusinessException(ErrorCode.SYS_BAD_REQUEST));

            mockMvc.perform(get("/api/v1/reporting/filiales")
                            .param("debut", "2026-11-30")
                            .param("fin", "2026-11-01")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_GROUPE", 1L, 10L))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SYS_002"));
        }

        @Test
        @DisplayName("❌ 400 — date mal formée rejetée avant le service")
        void shouldRefuserDateMalFormee() throws Exception {
            mockMvc.perform(get("/api/v1/reporting/filiales")
                            .param("debut", "30/11/2026")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_GROUPE", 1L, 10L))))
                    .andExpect(status().isBadRequest());

            verify(reportingService, never()).comparerFiliales(any(), any(), any());
        }
    }
}
