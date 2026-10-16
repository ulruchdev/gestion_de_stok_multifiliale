package com.stockmaster.catalogue.controller;

import com.stockmaster.catalogue.CatalogueTestApplication;
import com.stockmaster.catalogue.dto.request.CategorieUpdateRequest;
import com.stockmaster.catalogue.dto.response.CategorieListResponse;
import com.stockmaster.catalogue.dto.response.CategorieResponse;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.assertj.core.api.SoftAssertions;

/**
 * Opérations catalogue group-scoped (DEC-002, V5 §6).
 *
 * <ul>
 *   <li>US-027 — POST /api/v1/categories : créer une catégorie</li>
 *   <li>US-028 — GET  /api/v1/categories : liste paginée + search (designation)</li>
 *   <li>US-029 — PUT  /api/v1/categories/{id} : modifier designation / taux TVA (PATCH)</li>
 *   <li>US-030 — DELETE /api/v1/categories/{id} : suppression logique, bloquée si
 *       articles attachés (DEC-028)</li>
 * </ul>
 *
 * <p>RBAC partagé (GESTIONNAIRE_STOCK / ADMIN_FILIALE / ADMIN_GROUPE) via
 * @PreAuthorize. Le contenu est mocké au niveau service pour isoler le contrôleur.
 */
@WebMvcTest
@ContextConfiguration(classes = CatalogueTestApplication.class)
@Import(CategorieController.class)
@DisplayName("CategorieController — US-027/028/029/030 : /api/v1/categories")
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
        when(claims.get("role", String.class)).thenReturn(role);
        return new TestingAuthenticationToken(
                new StockMasterPrincipal(99L, claims), null, "ROLE_" + role);
    }

    private CategorieResponse categorieResponse(Long id, String code, String designation,
                                                BigDecimal tauxTva) {
        return CategorieResponse.builder()
                .id(id)
                .code(code)
                .designation(designation)
                .tauxTva(tauxTva)
                .groupId(1L)
                .build();
    }

    private CategorieListResponse listResponse(List<CategorieResponse> content) {
        return CategorieListResponse.builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements(content.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
    }

    // ─── création (US-027) ──────────────────────────────────────────────────

    @Nested
    @DisplayName("US-027 — Cas nominal création")
    class CreerCasNominal {

        @Test
        @DisplayName("✅ 201 — catégorie créée, enveloppe ApiResponse")
        void shouldCreerAvec201() throws Exception {
            when(categorieService.creer(any(StockMasterPrincipal.class), any()))
                    .thenReturn(categorieResponse(300L, "ALIM", "Alimentation générale",
                            new BigDecimal("19.25")));

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

    // ─── liste paginée + search (US-028) ───────────────────────────────────

    @Nested
    @DisplayName("US-028 — GET /api/v1/categories : liste paginée + search")
    class Lister {

        @Test
        @DisplayName("✅ 200 — liste paginée de catégories du groupe")
        void shouldListerAvec200() throws Exception {
            when(categorieService.lister(any(), eq(0), eq(20), eq("designation"),
                    eq("ASC"), any()))
                    .thenReturn(listResponse(List.of(
                            categorieResponse(10L, "ALIM", "Alimentation générale",
                                    new BigDecimal("19.25")),
                            categorieResponse(11L, "HYG", "Hygiène", new BigDecimal("19.25")))));

            mockMvc.perform(get("/api/v1/categories")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                            .param("page", "0")
                            .param("size", "20")
                            .param("sort", "designation")
                            .param("direction", "ASC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.content[0].code").value("ALIM"))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(true));
        }

        @Test
        @DisplayName("✅ 200 — recherche partielle sur la désignation (?designation=Alim)")
        void shouldRechercherParDesignation() throws Exception {
            when(categorieService.lister(any(), eq(0), eq(20), eq("designation"),
                    eq("ASC"), eq("Alim")))
                    .thenReturn(listResponse(List.of(
                            categorieResponse(10L, "ALIM", "Alimentation générale",
                                    new BigDecimal("19.25")))));

            mockMvc.perform(get("/api/v1/categories")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                            .param("page", "0")
                            .param("size", "20")
                            .param("sort", "designation")
                            .param("direction", "ASC")
                            .param("designation", "Alim"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].code").value("ALIM"));
        }

        @Test
        @DisplayName("✅ 200 — tri DESC sur designation")
        void shouldTrierDESC() throws Exception {
            when(categorieService.lister(any(), eq(0), eq(10), eq("designation"),
                    eq("DESC"), any()))
                    .thenReturn(listResponse(List.of(
                            categorieResponse(10L, "ALIM", "Alimentation générale",
                                    new BigDecimal("19.25")))));

            mockMvc.perform(get("/api/v1/categories")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "designation")
                            .param("direction", "DESC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].code").value("ALIM"));
        }

        @Test
        @DisplayName("✅ 200 — taille de page au-delà de la taille demandée")
        void shouldRevenirTaillePositive() throws Exception {
            when(categorieService.lister(any(), eq(0), eq(1), eq("designation"),
                    eq("ASC"), any()))
                    .thenReturn(listResponse(List.of(
                            categorieResponse(10L, "ALIM", "Alimentation générale",
                                    new BigDecimal("19.25")))));

            mockMvc.perform(get("/api/v1/categories")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                            .param("page", "0")
                            .param("size", "1")
                            .param("sort", "designation")
                            .param("direction", "ASC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.size").value(1));
        }

        @Nested
        @DisplayName("RBAC liste")
        class Rbac {

            @Test
            @DisplayName("❌ 403 — CAISSIER refusé (liste)")
            void shouldRefuserCaissier() throws Exception {            when(categorieService.lister(any(), eq(0), eq(20), eq("designation"), eq("ASC"), any()))
            .thenThrow(new BusinessException(ErrorCode.SEC_ACCESS_DENIED));

                mockMvc.perform(get("/api/v1/categories")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                asAuthentication("CAISSIER", 1L, 10L)))
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "designation")
                        .param("direction", "ASC"))
                        .andExpect(status().isForbidden());
            }

            @Test
            @DisplayName("❌ 403 — anonyme refusé (liste)")
            void shouldRejeterAnonyme() throws Exception {
                mockMvc.perform(get("/api/v1/categories")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "designation")
                        .param("direction", "ASC"))
                        .andExpect(status().isForbidden());
            }
        }

        @Nested
        @DisplayName("Erreurs métier propagées (liste)")
        class ErreursMetier {

            @Test
            @DisplayName("❌ 404 — ressource non trouvée (si service retourne RES_001)")
            void shouldReturn404FromService() throws Exception {
                when(categorieService.lister(any(), anyInt(), anyInt(), any(), any(), any()))
                .thenThrow(new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

            mockMvc.perform(get("/api/v1/categories")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                            .param("page", "0")
                            .param("size", "20")
                            .param("sort", "designation")
                            .param("direction", "ASC"))
                    .andExpect(status().isNotFound());
            }
        }
    }

    // ─── modification (US-029) ─────────────────────────────────────────────

    @Nested
    @DisplayName("US-029 — PUT /api/v1/categories/{id} : modification (PATCH)")
    class Modifier {

        @Test
        @DisplayName("✅ 200 — désignation et taux TVA mis à jour")
        void shouldModifierAvec200() throws Exception {
            var body = new CategorieUpdateRequest("Alimentation mise à jour",
                    new BigDecimal("19.25"));
            when(categorieService.modifier(any(StockMasterPrincipal.class), eq(42L), any()))
            .thenReturn(categorieResponse(42L, "ALIM", "Alimentation mise à jour",
                    new BigDecimal("19.25")));

            mockMvc.perform(put("/api/v1/categories/42")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"designation\":\"Alimentation mise à jour\",\"tauxTva\":19.25}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(42))
                    .andExpect(jsonPath("$.data.code").value("ALIM"))
                    .andExpect(jsonPath("$.data.designation").value("Alimentation mise à jour"))
                    .andExpect(jsonPath("$.data.tauxTva").value(19.25));
        }

        @Test
        @DisplayName("✅ 200 — taux TVA réduit configuré (6,6 %) accepté par service")
        void shouldModifierAvecTauxReduit() throws Exception {
            var body = new CategorieUpdateRequest("Alimentation mise à jour",
                    new BigDecimal("6.60"));
            when(categorieService.modifier(any(StockMasterPrincipal.class), eq(42L), any()))
            .thenReturn(categorieResponse(42L, "ALIM", "Alimentation mise à jour",
                    new BigDecimal("6.60")));

            mockMvc.perform(put("/api/v1/categories/42")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"designation\":\"Alimentation mise à jour\",\"tauxTva\":6.60}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.tauxTva").value(6.60));
        }

        @Test
        @DisplayName("❌ 404 — catégorie inconnue")
        void shouldReturn404() throws Exception {                when(categorieService.modifier(any(StockMasterPrincipal.class), eq(999L), any()))
                .thenThrow(new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

            mockMvc.perform(put("/api/v1/categories/999")
                    .with(SecurityMockMvcRequestPostProcessors.authentication(
                            asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"designation\":\"Alimentation mise à jour\",\"tauxTva\":19.25}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ 409 — catégorie avec articles attachés (DEC-028 via RES_005)")
        void shouldReturn409Dec028() throws Exception {                when(categorieService.modifier(any(StockMasterPrincipal.class), eq(42L), any()))
                .thenThrow(new BusinessException(ErrorCode.RES_ENTITY_HAS_DEPENDENCIES));

            mockMvc.perform(put("/api/v1/categories/42")
                    .with(SecurityMockMvcRequestPostProcessors.authentication(
                            asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"designation\":\"Alimentation mise à jour\",\"tauxTva\":19.25}"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("❌ 400 — validation Jakarta (designation absente)")
        void shouldReturn400Validation() throws Exception {
            mockMvc.perform(put("/api/v1/categories/42")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"tauxTva\":19.25}"))
                    .andExpect(status().isBadRequest());
        }

        @Nested
        @DisplayName("RBAC modification")
        class Rbac {

            @Test
            @DisplayName("❌ 403 — CAISSIER refusé (modification)")
            void shouldRefuserCaissier() throws Exception {
                var body = new CategorieUpdateRequest("Alimentation mise à jour",
                        new BigDecimal("19.25"));                when(categorieService.modifier(any(StockMasterPrincipal.class), eq(42L), any()))
                .thenThrow(new BusinessException(ErrorCode.SEC_ACCESS_DENIED));

            mockMvc.perform(put("/api/v1/categories/42")
                    .with(SecurityMockMvcRequestPostProcessors.authentication(
                            asAuthentication("CAISSIER", 1L, 10L)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"designation\":\"Alimentation mise à jour\",\"tauxTva\":19.25}"))
                    .andExpect(status().isForbidden());
            }

            @Test
            @DisplayName("❌ 403 — anonyme refusé (modification)")
            void shouldRejeterAnonyme() throws Exception {
                mockMvc.perform(put("/api/v1/categories/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"designation\":\"Alimentation mise à jour\",\"tauxTva\":19.25}"))
                        .andExpect(status().isForbidden());
            }
        }
    }

    // ─── suppression logique (US-030 / DEC-028) ───────────────────────────

    @Nested
    @DisplayName("US-030 — DELETE /api/v1/categories/{id} : suppression logique")
    class Supprimer {

        @Test
        @DisplayName("✅ 200 — suppression logique réussie (aucun article attaché)")
        void shouldSupprimerAvec200() throws Exception {
            when(categorieService.supprimer(any(StockMasterPrincipal.class), eq(42L)))
            .thenReturn(categorieResponse(42L, "ALIM", "Alimentation générale",
                    new BigDecimal("19.25")));

            mockMvc.perform(delete("/api/v1/categories/42")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(42))
                    .andExpect(jsonPath("$.data.code").value("ALIM"))
                    .andExpect(jsonPath("$.data.supprime").doesNotExist());
        }

        @Test
        @DisplayName("❌ 404 — catégorie inconnue")
        void shouldReturn404() throws Exception {
            when(categorieService.supprimer(any(StockMasterPrincipal.class), eq(999L)))
            .thenThrow(new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

        mockMvc.perform(delete("/api/v1/categories/999")
                .with(SecurityMockMvcRequestPostProcessors.authentication(
                        asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L))))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ 409 — articles attachés (DEC-028 via RES_005)")
        void shouldReturn409Dec028() throws Exception {            when(categorieService.supprimer(any(StockMasterPrincipal.class), eq(42L)))
            .thenThrow(new BusinessException(ErrorCode.RES_ENTITY_HAS_DEPENDENCIES));

        mockMvc.perform(delete("/api/v1/categories/42")
                .with(SecurityMockMvcRequestPostProcessors.authentication(
                        asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L))))
                .andExpect(status().isConflict());
        }

        @Nested
        @DisplayName("RBAC suppression")
        class Rbac {

            @Test
            @DisplayName("❌ 403 — CAISSIER refusé (suppression)")
            void shouldRefuserCaissier() throws Exception {
                when(categorieService.supprimer(any(StockMasterPrincipal.class), eq(42L)))
            .thenThrow(new BusinessException(ErrorCode.SEC_ACCESS_DENIED));

            mockMvc.perform(delete("/api/v1/categories/42")
                    .with(SecurityMockMvcRequestPostProcessors.authentication(
                            asAuthentication("CAISSIER", 1L, 10L))))
                    .andExpect(status().isForbidden());
            }

            @Test
            @DisplayName("❌ 403 — anonyme refusé (suppression)")
            void shouldRejeterAnonyme() throws Exception {
                mockMvc.perform(delete("/api/v1/categories/42"))
                        .andExpect(status().isForbidden());
            }
        }
    }
}
