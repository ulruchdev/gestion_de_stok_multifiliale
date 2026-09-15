package com.stockmaster.utilisateur.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.UtilisateurTestApplication;
import com.stockmaster.utilisateur.dto.response.UtilisateurListResponse;
import com.stockmaster.utilisateur.service.UtilisateurListService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-023 — {@code GET /api/v1/utilisateurs} : lister les utilisateurs du
 * périmètre. Tests contrôleur : codes HTTP, enveloppe {@code PageResponse},
 * RBAC, validation des filtres.
 */
@WebMvcTest
@ContextConfiguration(classes = UtilisateurTestApplication.class)
@Import(UtilisateurListController.class)
@DisplayName("UtilisateurListController — US-023 : GET /api/v1/utilisateurs")
class UtilisateurListControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UtilisateurListService utilisateurListService;

    // ─── helpers ────────────────────────────────────────────────────────────

    private TestingAuthenticationToken asAuthentication(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        return new TestingAuthenticationToken(
                new StockMasterPrincipal(99L, claims), null, "ROLE_" + role);
    }

    private UtilisateurListResponse employe(Long id, RoleUtilisateur role) {
        return UtilisateurListResponse.builder()
                .id(id)
                .email("user" + id + "@boutique.cm")
                .prenom("Prenom" + id)
                .nom("Nom" + id)
                .role(role)
                .actif(true)
                .entrepriseId(10L)
                .build();
    }

    private PageResponse<UtilisateurListResponse> pageOf(UtilisateurListResponse... items) {
        return PageResponse.<UtilisateurListResponse>builder()
                .content(List.of(items))
                .page(0).size(20).totalElements(items.length).totalPages(1)
                .build();
    }

    // ─── cas nominal ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cas nominal")
    class CasNominal {

        @Test
        @DisplayName("✅ 200 — Admin Groupe liste les utilisateurs (PageResponse + ApiResponse)")
        void shouldReturn200WhenAdminGroupe() throws Exception {
            when(utilisateurListService.lister(any(StockMasterPrincipal.class), isNull(), isNull(),
                    isNull(), any(Pageable.class)))
                    .thenReturn(pageOf(
                            employe(200L, RoleUtilisateur.CAISSIER),
                            employe(201L, RoleUtilisateur.COMMERCIAL)));

            mockMvc.perform(get("/api/v1/utilisateurs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_GROUPE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.content[0].id").value(200))
                    .andExpect(jsonPath("$.data.content[0].role").value("CAISSIER"))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.content[0].motDePasse").doesNotExist())
                    .andExpect(jsonPath("$.data.content[0].motDePasseProvisoire").doesNotExist());
        }

        @Test
        @DisplayName("✅ 200 — Admin Filiale liste les utilisateurs de sa filiale")
        void shouldReturn200WhenAdminFiliale() throws Exception {
            when(utilisateurListService.lister(any(StockMasterPrincipal.class), isNull(), isNull(),
                    isNull(), any(Pageable.class)))
                    .thenReturn(pageOf(employe(202L, RoleUtilisateur.GESTIONNAIRE_STOCK)));

            mockMvc.perform(get("/api/v1/utilisateurs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].id").value(202));
        }

        @Test
        @DisplayName("✅ 200 — filtres role/actif/filialeId transmis au service")
        void shouldTransmettreFiltres() throws Exception {
            when(utilisateurListService.lister(any(), eq(RoleUtilisateur.COMMERCIAL), eq(true),
                    eq(11L), any(Pageable.class)))
                    .thenReturn(pageOf());

            mockMvc.perform(get("/api/v1/utilisateurs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_GROUPE", 1L, 10L)))
                            .param("role", "COMMERCIAL")
                            .param("actif", "true")
                            .param("filialeId", "11")
                            .contentType(MediaType.APPLICATION_JSON))
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
            mockMvc.perform(get("/api/v1/utilisateurs")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("❌ 403 — un CAISSIER ne peut pas lister les utilisateurs")
        void shouldRejectCaissier() throws Exception {
            mockMvc.perform(get("/api/v1/utilisateurs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("CAISSIER", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ 403 — périmètre refusé côté service (autre filiale) → SEC_001")
        void shouldPropagerPerimetreRefuse() throws Exception {
            when(utilisateurListService.lister(any(), any(), any(), any(), any()))
                    .thenThrow(new BusinessException(ErrorCode.SEC_ACCESS_DENIED));

            mockMvc.perform(get("/api/v1/utilisateurs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L)))
                            .param("filialeId", "11")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("SEC_001"));
        }
    }

    // ─── validation des filtres ─────────────────────────────────────────────

    @Nested
    @DisplayName("Validation des filtres")
    class ValidationFiltres {

        @Test
        @DisplayName("❌ 400 — rôle inconnu (method argument type mismatch)")
        void shouldReturn400WhenRoleInconnu() throws Exception {
            mockMvc.perform(get("/api/v1/utilisateurs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_GROUPE", 1L, 10L)))
                            .param("role", "PIRATE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
