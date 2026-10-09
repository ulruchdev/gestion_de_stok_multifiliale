package com.stockmaster.catalogue.controller;

import com.stockmaster.catalogue.CatalogueTestApplication;
import com.stockmaster.catalogue.service.CategorieService;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-027 — {@code POST /api/v1/categories} : créer une catégorie.
 * Tests contrôleur : codes HTTP, enveloppe ApiResponse, RBAC, corps JSON.
 */
@WebMvcTest
@ContextConfiguration(classes = CatalogueTestApplication.class)
@Import(CategorieController.class)
@DisplayName("CategorieController — US-027 : POST /api/v1/categories")
class CategorieControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CategorieService categorieService;

    // ─── helpers ────────────────────────────────────────────────────────────

    private TestingAuthenticationToken asAuthentication(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        return new TestingAuthenticationToken(
                new StockMasterPrincipal(99L, claims), null, "ROLE_" + role);
    }

    // ─── cas nominal ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cas nominal")
    class CasNominal {

        @Test
        @DisplayName("✅ 201 — catégorie créée, enveloppe ApiResponse")
        void shouldCreerAvec201() throws Exception {
            when(categorieService.creer(any(StockMasterPrincipal.class), any()))
                    .thenReturn(com.stockmaster.catalogue.dto.response.CategorieResponse.builder()
                            .id(300L).code("ALIM").designation("Alimentation générale")
                            .tauxTva(new java.math.BigDecimal("19.25"))
                            .build());

            mockMvc.perform(post("/api/v1/categories")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\":\"ALIM\",\"designation\":\"Alimentation générale\",\"tauxTva\":19.25}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(300))
                    .andExpect(jsonPath("$.data.code").value("ALIM"))
                    .andExpect(jsonPath("$.data.tauxTva").value(19.25));
        }
    }

    // ─── RBAC ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("RBAC")
    class Rbac {

        @Test
        @DisplayName("❌ 403 — CAISSIER refusé par le garde (@PreAuthorize + service)")
        void shouldRefuserCaissier() throws Exception {
            when(categorieService.creer(any(StockMasterPrincipal.class), any()))
                    .thenThrow(new BusinessException(ErrorCode.SEC_ACCESS_DENIED));

            mockMvc.perform(post("/api/v1/categories")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("CAISSIER", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\":\"ALIM\",\"designation\":\"Alimentation\",\"tauxTva\":19.25}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ 40x — non authentifié")
        void shouldRejeterAnonyme() throws Exception {
            mockMvc.perform(post("/api/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\":\"ALIM\",\"designation\":\"Alimentation\",\"tauxTva\":19.25}"))
                    .andExpect(status().is4xxClientError());
        }
    }

    // ─── erreurs métier propagées ───────────────────────────────────────────

    @Nested
    @DisplayName("Erreurs métier (handler global)")
    class ErreursMetier {

        @Test
        @DisplayName("❌ 409 — code dupliqué (RES_002 propagé par le service)")
        void shouldReturn409FromService() throws Exception {
            when(categorieService.creer(any(StockMasterPrincipal.class), any()))
                    .thenThrow(new BusinessException(ErrorCode.RES_DUPLICATE_CODE));

            mockMvc.perform(post("/api/v1/categories")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\":\"ALIM\",\"designation\":\"Alimentation\",\"tauxTva\":19.25}"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("❌ 400 — taux TVA non supporté (CAT_001 propagé par le service)")
        void shouldReturn400FromService() throws Exception {
            when(categorieService.creer(any(StockMasterPrincipal.class), any()))
                    .thenThrow(new BusinessException(ErrorCode.CAT_TAUX_TVA_INVALIDE));

            mockMvc.perform(post("/api/v1/categories")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\":\"ALIM\",\"designation\":\"Alimentation\",\"tauxTva\":8}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ 400 — validation Jakarta (code absent du corps)")
        void shouldReturn400Validation() throws Exception {
            mockMvc.perform(post("/api/v1/categories")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"designation\":\"Alimentation\",\"tauxTva\":19.25}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
