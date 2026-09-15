package com.stockmaster.utilisateur.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.UtilisateurTestApplication;
import com.stockmaster.utilisateur.dto.request.AdminFilialeCreateRequest;
import com.stockmaster.utilisateur.dto.response.UtilisateurAdminResponse;
import com.stockmaster.utilisateur.service.AdminFilialeService;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-021 — POST /api/v1/utilisateurs/admin-filiale : tests contrôleur.
 */
@WebMvcTest
@ContextConfiguration(classes = UtilisateurTestApplication.class)
@Import(UtilisateurAdminController.class)
@DisplayName("UtilisateurAdminController — US-021")
class UtilisateurAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminFilialeService adminFilialeService;

    private TestingAuthenticationToken asAuthentication(String role, Long groupId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        return new TestingAuthenticationToken(
                new StockMasterPrincipal(9L, claims), null, "ROLE_" + role);
    }

    private String corpsValide() {
        return """
                {
                  "filialeId": 5,
                  "prenom": "Marie",
                  "nom": "Ngono",
                  "email": "marie.ngono@distribo.cm"
                }
                """;
    }

    private UtilisateurAdminResponse responseStub() {
        return UtilisateurAdminResponse.builder()
                .id(12L)
                .email("marie.ngono@distribo.cm")
                .prenom("Marie")
                .nom("Ngono")
                .role(RoleUtilisateur.ADMIN_FILIALE)
                .actif(false)
                .filialeId(5L)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/utilisateurs/admin-filiale")
    class Creer {

        @Test
        @DisplayName("✅ ADMIN_GROUPE → 201 + enveloppe ApiResponse")
        void shouldCreateWith201() throws Exception {
            when(adminFilialeService.creer(any(StockMasterPrincipal.class), any(AdminFilialeCreateRequest.class)))
                    .thenReturn(responseStub());

            mockMvc.perform(post("/api/v1/utilisateurs/admin-filiale")
                            .contentType("application/json")
                            .content(corpsValide())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("ADMIN_GROUPE", 1L))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(12))
                    .andExpect(jsonPath("$.data.role").value("ADMIN_FILIALE"))
                    .andExpect(jsonPath("$.data.actif").value(false))
                    .andExpect(jsonPath("$.data.filialeId").value(5));
        }

        @Test
        @DisplayName("❌ rôle non autorisé (CAISSIER) → 403 via @PreAuthorize")
        void shouldRejectWrongRole() throws Exception {
            mockMvc.perform(post("/api/v1/utilisateurs/admin-filiale")
                            .contentType("application/json")
                            .content(corpsValide())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("CAISSIER", 1L))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ email déjà pris → 409 AUTH_008 via GlobalExceptionHandler")
        void shouldReturn409OnDuplicateEmail() throws Exception {
            when(adminFilialeService.creer(any(StockMasterPrincipal.class), any(AdminFilialeCreateRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS));

            mockMvc.perform(post("/api/v1/utilisateurs/admin-filiale")
                            .contentType("application/json")
                            .content(corpsValide())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("ADMIN_GROUPE", 1L))))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_008"));
        }

        @Test
        @DisplayName("❌ limite plan atteinte → 403 USR_001")
        void shouldReturn403OnLimitReached() throws Exception {
            when(adminFilialeService.creer(any(StockMasterPrincipal.class), any(AdminFilialeCreateRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.USR_USER_LIMIT_REACHED));

            mockMvc.perform(post("/api/v1/utilisateurs/admin-filiale")
                            .contentType("application/json")
                            .content(corpsValide())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("ADMIN_GROUPE", 1L))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("USR_001"));
        }

        @Test
        @DisplayName("❌ validation Jakarta (email invalide) → 400")
        void shouldReturn400OnInvalidBody() throws Exception {
            String corpsInvalide = """
                    {
                      "filialeId": 5,
                      "prenom": "Marie",
                      "nom": "Ngono",
                      "email": "pas-un-email"
                    }
                    """;

            mockMvc.perform(post("/api/v1/utilisateurs/admin-filiale")
                            .contentType("application/json")
                            .content(corpsInvalide)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("ADMIN_GROUPE", 1L))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ non authentifié → 401/403")
        void shouldRejectAnonymous() throws Exception {
            mockMvc.perform(post("/api/v1/utilisateurs/admin-filiale")
                            .contentType("application/json")
                            .content(corpsValide()))
                    .andExpect(status().is4xxClientError());
        }
    }
}
