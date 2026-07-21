package com.stockmaster.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmaster.auth.AuthTestApplication;
import com.stockmaster.auth.dto.request.InscriptionEntrepriseUniqueRequest;
import com.stockmaster.auth.dto.request.ForgotPasswordRequest;
import com.stockmaster.auth.dto.request.InscriptionGroupeRequest;
import com.stockmaster.auth.dto.request.LoginRequest;
import com.stockmaster.auth.dto.request.RefreshTokenRequest;
import com.stockmaster.auth.dto.request.ResetPasswordRequest;
import com.stockmaster.auth.dto.request.ChangePasswordRequest;
import com.stockmaster.auth.dto.response.InscriptionResponse;
import com.stockmaster.auth.dto.response.LoginResponse;
import com.stockmaster.auth.dto.response.RefreshTokenResponse;
import com.stockmaster.auth.service.AuthService;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ContextConfiguration(classes = AuthTestApplication.class)
@DisplayName("AuthController — Tests d'intégration")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private InscriptionEntrepriseUniqueRequest inscriptionUniqueRequest;
    private InscriptionGroupeRequest inscriptionGroupeRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        inscriptionUniqueRequest = InscriptionEntrepriseUniqueRequest.builder()
                .nomBoutique("Épicerie Centrale")
                .ville("Douala")
                .quartier("Akwa")
                .prenom("Jean")
                .nom("Kamga")
                .email("jean.kamga@epicerie.cm")
                .motDePasse("MotDePasse@2026")
                .build();

        inscriptionGroupeRequest = InscriptionGroupeRequest.builder()
                .nomGroupe("Distribo Sarl")
                .villesiege("Yaoundé")
                .nif("M123456789")
                .telephone("699000001")
                .emailEntreprise("contact@distribo.cm")
                .prenom("Paul")
                .nom("Biya Jr")
                .emailAdmin("paul@distribo.cm")
                .motDePasse("MotDePasse@2026")
                .build();

        loginRequest = LoginRequest.builder()
                .email("jean.kamga@epicerie.cm")
                .motDePasse("MotDePasse@2026")
                .build();
    }

    // ========================================================================
    // US-006 — POST /api/v1/auth/inscription/entreprise-unique
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/inscription/entreprise-unique")
    class InscriptionEntrepriseUniqueEndpoint {

        @Test
        @DisplayName("201 CREATED — inscription valide")
        void shouldReturn201WhenInscriptionValid() throws Exception {
            InscriptionResponse response = InscriptionResponse.builder()
                    .email("jean.kamga@epicerie.cm")
                    .groupId(1L)
                    .message("Votre espace a été créé. Vérifiez votre email pour activer votre compte.")
                    .build();

            when(authService.inscrireEntrepriseUnique(any(InscriptionEntrepriseUniqueRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/inscription/entreprise-unique")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inscriptionUniqueRequest))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("jean.kamga@epicerie.cm"))
                    .andExpect(jsonPath("$.data.groupId").value(1))
                    .andExpect(jsonPath("$.message").value("Votre espace a été créé. Vérifiez votre email pour activer votre compte."));
        }

        @Test
        @DisplayName("400 BAD REQUEST — nomBoutique vide")
        void shouldReturn400WhenNomBoutiqueIsBlank() throws Exception {
            inscriptionUniqueRequest.setNomBoutique("");

            mockMvc.perform(post("/api/v1/auth/inscription/entreprise-unique")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inscriptionUniqueRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST — email invalide")
        void shouldReturn400WhenEmailInvalid() throws Exception {
            inscriptionUniqueRequest.setEmail("pas-un-email");

            mockMvc.perform(post("/api/v1/auth/inscription/entreprise-unique")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inscriptionUniqueRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST — mot de passe trop court")
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            inscriptionUniqueRequest.setMotDePasse("Ab1!");

            mockMvc.perform(post("/api/v1/auth/inscription/entreprise-unique")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inscriptionUniqueRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("409 CONFLICT — email déjà existant")
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {
            when(authService.inscrireEntrepriseUnique(any(InscriptionEntrepriseUniqueRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS));

            mockMvc.perform(post("/api/v1/auth/inscription/entreprise-unique")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inscriptionUniqueRequest))
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_008"));
        }

        @Test
        @DisplayName("400 BAD REQUEST — corps vide")
        void shouldReturn400WhenEmptyBody() throws Exception {
            mockMvc.perform(post("/api/v1/auth/inscription/entreprise-unique")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========================================================================
    // US-007 — POST /api/v1/auth/inscription/groupe
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/inscription/groupe")
    class InscriptionGroupeEndpoint {

        @Test
        @DisplayName("201 CREATED — inscription groupe valide avec NIF, téléphone")
        void shouldReturn201WhenInscriptionGroupeValid() throws Exception {
            InscriptionResponse response = InscriptionResponse.builder()
                    .email("paul@distribo.cm")
                    .groupId(2L)
                    .message("Votre groupe a été créé. Créez votre première filiale depuis le tableau de bord.")
                    .build();

            when(authService.inscrireGroupe(any(InscriptionGroupeRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/inscription/groupe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inscriptionGroupeRequest))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("paul@distribo.cm"))
                    .andExpect(jsonPath("$.data.groupId").value(2))
                    .andExpect(jsonPath("$.message").value("Votre groupe a été créé. Créez votre première filiale depuis le tableau de bord."));
        }

        @Test
        @DisplayName("400 BAD REQUEST — nomGroupe vide")
        void shouldReturn400WhenNomGroupeIsBlank() throws Exception {
            inscriptionGroupeRequest.setNomGroupe("");

            mockMvc.perform(post("/api/v1/auth/inscription/groupe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inscriptionGroupeRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST — emailAdmin invalide")
        void shouldReturn400WhenEmailAdminInvalid() throws Exception {
            inscriptionGroupeRequest.setEmailAdmin("pas-un-email");

            mockMvc.perform(post("/api/v1/auth/inscription/groupe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inscriptionGroupeRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST — mot de passe trop court")
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            inscriptionGroupeRequest.setMotDePasse("Ab1!");

            mockMvc.perform(post("/api/v1/auth/inscription/groupe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inscriptionGroupeRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("409 CONFLICT — emailAdmin déjà existant")
        void shouldReturn409WhenEmailAdminAlreadyExists() throws Exception {
            when(authService.inscrireGroupe(any(InscriptionGroupeRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS));

            mockMvc.perform(post("/api/v1/auth/inscription/groupe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inscriptionGroupeRequest))
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_008"));
        }

        @Test
        @DisplayName("400 BAD REQUEST — corps vide")
        void shouldReturn400WhenEmptyBody() throws Exception {
            mockMvc.perform(post("/api/v1/auth/inscription/groupe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========================================================================
    // US-008 — POST /api/v1/auth/login
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("200 OK — credentials valides")
        void shouldReturn200WhenLoginValid() throws Exception {
            LoginResponse response = LoginResponse.builder()
                    .accessToken("access-token")
                    .refreshToken("refresh-token")
                    .expiresIn(900)
                    .role("ADMIN_GROUPE")
                    .scope("GROUPE")
                    .build();

            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.data.role").value("ADMIN_GROUPE"));
        }

        @Test
        @DisplayName("400 BAD REQUEST — email vide")
        void shouldReturn400WhenEmailIsBlank() throws Exception {
            loginRequest.setEmail("");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST — mot de passe vide")
        void shouldReturn400WhenPasswordIsBlank() throws Exception {
            loginRequest.setMotDePasse("");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("401 UNAUTHORIZED — credentials invalides")
        void shouldReturn401WhenInvalidCredentials() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_001"));
        }

        @Test
        @DisplayName("403 FORBIDDEN — compte désactivé")
        void shouldReturn403WhenAccountDisabled() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
        }

        @Test
        @DisplayName("403 FORBIDDEN — groupe suspendu")
        void shouldReturn403WhenTenantSuspended() throws Exception {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_TENANT_SUSPENDED));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_003"));
        }
    }

    // ========================================================================
    // US-009 — POST /api/v1/auth/refresh
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class RefreshTokenEndpoint {

        private RefreshTokenRequest refreshTokenRequest;

        @BeforeEach
        void setUp() {
            refreshTokenRequest = RefreshTokenRequest.builder()
                    .refreshToken("valid-refresh-token")
                    .build();
        }

        @Test
        @DisplayName("200 OK — refresh token valide")
        void shouldReturn200WhenRefreshTokenValid() throws Exception {
            RefreshTokenResponse response = RefreshTokenResponse.builder()
                    .accessToken("new-access-token")
                    .expiresIn(900)
                    .build();

            when(authService.refreshAccessToken(any(RefreshTokenRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshTokenRequest))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.data.expiresIn").value(900));
        }

        @Test
        @DisplayName("400 BAD REQUEST — refresh token vide")
        void shouldReturn400WhenRefreshTokenIsBlank() throws Exception {
            refreshTokenRequest.setRefreshToken("");

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshTokenRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("401 UNAUTHORIZED — refresh token invalide")
        void shouldReturn401WhenRefreshTokenInvalid() throws Exception {
            when(authService.refreshAccessToken(any(RefreshTokenRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshTokenRequest))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_001"));
        }

        @Test
        @DisplayName("403 FORBIDDEN — compte désactivé")
        void shouldReturn403WhenAccountDisabled() throws Exception {
            when(authService.refreshAccessToken(any(RefreshTokenRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshTokenRequest))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
        }

        @Test
        @DisplayName("400 BAD REQUEST — corps vide")
        void shouldReturn400WhenEmptyBody() throws Exception {
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========================================================================
    // US-010 — POST /api/v1/auth/logout
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    class LogoutEndpoint {

        @Test
        @DisplayName("200 OK — déconnexion réussie")
        void shouldReturn200WhenLogoutSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(user("1").roles("ADMIN_GROUPE"))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Déconnexion réussie"));

            verify(authService).logout();
        }

        @Test
        @DisplayName("401 UNAUTHORIZED — token invalide (simulé par le service)")
        void shouldReturn401WhenTokenInvalid() throws Exception {
            doThrow(new BusinessException(ErrorCode.AUTH_TOKEN_INVALID))
                    .when(authService).logout();

            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(user("1").roles("ADMIN_GROUPE"))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_005"));
        }
    }

    // ========================================================================
    // US-011 — POST /api/v1/auth/forgot-password
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/forgot-password")
    class ForgotPasswordEndpoint {

        @Test
        @DisplayName("200 OK — email valide (existant ou non)")
        void shouldReturn200WhenEmailValid() throws Exception {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("jean.kamga@epicerie.cm")
                    .build();

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Si cet email existe, un lien de réinitialisation vous a été envoyé."));

            verify(authService).forgotPassword(any(ForgotPasswordRequest.class));
        }

        @Test
        @DisplayName("400 BAD REQUEST — email vide")
        void shouldReturn400WhenEmailBlank() throws Exception {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .email("")
                    .build();

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========================================================================
    // US-012 — POST /api/v1/auth/reset-password
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/reset-password")
    class ResetPasswordEndpoint {

        @Test
        @DisplayName("200 OK — token valide et nouveau mot de passe")
        void shouldReturn200WhenResetPasswordValid() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-reset-token")
                    .nouveauMotDePasse("NewPass@2026")
                    .build();

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Mot de passe réinitialisé avec succès."));

            verify(authService).resetPassword(any(ResetPasswordRequest.class));
        }

        @Test
        @DisplayName("400 BAD REQUEST — token vide")
        void shouldReturn400WhenTokenBlank() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("")
                    .nouveauMotDePasse("NewPass@2026")
                    .build();

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST — mot de passe faible (pas de chiffre)")
        void shouldReturn400WhenPasswordWeak() throws Exception {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .token("valid-reset-token")
                    .nouveauMotDePasse("WeakPass!")
                    .build();

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========================================================================
    // US-013 — PUT /api/v1/auth/change-password
    // ========================================================================

    @Nested
    @DisplayName("PUT /api/v1/auth/change-password")
    class ChangePasswordEndpoint {

        @Test
        @DisplayName("200 OK — changement de mot de passe réussi")
        void shouldReturn200WhenChangePasswordValid() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .ancienMotDePasse("MotDePasse@2026")
                    .nouveauMotDePasse("NewPass@2026")
                    .build();

            mockMvc.perform(put("/api/v1/auth/change-password")
                            .with(user("1").roles("ADMIN_GROUPE"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Mot de passe modifié avec succès."));

            verify(authService).changePassword(any(ChangePasswordRequest.class));
        }

        @Test
        @DisplayName("400 BAD REQUEST — ancien mot de passe incorrect (service exception)")
        void shouldReturn400WhenOldPasswordIncorrect() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .ancienMotDePasse("WrongPass@2026")
                    .nouveauMotDePasse("NewPass@2026")
                    .build();

            doThrow(new BusinessException(ErrorCode.SEC_INVALID_PASSWORD))
                    .when(authService).changePassword(any(ChangePasswordRequest.class));

            mockMvc.perform(put("/api/v1/auth/change-password")
                            .with(user("1").roles("ADMIN_GROUPE"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SEC_002"));
        }

        @Test
        @DisplayName("400 BAD REQUEST — ancien mot de passe vide")
        void shouldReturn400WhenOldPasswordBlank() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .ancienMotDePasse("")
                    .nouveauMotDePasse("NewPass@2026")
                    .build();

            mockMvc.perform(put("/api/v1/auth/change-password")
                            .with(user("1").roles("ADMIN_GROUPE"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 BAD REQUEST — nouveau mot de passe faible")
        void shouldReturn400WhenNewPasswordWeak() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .ancienMotDePasse("MotDePasse@2026")
                    .nouveauMotDePasse("weak")
                    .build();

            mockMvc.perform(put("/api/v1/auth/change-password")
                            .with(user("1").roles("ADMIN_GROUPE"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("401 UNAUTHORIZED — token invalide (simulé par le service)")
        void shouldReturn401WhenTokenInvalid() throws Exception {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .ancienMotDePasse("MotDePasse@2026")
                    .nouveauMotDePasse("NewPass@2026")
                    .build();

            doThrow(new BusinessException(ErrorCode.AUTH_TOKEN_INVALID))
                    .when(authService).changePassword(any(ChangePasswordRequest.class));

            mockMvc.perform(put("/api/v1/auth/change-password")
                            .with(user("1").roles("ADMIN_GROUPE"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_005"));
        }
    }
}
