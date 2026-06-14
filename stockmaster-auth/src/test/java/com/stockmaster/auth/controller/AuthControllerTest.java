package com.stockmaster.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmaster.auth.dto.request.InscriptionEntrepriseUniqueRequest;
import com.stockmaster.auth.dto.request.LoginRequest;
import com.stockmaster.auth.dto.response.InscriptionResponse;
import com.stockmaster.auth.dto.response.LoginResponse;
import com.stockmaster.auth.service.AuthService;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.stockmaster.auth.AuthTestApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = AuthTestApplication.class)
@DisplayName("AuthController — Tests web MVC")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private static final String BASE_URL = "/api/v1/auth";

    // ====================================================================
    // US-006 — Inscription entreprise unique
    // ====================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/inscription/entreprise-unique")
    class InscriptionTests {

        private static final String URL = BASE_URL + "/inscription/entreprise-unique";

        private InscriptionEntrepriseUniqueRequest createValidRequest() {
            return InscriptionEntrepriseUniqueRequest.builder()
                    .nomBoutique("Épicerie Centrale")
                    .ville("Douala")
                    .quartier("Akwa")
                    .prenom("Jean")
                    .nom("Kamga")
                    .email("jean.kamga@test.cm")
                    .motDePasse("Test@2026")
                    .build();
        }

        @Test
        @DisplayName("✅ 201 CREATED quand inscription valide")
        void shouldReturn201WhenInscriptionValid() throws Exception {
            // given
            InscriptionEntrepriseUniqueRequest request = createValidRequest();
            InscriptionResponse serviceResponse = InscriptionResponse.builder()
                    .email("jean.kamga@test.cm")
                    .groupId(1L)
                    .message("Votre espace a été créé")
                    .build();

            when(authService.inscrireEntrepriseUnique(any())).thenReturn(serviceResponse);

            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("jean.kamga@test.cm"))
                    .andExpect(jsonPath("$.data.groupId").value(1))
                    .andExpect(jsonPath("$.data.message").value(containsString("créé")));
        }

        @Test
        @DisplayName("❌ 400 BAD REQUEST quand nomBoutique manquant")
        void shouldReturn400WhenNomBoutiqueIsBlank() throws Exception {
            // given
            InscriptionEntrepriseUniqueRequest request = createValidRequest();
            request.setNomBoutique("");

            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ 400 BAD REQUEST quand email invalide")
        void shouldReturn400WhenEmailInvalid() throws Exception {
            // given
            InscriptionEntrepriseUniqueRequest request = createValidRequest();
            request.setEmail("pas-un-email");

            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ 400 BAD REQUEST quand motDePasse trop court")
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            // given
            InscriptionEntrepriseUniqueRequest request = createValidRequest();
            request.setMotDePasse("Ab1!");

            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ 400 BAD REQUEST quand motDePasse sans caractère spécial")
        void shouldReturn400WhenPasswordNoSpecialChar() throws Exception {
            // given
            InscriptionEntrepriseUniqueRequest request = createValidRequest();
            request.setMotDePasse("MotDePasse123");

            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ 409 CONFLICT quand service lève BusinessException EMAIL_ALREADY_EXISTS")
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {
            // given
            InscriptionEntrepriseUniqueRequest request = createValidRequest();
            when(authService.inscrireEntrepriseUnique(any()))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS,
                            "Cet email est déjà utilisé"));

            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_008"));
        }

        @Test
        @DisplayName("❌ 400 BAD REQUEST quand corps vide")
        void shouldReturn400WhenEmptyBody() throws Exception {
            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ====================================================================
    // US-008 — Connexion JWT
    // ====================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginTests {

        private static final String URL = BASE_URL + "/login";

        private LoginRequest createValidLoginRequest() {
            return LoginRequest.builder()
                    .email("jean.kamga@test.cm")
                    .motDePasse("Test@2026")
                    .build();
        }

        @Test
        @DisplayName("✅ 200 OK quand credentials valides")
        void shouldReturn200WhenLoginValid() throws Exception {
            // given
            LoginRequest request = createValidLoginRequest();
            LoginResponse serviceResponse = LoginResponse.builder()
                    .accessToken("eyJ.access.token")
                    .refreshToken("eyJ.refresh.token")
                    .expiresIn(900)
                    .role("ADMIN_GROUPE")
                    .scope("GROUPE")
                    .build();

            when(authService.login(any())).thenReturn(serviceResponse);

            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("eyJ.access.token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("eyJ.refresh.token"))
                    .andExpect(jsonPath("$.data.expiresIn").value(900))
                    .andExpect(jsonPath("$.data.role").value("ADMIN_GROUPE"))
                    .andExpect(jsonPath("$.data.scope").value("GROUPE"));
        }

        @Test
        @DisplayName("❌ 400 BAD REQUEST quand email manquant")
        void shouldReturn400WhenEmailIsBlank() throws Exception {
            // given
            LoginRequest request = createValidLoginRequest();
            request.setEmail("");

            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ 400 BAD REQUEST quand motDePasse manquant")
        void shouldReturn400WhenPasswordIsBlank() throws Exception {
            // given
            LoginRequest request = createValidLoginRequest();
            request.setMotDePasse("");

            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ 401 UNAUTHORIZED quand service lève AUTH_INVALID_CREDENTIALS")
        void shouldReturn401WhenInvalidCredentials() throws Exception {
            // given
            LoginRequest request = createValidLoginRequest();
            when(authService.login(any()))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_001"));
        }

        @Test
        @DisplayName("❌ 403 FORBIDDEN quand service lève AUTH_ACCOUNT_DISABLED")
        void shouldReturn403WhenAccountDisabled() throws Exception {
            // given
            LoginRequest request = createValidLoginRequest();
            when(authService.login(any()))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED));

            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
        }

        @Test
        @DisplayName("❌ 403 FORBIDDEN quand service lève AUTH_TENANT_SUSPENDED")
        void shouldReturn403WhenTenantSuspended() throws Exception {
            // given
            LoginRequest request = createValidLoginRequest();
            when(authService.login(any()))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_TENANT_SUSPENDED));

            // when & then
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_003"));
        }
    }
}
