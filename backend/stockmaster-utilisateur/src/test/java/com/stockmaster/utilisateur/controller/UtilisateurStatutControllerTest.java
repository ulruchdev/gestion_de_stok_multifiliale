package com.stockmaster.utilisateur.controller;

import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.UtilisateurTestApplication;
import com.stockmaster.utilisateur.dto.response.UtilisateurListResponse;
import com.stockmaster.utilisateur.service.UtilisateurStatutService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-025 — {@code PATCH /api/v1/utilisateurs/{id}/statut} : activer/désactiver.
 * Tests contrôleur : codes HTTP, enveloppe ApiResponse, RBAC, corps JSON.
 */
@WebMvcTest
@ContextConfiguration(classes = UtilisateurTestApplication.class)
@Import(UtilisateurStatutController.class)
@DisplayName("UtilisateurStatutController — US-025 : PATCH /api/v1/utilisateurs/{id}/statut")
class UtilisateurStatutControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UtilisateurStatutService utilisateurStatutService;

    // ─── helpers ────────────────────────────────────────────────────────────

    private static final Long CIBLE_ID = 200L;

    private TestingAuthenticationToken asAuthentication(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        return new TestingAuthenticationToken(
                new StockMasterPrincipal(99L, claims), null, "ROLE_" + role);
    }

    private UtilisateurListResponse responseAvec(boolean actif) {
        return UtilisateurListResponse.builder()
                .id(CIBLE_ID)
                .email("employe@boutique.cm")
                .prenom("Jean").nom("Mbarga")
                .role(RoleUtilisateur.CAISSIER)
                .actif(actif).entrepriseId(10L)
                .build();
    }

    // ─── cas nominal ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cas nominal")
    class CasNominal {

        @Test
        @DisplayName("✅ 200 — désactivation : ApiResponse avec actif=false")
        void shouldDesactiverAvec200() throws Exception {
            when(utilisateurStatutService.changerStatut(
                    ArgumentMatchers.any(StockMasterPrincipal.class), eq(CIBLE_ID), eq(false)))
                    .thenReturn(responseAvec(false));

            mockMvc.perform(patch("/api/v1/utilisateurs/" + CIBLE_ID + "/statut")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"actif\": false}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(200))
                    .andExpect(jsonPath("$.data.actif").value(false))
                    .andExpect(jsonPath("$.data.motDePasse").doesNotExist());
        }

        @Test
        @DisplayName("✅ 200 — réactivation : ApiResponse avec actif=true")
        void shouldReactiverAvec200() throws Exception {
            when(utilisateurStatutService.changerStatut(
                    ArgumentMatchers.any(StockMasterPrincipal.class), eq(CIBLE_ID), eq(true)))
                    .thenReturn(responseAvec(true));

            mockMvc.perform(patch("/api/v1/utilisateurs/" + CIBLE_ID + "/statut")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_GROUPE", 1L, 2L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"actif\": true}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.actif").value(true));
        }
    }

    // ─── validation du corps ────────────────────────────────────────────────

    @Nested
    @DisplayName("Validation du corps")
    class ValidationCorps {

        @Test
        @DisplayName("❌ 400 — actif null (le champ est obligatoire : pas de bascule implicite)")
        void shouldRejectActifNull() throws Exception {
            mockMvc.perform(patch("/api/v1/utilisateurs/" + CIBLE_ID + "/statut")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ 400 — corps mal formé")
        void shouldRejectCorpsMalForme() throws Exception {
            mockMvc.perform(patch("/api/v1/utilisateurs/" + CIBLE_ID + "/statut")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("pas-du-json"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── accès refusé ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Accès refusé")
    class AccesRefuse {

        @Test
        @DisplayName("❌ 403 — rôle métier (défense en profondeur après @PreAuthorize)")
        void shouldRejectRoleMetier() throws Exception {
            mockMvc.perform(patch("/api/v1/utilisateurs/" + CIBLE_ID + "/statut")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("CAISSIER", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"actif\": false}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ 40x — non authentifié")
        void shouldRejectAnonymous() throws Exception {
            mockMvc.perform(patch("/api/v1/utilisateurs/" + CIBLE_ID + "/statut")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"actif\": false}"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("❌ 404 — périmètre (BusinessException propagée par le handler global)")
        void shouldReturn404FromService() throws Exception {
            when(utilisateurStatutService.changerStatut(
                    ArgumentMatchers.any(StockMasterPrincipal.class), eq(CIBLE_ID), eq(false)))
                    .thenThrow(new com.stockmaster.shared.exception.BusinessException(
                            com.stockmaster.shared.exception.ErrorCode.RES_ENTITY_NOT_FOUND));

            mockMvc.perform(patch("/api/v1/utilisateurs/" + CIBLE_ID + "/statut")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"actif\": false}"))
                    .andExpect(status().isNotFound());
        }
    }
}
