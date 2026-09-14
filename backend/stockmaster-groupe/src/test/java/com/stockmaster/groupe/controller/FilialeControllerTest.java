package com.stockmaster.groupe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmaster.groupe.FilialeTestApplication;
import com.stockmaster.groupe.dto.request.FilialeCreateRequest;
import com.stockmaster.groupe.dto.response.FilialeResponse;
import com.stockmaster.groupe.service.FilialeService;
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
 * US-016 — POST /api/v1/groupe/filiales : tests contrôleur.
 */
@WebMvcTest(FilialeController.class)
@ContextConfiguration(classes = FilialeTestApplication.class)
@DisplayName("FilialeController")
class FilialeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilialeService filialeService;

    private FilialeResponse response;

    private TestingAuthenticationToken asAuthentication(String role, Long groupId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        return new TestingAuthenticationToken(
                new StockMasterPrincipal(9L, claims), null, "ROLE_" + role);
    }

    private FilialeCreateRequest validRequest() {
        FilialeCreateRequest request = new FilialeCreateRequest();
        request.setNom("Boutique Akwa");
        request.setCodeFiliale("DLA01");
        request.setVille("Douala");
        request.setQuartier("Akwa");
        return request;
    }

    @Nested
    @DisplayName("POST /api/v1/groupe/filiales")
    class Creer {

        @Test
        @DisplayName("✅ ADMIN_GROUPE, payload valide → 201")
        void shouldReturn201WhenAdminGroupeCreatesValidFiliale() throws Exception {
            response = FilialeResponse.builder()
                    .id(42L).nom("Boutique Akwa").codeFiliale("DLA01")
                    .ville("Douala").quartier("Akwa").actif(true).siteOperationnel(true).parentId(10L)
                    .build();
            when(filialeService.creer(any(StockMasterPrincipal.class), any(FilialeCreateRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/groupe/filiales")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(validRequest()))
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("ADMIN_GROUPE", 1L))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(42))
                    .andExpect(jsonPath("$.data.codeFiliale").value("DLA01"))
                    .andExpect(jsonPath("$.data.parentId").value(10));
        }

        @Test
        @DisplayName("❌ CAISSIER → 403 (@PreAuthorize hasRole ADMIN_GROUPE)")
        void shouldReturn403WhenRoleNotAllowed() throws Exception {
            mockMvc.perform(post("/api/v1/groupe/filiales")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(validRequest()))
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("CAISSIER", 1L))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ nom et codeFiliale vides → 400 (validation)")
        void shouldReturn400WhenValidationFails() throws Exception {
            FilialeCreateRequest invalide = new FilialeCreateRequest();

            mockMvc.perform(post("/api/v1/groupe/filiales")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(invalide))
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("ADMIN_GROUPE", 1L))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ Limite de filiales atteinte (service) → 403 via GlobalExceptionHandler")
        void shouldReturn403WhenLimitReached() throws Exception {
            when(filialeService.creer(any(StockMasterPrincipal.class), any(FilialeCreateRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.GRP_FILIALE_LIMIT_REACHED));

            mockMvc.perform(post("/api/v1/groupe/filiales")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(validRequest()))
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("ADMIN_GROUPE", 1L))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("GRP_001"));
        }

        @Test
        @DisplayName("❌ Code filiale déjà utilisé (service) → 409 via GlobalExceptionHandler")
        void shouldReturn409WhenCodeFilialeAlreadyExists() throws Exception {
            when(filialeService.creer(any(StockMasterPrincipal.class), any(FilialeCreateRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.RES_DUPLICATE_FILIALE_CODE));

            mockMvc.perform(post("/api/v1/groupe/filiales")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(validRequest()))
                            .with(SecurityMockMvcRequestPostProcessors.authentication(asAuthentication("ADMIN_GROUPE", 1L))))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("RES_004"));
        }

        @Test
        @DisplayName("❌ Non authentifié → 401/403 (accès refusé)")
        void shouldRejectWhenAnonymous() throws Exception {
            mockMvc.perform(post("/api/v1/groupe/filiales")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().is4xxClientError());
        }
    }
}
