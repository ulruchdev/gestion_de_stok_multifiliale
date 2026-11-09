package com.stockmaster.stock.controller;

import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.stock.StockTestApplication;
import com.stockmaster.stock.domain.enums.StatutAlerte;
import com.stockmaster.stock.domain.enums.TypeMouvement;
import com.stockmaster.stock.dto.response.MouvementStockResponse;
import com.stockmaster.stock.dto.response.StockArticleResponse;
import com.stockmaster.stock.service.StockService;
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
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrôleur stock — US-051 (consultation) et US-052 (historique des mouvements).
 *
 * <ul>
 *   <li>US-051 — GET /api/v1/stock : liste paginée, aucun rôle restreint
 *       (« tout utilisateur authentifié » — critère du backlog)</li>
 *   <li>US-052 — GET /api/v1/stock/articles/{id}/mouvements : RBAC
 *       GESTIONNAIRE_STOCK / ADMIN_FILIALE / ADMIN_GROUPE via {@code @PreAuthorize}</li>
 * </ul>
 *
 * <p>Contenu mocké au niveau service pour isoler le contrôleur (pattern
 * {@code CategorieControllerTest}).</p>
 */
@WebMvcTest
@ContextConfiguration(classes = StockTestApplication.class)
@Import(StockController.class)
@DisplayName("StockController — US-051/US-052 : /api/v1/stock")
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockService stockService;

    // ─── helpers ────────────────────────────────────────────────────────────

    private TestingAuthenticationToken asAuthentication(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        when(claims.get("role", String.class)).thenReturn(role);
        return new TestingAuthenticationToken(
                new StockMasterPrincipal(99L, claims), null, "ROLE_" + role);
    }

    private PageResponse<StockArticleResponse> pageStock(List<StockArticleResponse> content) {
        return PageResponse.<StockArticleResponse>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements(content.size())
                .totalPages(content.isEmpty() ? 0 : 1)
                .build();
    }

    private StockArticleResponse stockArticle(long articleId, String code, String designation,
                                              String stock, String seuil, StatutAlerte statut) {
        return StockArticleResponse.builder()
                .articleId(articleId)
                .codeArticle(code)
                .designation(designation)
                .stockActuel(new BigDecimal(stock))
                .seuilAlerte(new BigDecimal(seuil))
                .statutAlerte(statut)
                .build();
    }

    // ─── US-051 — GET /api/v1/stock ─────────────────────────────────────────

    @Nested
    @DisplayName("US-051 — GET /api/v1/stock")
    class ListerStock {

        @Test
        @DisplayName("✅ 200 — liste paginée dans l'enveloppe ApiResponse ($.data.content)")
        void shouldRetourner200AvecEnveloppe() throws Exception {
            when(stockService.listerStock(any(StockMasterPrincipal.class), any(), anyInt(), anyInt()))
                    .thenReturn(pageStock(List.of(
                            stockArticle(5L, "ART-001", "Riz sac 25kg", "0.000", "10.000",
                                    StatutAlerte.RUPTURE))));

            mockMvc.perform(get("/api/v1/stock")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].articleId").value(5))
                    .andExpect(jsonPath("$.data.content[0].codeArticle").value("ART-001"))
                    .andExpect(jsonPath("$.data.content[0].stockActuel").value(0.0))
                    .andExpect(jsonPath("$.data.content[0].statutAlerte").value("RUPTURE"))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("✅ 200 — filtre ?statutAlerte=BAS transmis au service (avec page/size)")
        void shouldTransmettreFiltreStatut() throws Exception {
            when(stockService.listerStock(any(StockMasterPrincipal.class), eq("BAS"), eq(0), eq(20)))
                    .thenReturn(pageStock(List.of(
                            stockArticle(6L, "ART-002", "Huile 1L", "3.000", "5.000",
                                    StatutAlerte.BAS))));

            mockMvc.perform(get("/api/v1/stock")
                            .param("statutAlerte", "BAS")
                            .param("page", "0")
                            .param("size", "20")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("CAISSIER", 1L, 10L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].statutAlerte").value("BAS"));

            verify(stockService).listerStock(any(StockMasterPrincipal.class), eq("BAS"), eq(0), eq(20));
        }

        @Test
        @DisplayName("✅ 200 — CAISSIER accepté (US-051 : tout utilisateur authentifié)")
        void shouldAccepterCaissier() throws Exception {
            when(stockService.listerStock(any(StockMasterPrincipal.class), any(), anyInt(), anyInt()))
                    .thenReturn(pageStock(List.of()));

            mockMvc.perform(get("/api/v1/stock")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("CAISSIER", 1L, 10L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isEmpty());
        }

        @Test
        @DisplayName("❌ 403 — service refuse (principal sans groupId → GRP_002)")
        void shouldRetourner403() throws Exception {
            when(stockService.listerStock(any(), any(), anyInt(), anyInt()))
                    .thenThrow(new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN));

            mockMvc.perform(get("/api/v1/stock")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("GRP_002"));
        }

        @Test
        @DisplayName("❌ 400 — ?statutAlerte=XYZ invalide → SYS_002")
        void shouldRetourner400() throws Exception {
            when(stockService.listerStock(any(), eq("XYZ"), anyInt(), anyInt()))
                    .thenThrow(new BusinessException(ErrorCode.SYS_BAD_REQUEST));

            mockMvc.perform(get("/api/v1/stock")
                            .param("statutAlerte", "XYZ")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SYS_002"));
        }
    }

    // ─── US-052 — GET /api/v1/stock/articles/{id}/mouvements ────────────────

    @Nested
    @DisplayName("US-052 — GET /api/v1/stock/articles/{articleId}/mouvements")
    class ListerMouvements {

        private PageResponse<MouvementStockResponse> pageMouvements(List<MouvementStockResponse> content) {
            return PageResponse.<MouvementStockResponse>builder()
                    .content(content)
                    .page(0)
                    .size(content.size())
                    .totalElements(content.size())
                    .totalPages(content.isEmpty() ? 0 : 1)
                    .build();
        }

        @Test
        @DisplayName("✅ 200 — GESTIONNAIRE_STOCK consulte l'historique")
        void shouldRetourner200() throws Exception {
            when(stockService.listerMouvements(any(StockMasterPrincipal.class), eq(5L),
                    any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(pageMouvements(List.of(
                            MouvementStockResponse.builder()
                                    .id(111L)
                                    .typeMouvement(TypeMouvement.ENTREE)
                                    .quantite(new BigDecimal("12.000"))
                                    .dateMouvement(Instant.parse("2026-10-01T10:15:00Z"))
                                    .utilisateurId(7L)
                                    .utilisateurNom("Jean Mvogo")
                                    .origineId(42L)
                                    .motif("Réception partielle")
                                    .build())));

            mockMvc.perform(get("/api/v1/stock/articles/5/mouvements")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].id").value(111))
                    .andExpect(jsonPath("$.data.content[0].typeMouvement").value("ENTREE"))
                    .andExpect(jsonPath("$.data.content[0].utilisateurNom").value("Jean Mvogo"))
                    .andExpect(jsonPath("$.data.content[0].motif").value("Réception partielle"));
        }

        @Test
        @DisplayName("✅ 200 — ADMIN_FILIALE autorisé (rôle du trio)")
        void shouldAutoriserAdminFiliale() throws Exception {
            when(stockService.listerMouvements(any(), eq(5L), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(pageMouvements(List.of()));

            mockMvc.perform(get("/api/v1/stock/articles/5/mouvements")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("ADMIN_FILIALE", 1L, 10L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isEmpty());
        }

        @Test
        @DisplayName("❌ 403 — CAISSIER refusé par @PreAuthorize, service jamais appelé")
        void shouldRefuserCaissier() throws Exception {
            mockMvc.perform(get("/api/v1/stock/articles/5/mouvements")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("CAISSIER", 1L, 10L))))
                    .andExpect(status().isForbidden());

            verify(stockService, never()).listerMouvements(any(), any(), any(), any(), any(),
                    anyInt(), anyInt());
        }

        @Test
        @DisplayName("❌ 403 — RESP_ACHATS refusé par @PreAuthorize")
        void shouldRefuserRespAchats() throws Exception {
            mockMvc.perform(get("/api/v1/stock/articles/5/mouvements")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("RESP_ACHATS", 1L, 10L))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ 404 — article hors groupe (RES_001 uniforme)")
        void shouldRetourner404() throws Exception {
            when(stockService.listerMouvements(any(), eq(999L), any(), any(), any(),
                    anyInt(), anyInt()))
                    .thenThrow(new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

            mockMvc.perform(get("/api/v1/stock/articles/999/mouvements")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RES_001"));
        }

        @Test
        @DisplayName("❌ 403 — groupe absent (GRP_002)")
        void shouldRetourner403() throws Exception {
            when(stockService.listerMouvements(any(), eq(5L), any(), any(), any(),
                    anyInt(), anyInt()))
                    .thenThrow(new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN));

            mockMvc.perform(get("/api/v1/stock/articles/5/mouvements")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("GESTIONNAIRE_STOCK", 1L, 10L))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("GRP_002"));
        }
    }
}
