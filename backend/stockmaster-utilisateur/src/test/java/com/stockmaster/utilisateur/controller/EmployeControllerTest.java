package com.stockmaster.utilisateur.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.UtilisateurTestApplication;
import com.stockmaster.utilisateur.dto.request.CreerEmployeRequest;
import com.stockmaster.utilisateur.dto.response.EmployeResponse;
import com.stockmaster.utilisateur.service.EmployeService;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-022 — {@code POST /api/v1/utilisateurs/employes}.
 * Tests contrôleur : codes HTTP, format de réponse, RBAC, validation Jakarta.
 */
@WebMvcTest
@ContextConfiguration(classes = UtilisateurTestApplication.class)
@Import(EmployeController.class)
@DisplayName("EmployeController — US-022 : POST /api/v1/utilisateurs/employes")
class EmployeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private EmployeService employeService;

    // ─── helpers ────────────────────────────────────────────────────────────

    /** Construit un TestingAuthenticationToken avec le principal réel — même pattern que US-021. */
    private TestingAuthenticationToken asAuthentication(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        return new TestingAuthenticationToken(
                new StockMasterPrincipal(99L, claims), null, "ROLE_" + role);
    }

    private CreerEmployeRequest requeteValide() {
        CreerEmployeRequest r = new CreerEmployeRequest();
        r.setPrenom("Claude");
        r.setNom("Fotso");
        r.setEmail("claude.fotso@boutique.cm");
        r.setRole(RoleUtilisateur.GESTIONNAIRE_STOCK);
        r.setMotDePasseProvisoire("Prov@2026");
        return r;
    }

    private EmployeResponse responseOk() {
        return EmployeResponse.builder()
                .id(200L).email("claude.fotso@boutique.cm")
                .prenom("Claude").nom("Fotso")
                .role(RoleUtilisateur.GESTIONNAIRE_STOCK)
                .actif(true).entrepriseId(10L)
                .build();
    }

    // ─── cas nominal ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cas nominal")
    class CasNominal {

        @Test
        @DisplayName("✅ 201 — Admin Filiale crée un employé")
        void shouldReturn201WhenAdminFilialeValide() throws Exception {
            when(employeService.creer(any(StockMasterPrincipal.class), any(CreerEmployeRequest.class)))
                    .thenReturn(responseOk());

            mockMvc.perform(post("/api/v1/utilisateurs/employes")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requeteValide())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(200))
                    .andExpect(jsonPath("$.data.email").value("claude.fotso@boutique.cm"))
                    .andExpect(jsonPath("$.data.role").value("GESTIONNAIRE_STOCK"))
                    .andExpect(jsonPath("$.data.actif").value(true))
                    .andExpect(jsonPath("$.data.motDePasseProvisoire").doesNotExist()); // jamais exposé
        }

        @Test
        @DisplayName("✅ 201 — Admin Groupe peut aussi créer un employé")
        void shouldReturn201WhenAdminGroupeValide() throws Exception {
            when(employeService.creer(any(StockMasterPrincipal.class), any(CreerEmployeRequest.class)))
                    .thenReturn(responseOk());

            mockMvc.perform(post("/api/v1/utilisateurs/employes")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_GROUPE", 1L, 2L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requeteValide())))
                    .andExpect(status().isCreated());
        }
    }

    // ─── accès refusé ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Accès refusé")
    class AccesRefuse {

        @Test
        @DisplayName("❌ 403 — non authentifié")
        void shouldReturn403WhenSansToken() throws Exception {
            mockMvc.perform(post("/api/v1/utilisateurs/employes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requeteValide())))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("❌ 403 — rôle CAISSIER ne peut pas créer d'employé")
        void shouldReturn403WhenCaissierEssaieDeCreer() throws Exception {
            mockMvc.perform(post("/api/v1/utilisateurs/employes")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("CAISSIER", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requeteValide())))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── validation Jakarta ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Validation Jakarta")
    class ValidationJakarta {

        @Test
        @DisplayName("❌ 400 — email invalide")
        void shouldReturn400WhenEmailInvalide() throws Exception {
            CreerEmployeRequest req = requeteValide();
            req.setEmail("pas-un-email");

            mockMvc.perform(post("/api/v1/utilisateurs/employes")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ 400 — mot de passe trop court")
        void shouldReturn400WhenMotDePasseTropCourt() throws Exception {
            CreerEmployeRequest req = requeteValide();
            req.setMotDePasseProvisoire("court");

            mockMvc.perform(post("/api/v1/utilisateurs/employes")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ 400 — rôle absent")
        void shouldReturn400WhenRoleAbsent() throws Exception {
            CreerEmployeRequest req = requeteValide();
            req.setRole(null);

            mockMvc.perform(post("/api/v1/utilisateurs/employes")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── erreurs métier propagées ────────────────────────────────────────────

    @Nested
    @DisplayName("Erreurs métier propagées")
    class ErreurMetier {

        @Test
        @DisplayName("❌ 409 — email déjà utilisé → AUTH_008")
        void shouldReturn409WhenEmailDuplique() throws Exception {
            when(employeService.creer(any(), any()))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS));

            mockMvc.perform(post("/api/v1/utilisateurs/employes")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requeteValide())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_008"));
        }

        @Test
        @DisplayName("❌ 403 — limite utilisateurs atteinte → USR_001")
        void shouldReturn403WhenLimiteAtteinte() throws Exception {
            when(employeService.creer(any(), any()))
                    .thenThrow(new BusinessException(ErrorCode.USR_USER_LIMIT_REACHED));

            mockMvc.perform(post("/api/v1/utilisateurs/employes")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requeteValide())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("USR_001"));
        }

        @Test
        @DisplayName("❌ 403 — rôle admin interdit → SEC_001")
        void shouldReturn403WhenRoleAdminInterdit() throws Exception {
            when(employeService.creer(any(), any()))
                    .thenThrow(new BusinessException(ErrorCode.SEC_ACCESS_DENIED));

            mockMvc.perform(post("/api/v1/utilisateurs/employes")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requeteValide())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("SEC_001"));
        }
    }
}
