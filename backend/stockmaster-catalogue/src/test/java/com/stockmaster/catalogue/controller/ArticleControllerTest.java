package com.stockmaster.catalogue.controller;

import com.stockmaster.catalogue.CatalogueTestApplication;
import com.stockmaster.catalogue.dto.response.ArticleResponse;
import com.stockmaster.catalogue.service.ArticleService;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrôleur catalogue — US-031 « Créer un article ».
 *
 * <p>RBAC porté par {@code @PreAuthorize} (GESTIONNAIRE_STOCK / ADMIN_FILIALE /
 * ADMIN_GROUPE), service mocké pour isoler le contrôleur — même pattern que
 * {@code CategorieControllerTest}.</p>
 */
@WebMvcTest
@ContextConfiguration(classes = CatalogueTestApplication.class)
@Import(ArticleController.class)
@DisplayName("ArticleController — US-031 : POST /api/v1/articles")
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    private static final String CORPS_VALIDE = """
            {
              "codeArticle": "RIZ50KG",
              "designation": "Riz parfumé 50 kg",
              "categorieId": 3,
              "prixAchatHt": 15000,
              "prixVenteHt": 18500,
              "tauxTva": 19.25,
              "seuilAlerte": 10
            }
            """;

    private ArticleResponse reponseAttendue() {
        return ArticleResponse.builder()
                .id(7L)
                .codeArticle("RIZ50KG")
                .designation("Riz parfumé 50 kg")
                .categorieId(3L)
                .prixAchatHt(15000)
                .prixVenteHt(18500)
                .tauxTva(new BigDecimal("19.25"))
                .prixVenteTtc(22061)
                .margeBrutePct(new BigDecimal("23.33"))
                .seuilAlerte(new BigDecimal("10"))
                .actif(true)
                .groupId(1L)
                .build();
    }

    @Nested
    @DisplayName("✅ Cas nominaux")
    class Nominale {

        @Test
        @DisplayName("✅ 201 avec rôle GESTIONNAIRE_STOCK — enveloppe ApiResponse<T>")
        void shouldReturn201WithRoleGestionnaireStock() throws Exception {
            when(articleService.creer(any(), any())).thenReturn(reponseAttendue());

            mockMvc.perform(post("/api/v1/articles")
                            .with(user("u").roles("GESTIONNAIRE_STOCK"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPS_VALIDE))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(7))
                    .andExpect(jsonPath("$.data.codeArticle").value("RIZ50KG"))
                    .andExpect(jsonPath("$.data.prixVenteTtc").value(22061));
        }

        @Test
        @DisplayName("✅ 201 avec rôle ADMIN_GROUPE")
        void shouldReturn201WithRoleAdminGroupe() throws Exception {
            when(articleService.creer(any(), any())).thenReturn(reponseAttendue());

            mockMvc.perform(post("/api/v1/articles")
                            .with(user("u").roles("ADMIN_GROUPE"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPS_VALIDE))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("❌ Cas d'erreur")
    class Erreurs {

        @Test
        @DisplayName("❌ 403 si rôle CAISSIER — refus avant le service")
        void shouldReturn403WhenRoleCaissier() throws Exception {
            mockMvc.perform(post("/api/v1/articles")
                            .with(user("u").roles("CAISSIER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPS_VALIDE))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(articleService);
        }

        @Test
        @DisplayName("❌ 400 si corps invalide (champs obligatoires manquants)")
        void shouldReturn400WhenBodyInvalid() throws Exception {
            mockMvc.perform(post("/api/v1/articles")
                            .with(user("u").roles("GESTIONNAIRE_STOCK"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(articleService);
        }

        @Test
        @DisplayName("❌ 409 si code article dupliqué (RES_002)")
        void shouldReturn409WhenServiceThrowsDuplicate() throws Exception {
            when(articleService.creer(any(), any()))
                    .thenThrow(new BusinessException(ErrorCode.RES_DUPLICATE_CODE));

            mockMvc.perform(post("/api/v1/articles")
                            .with(user("u").roles("GESTIONNAIRE_STOCK"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPS_VALIDE))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("❌ 404 si catégorie inconnue (RES_001)")
        void shouldReturn404WhenCategorieUnknown() throws Exception {
            when(articleService.creer(any(), any()))
                    .thenThrow(new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

            mockMvc.perform(post("/api/v1/articles")
                            .with(user("u").roles("GESTIONNAIRE_STOCK"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPS_VALIDE))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ 400 si taux TVA non supporté (CAT_001)")
        void shouldReturn400WhenTauxInvalide() throws Exception {
            when(articleService.creer(any(), any()))
                    .thenThrow(new BusinessException(ErrorCode.CAT_TAUX_TVA_INVALIDE));

            mockMvc.perform(post("/api/v1/articles")
                            .with(user("u").roles("GESTIONNAIRE_STOCK"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPS_VALIDE))
                    .andExpect(status().isBadRequest());
        }
    }
}
