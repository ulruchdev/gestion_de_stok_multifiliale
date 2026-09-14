package com.stockmaster.groupe.controller;

import com.stockmaster.groupe.GroupeTestApplication;
import com.stockmaster.groupe.dto.response.GroupeResponse;
import com.stockmaster.groupe.service.GroupeService;
import com.stockmaster.shared.domain.enums.PlanAbonnement;
import com.stockmaster.shared.security.StockMasterPrincipal;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-015 — GET /api/v1/groupe : tests contrôleur (200 / 403 / 404).
 */
@WebMvcTest(GroupeController.class)
@ContextConfiguration(classes = GroupeTestApplication.class)
@DisplayName("US-015 — GET /api/v1/groupe")
class GroupeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupeService groupeService;

    private GroupeResponse response;

    @BeforeEach
    void setUp() {
        response = GroupeResponse.builder()
                .id(1L)
                .nomGroupe("Distribo Sarl")
                .planAbonnement(PlanAbonnement.PRO)
                .limiteFiliales(15)
                .nombreFiliales(7L)
                .dateExpirationPlan(LocalDate.of(2027, 6, 30))
                .build();
    }

    private TestingAuthenticationToken asAuthentication(String role, Long groupId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        return new TestingAuthenticationToken(
                new StockMasterPrincipal(9L, claims), null, "ROLE_" + role);
    }

    @Nested
    @DisplayName("GET /api/v1/groupe")
    class Consulter {

        @Test
        @DisplayName("✅ ADMIN_GROUPE → 200 avec nom, plan, limite, nombre de filiales")
        void shouldReturn200WhenAdminGroupe() throws Exception {
            when(groupeService.consulter(org.mockito.ArgumentMatchers.any(StockMasterPrincipal.class)))
                    .thenReturn(response);

            mockMvc.perform(get("/api/v1/groupe")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("ADMIN_GROUPE", 1L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.nomGroupe").value("Distribo Sarl"))
                    .andExpect(jsonPath("$.data.planAbonnement").value("PRO"))
                    .andExpect(jsonPath("$.data.limiteFiliales").value(15))
                    .andExpect(jsonPath("$.data.nombreFiliales").value(7))
                    .andExpect(jsonPath("$.data.dateExpirationPlan").value("2027-06-30"));
        }

        @Test
        @DisplayName("❌ CAISSIER → 403 (@PreAuthorize hasRole ADMIN_GROUPE)")
        void shouldReturn403WhenRoleNotAllowed() throws Exception {
            mockMvc.perform(get("/api/v1/groupe")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("CAISSIER", 1L))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ Non authentifié → 401/403 (accès refusé)")
        void shouldRejectWhenAnonymous() throws Exception {
            mockMvc.perform(get("/api/v1/groupe"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("❌ Groupe introuvable (service) → 404 via GlobalExceptionHandler")
        void shouldReturn404WhenServiceThrowsNotFound() throws Exception {
            when(groupeService.consulter(org.mockito.ArgumentMatchers.any(StockMasterPrincipal.class)))
                    .thenThrow(new com.stockmaster.shared.exception.BusinessException(
                            com.stockmaster.shared.exception.ErrorCode.RES_ENTITY_NOT_FOUND));

            mockMvc.perform(get("/api/v1/groupe")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("ADMIN_GROUPE", 42L))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RES_001"));
        }
    }
}
