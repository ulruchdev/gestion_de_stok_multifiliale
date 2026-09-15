package com.stockmaster.utilisateur.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.UtilisateurTestApplication;
import com.stockmaster.utilisateur.dto.response.UtilisateurListResponse;
import com.stockmaster.utilisateur.service.UtilisateurUpdateService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-024 — {@code PUT /api/v1/utilisateurs/{id}} : modifier un utilisateur.
 * Tests contrôleur : codes HTTP, enveloppe ApiResponse, RBAC, validation Jakarta.
 */
@WebMvcTest
@ContextConfiguration(classes = UtilisateurTestApplication.class)
@Import(UtilisateurUpdateController.class)
@DisplayName("UtilisateurUpdateController — US-024 : PUT /api/v1/utilisateurs/{id}")
class UtilisateurUpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UtilisateurUpdateService utilisateurUpdateService;

    // ─── helpers ────────────────────────────────────────────────────────────

    private static final Long CIBLE_ID = 200L;

    private TestingAuthenticationToken asAuthentication(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        return new TestingAuthenticationToken(
                new StockMasterPrincipal(99L, claims), null, "ROLE_" + role);
    }

    private String corpsJson(String prenom, String nom, String email, String role) {
        StringBuilder sb = new StringBuilder("{");
        if (prenom != null) sb.append("\"prenom\": \"").append(prenom).append("\", ");
        if (nom != null) sb.append("\"nom\": \"").append(nom).append("\", ");
        if (email != null) sb.append("\"email\": \"").append(email).append("\", ");
        if (role != null) sb.append("\"role\": \"").append(role).append("\", ");
        String s = sb.toString();
        return s.endsWith(", ") ? s.substring(0, s.length() - 2) + "}" : s + "}";
    }

    private UtilisateurListResponse responseOk() {
        return UtilisateurListResponse.builder()
                .id(CIBLE_ID)
                .email("claude.fotso@boutique.cm")
                .prenom("Jean").nom("Mbarga")
                .role(RoleUtilisateur.CAISSIER)
                .actif(true).entrepriseId(10L)
                .build();
    }

    // ─── cas nominal ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cas nominal")
    class CasNominal {

        @Test
        @DisplayName("✅ 200 — Admin Filiale modifie un employé (ApiResponse + pas de secret)")
        void shouldReturn200WhenAdminFilialeModifie() throws Exception {
            when(utilisateurUpdateService.modifier(any(StockMasterPrincipal.class), eq(CIBLE_ID), any()))
                    .thenReturn(responseOk());

            mockMvc.perform(put("/api/v1/utilisateurs/" + CIBLE_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpsJson("Jean", "Mbarga", null, null)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(200))
                    .andExpect(jsonPath("$.data.prenom").value("Jean"))
                    .andExpect(jsonPath("$.data.motDePasse").doesNotExist());
        }

        @Test
        @DisplayName("✅ 200 — corps vide {} : sémantique PATCH, aucun champ requis")
        void shouldAccepterCorpsVide() throws Exception {
            when(utilisateurUpdateService.modifier(any(), eq(CIBLE_ID), any()))
                    .thenReturn(responseOk());

            mockMvc.perform(put("/api/v1/utilisateurs/" + CIBLE_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_GROUPE", 1L, 2L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }
    }

    // ─── accès refusé ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Accès refusé")
    class AccesRefuse {

        @Test
        @DisplayName("❌ 403 — non authentifié")
        void shouldRejectAnonymous() throws Exception {
            mockMvc.perform(put("/api/v1/utilisateurs/" + CIBLE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("❌ 403 — un COMMERCIAL ne peut pas modifier d'utilisateur")
        void shouldRejectCommercial() throws Exception {
            mockMvc.perform(put("/api/v1/utilisateurs/" + CIBLE_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("COMMERCIAL", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── validation Jakarta ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Validation Jakarta")
    class ValidationJakarta {

        @Test
        @DisplayName("❌ 400 — email mal formé")
        void shouldReturn400WhenEmailInvalide() throws Exception {
            mockMvc.perform(put("/api/v1/utilisateurs/" + CIBLE_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpsJson(null, null, "pas-un-email", null)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ 400 — nom trop long")
        void shouldReturn400WhenNomTropLong() throws Exception {
            mockMvc.perform(put("/api/v1/utilisateurs/" + CIBLE_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpsJson(null, "x".repeat(101), null, null)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── erreurs métier propagées ────────────────────────────────────────────

    @Nested
    @DisplayName("Erreurs métier propagées")
    class ErreurMetier {

        @Test
        @DisplayName("❌ 404 — cible hors périmètre → RES_001 (jamais révéler l'existence)")
        void shouldReturn404WhenHorsPerimetre() throws Exception {
            when(utilisateurUpdateService.modifier(any(), eq(CIBLE_ID), any()))
                    .thenThrow(new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

            mockMvc.perform(put("/api/v1/utilisateurs/" + CIBLE_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RES_001"));
        }

        @Test
        @DisplayName("❌ 409 — email pris → AUTH_008")
        void shouldReturn409WhenEmailPris() throws Exception {
            when(utilisateurUpdateService.modifier(any(), eq(CIBLE_ID), any()))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS));

            mockMvc.perform(put("/api/v1/utilisateurs/" + CIBLE_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpsJson(null, null, "deja.pris@boutique.cm", null)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_008"));
        }

        @Test
        @DisplayName("❌ 403 — cible classe non modifiable → SEC_001")
        void shouldReturn403WhenClasseNonModifiable() throws Exception {
            when(utilisateurUpdateService.modifier(any(), eq(CIBLE_ID), any()))
                    .thenThrow(new BusinessException(ErrorCode.SEC_ACCESS_DENIED));

            mockMvc.perform(put("/api/v1/utilisateurs/" + CIBLE_ID)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("SEC_001"));
        }
    }
}
