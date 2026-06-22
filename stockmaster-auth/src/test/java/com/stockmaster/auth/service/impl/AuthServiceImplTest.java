package com.stockmaster.auth.service.impl;

import com.stockmaster.auth.config.JwtTokenProvider;
import com.stockmaster.auth.domain.entity.Entreprise;
import com.stockmaster.auth.domain.entity.TenantGroup;
import com.stockmaster.auth.domain.entity.Utilisateur;
import com.stockmaster.auth.domain.enums.PlanAbonnement;
import com.stockmaster.auth.domain.enums.RoleUtilisateur;
import com.stockmaster.auth.domain.enums.ScopeUtilisateur;
import com.stockmaster.auth.domain.enums.TypeEntreprise;
import com.stockmaster.auth.dto.request.InscriptionEntrepriseUniqueRequest;
import com.stockmaster.auth.dto.request.InscriptionGroupeRequest;
import com.stockmaster.auth.dto.request.ForgotPasswordRequest;
import com.stockmaster.auth.dto.request.LoginRequest;
import com.stockmaster.auth.dto.request.RefreshTokenRequest;
import com.stockmaster.auth.dto.request.ResetPasswordRequest;
import com.stockmaster.auth.dto.request.ChangePasswordRequest;
import com.stockmaster.auth.dto.response.InscriptionResponse;
import com.stockmaster.auth.dto.response.LoginResponse;
import com.stockmaster.auth.dto.response.RefreshTokenResponse;
import com.stockmaster.auth.event.InscriptionSuccessEvent;
import com.stockmaster.auth.mapper.AuthMapper;
import com.stockmaster.auth.repository.EntrepriseRepository;
import com.stockmaster.auth.repository.TenantGroupRepository;
import com.stockmaster.auth.repository.UtilisateurRepository;
import com.stockmaster.shared.config.JwtProperties;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.auth.config.StockMasterPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl — Tests unitaires")
class AuthServiceImplTest {

    @Mock private TenantGroupRepository tenantGroupRepository;
    @Mock private EntrepriseRepository entrepriseRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthMapper authMapper;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<InscriptionSuccessEvent> eventCaptor;

    private InscriptionEntrepriseUniqueRequest inscriptionUniqueRequest;
    private InscriptionGroupeRequest inscriptionGroupeRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private ForgotPasswordRequest forgotPasswordRequest;

    private TenantGroup savedGroupe;
    private Entreprise savedEntreprise;
    private Utilisateur savedUtilisateur;

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

        refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        forgotPasswordRequest = ForgotPasswordRequest.builder()
                .email("jean.kamga@epicerie.cm")
                .build();

        savedGroupe = TenantGroup.builder()
                .id(1L)
                .nomGroupe("Épicerie Centrale")
                .planAbonnement(PlanAbonnement.GRATUIT)
                .limiteFiliales(1)
                .actif(true)
                .build();

        savedEntreprise = Entreprise.builder()
                .id(1L)
                .groupe(savedGroupe)
                .typeEntreprise(TypeEntreprise.MERE)
                .nom("Épicerie Centrale")
                .adresseVille("Douala")
                .adresseQuartier("Akwa")
                .adressePays("Cameroun")
                .email("jean.kamga@epicerie.cm")
                .actif(true)
                .build();

        savedUtilisateur = Utilisateur.builder()
                .id(1L)
                .entreprise(savedEntreprise)
                .scope(ScopeUtilisateur.GROUPE)
                .role(RoleUtilisateur.ADMIN_GROUPE)
                .nom("Kamga")
                .prenom("Jean")
                .email("jean.kamga@epicerie.cm")
                .motDePasse("$2a$10$hash")
                .actif(true)
                .build();
    }

    // ========================================================================
    // US-006 — Inscription entreprise unique
    // ========================================================================

    @Nested
    @DisplayName("Inscription entreprise unique (US-006)")
    class InscriptionEntrepriseUnique {

        @Test
        @DisplayName("✅ Crée un groupe, une entreprise et un utilisateur avec tous les champs valides")
        void shouldCreateGroupAndEntrepriseAndUtilisateur() {
            // Arrange
            when(utilisateurRepository.existsByEmail(inscriptionUniqueRequest.getEmail())).thenReturn(false);
            when(tenantGroupRepository.save(any(TenantGroup.class))).thenReturn(savedGroupe);
            when(authMapper.toEntreprise(inscriptionUniqueRequest)).thenReturn(savedEntreprise);
            when(entrepriseRepository.save(any(Entreprise.class))).thenReturn(savedEntreprise);
            when(passwordEncoder.encode(inscriptionUniqueRequest.getMotDePasse())).thenReturn("$2a$10$hash");
            when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(savedUtilisateur);

            // Act
            InscriptionResponse response = authService.inscrireEntrepriseUnique(inscriptionUniqueRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("jean.kamga@epicerie.cm");
            assertThat(response.getGroupId()).isEqualTo(1L);
            assertThat(response.getMessage()).contains("Votre espace a été créé");

            verify(tenantGroupRepository).save(any(TenantGroup.class));
            verify(entrepriseRepository).save(any(Entreprise.class));
            verify(utilisateurRepository).save(any(Utilisateur.class));
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            InscriptionSuccessEvent event = eventCaptor.getValue();
            assertThat(event.getEmail()).isEqualTo("jean.kamga@epicerie.cm");
            assertThat(event.getPrenom()).isEqualTo("Jean");
        }

        @Test
        @DisplayName("❌ Lève BusinessException AUTH_EMAIL_ALREADY_EXISTS quand l'email existe déjà")
        void shouldThrowWhenEmailAlreadyExists() {
            // Arrange
            when(utilisateurRepository.existsByEmail(inscriptionUniqueRequest.getEmail())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.inscrireEntrepriseUnique(inscriptionUniqueRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_EMAIL_ALREADY_EXISTS)
                    .hasMessageContaining("déjà utilisé");

            verify(tenantGroupRepository, never()).save(any());
            verify(entrepriseRepository, never()).save(any());
            verify(utilisateurRepository, never()).save(any());
        }
    }

    // ========================================================================
    // US-007 — Inscription groupe multi-sites
    // ========================================================================

    @Nested
    @DisplayName("Inscription groupe multi-sites (US-007)")
    class InscriptionGroupe {

        private TenantGroup savedGroupeGroupe;
        private Entreprise savedEntrepriseGroupe;
        private Utilisateur savedUtilisateurGroupe;

        @BeforeEach
        void setUp() {
            savedGroupeGroupe = TenantGroup.builder()
                    .id(2L)
                    .nomGroupe("Distribo Sarl")
                    .planAbonnement(PlanAbonnement.GRATUIT)
                    .limiteFiliales(5)
                    .actif(true)
                    .build();

            savedEntrepriseGroupe = Entreprise.builder()
                    .id(2L)
                    .groupe(savedGroupeGroupe)
                    .typeEntreprise(TypeEntreprise.MERE)
                    .nom("Distribo Sarl")
                    .adresseVille("Yaoundé")
                    .adressePays("Cameroun")
                    .nif("M123456789")
                    .telephone("699000001")
                    .email("contact@distribo.cm")
                    .actif(true)
                    .build();

            savedUtilisateurGroupe = Utilisateur.builder()
                    .id(2L)
                    .entreprise(savedEntrepriseGroupe)
                    .scope(ScopeUtilisateur.GROUPE)
                    .role(RoleUtilisateur.ADMIN_GROUPE)
                    .nom("Biya Jr")
                    .prenom("Paul")
                    .email("paul@distribo.cm")
                    .motDePasse("$2a$10$hash")
                    .actif(true)
                    .build();
        }

        @Test
        @DisplayName("✅ Crée un groupe multi-sites avec NIF, téléphone et tous les champs")
        void shouldCreateGroupeSuccessfully() {
            // Arrange
            when(utilisateurRepository.existsByEmail(inscriptionGroupeRequest.getEmailAdmin())).thenReturn(false);
            when(tenantGroupRepository.save(any(TenantGroup.class))).thenReturn(savedGroupeGroupe);
            when(authMapper.toEntrepriseFromGroupe(inscriptionGroupeRequest)).thenReturn(savedEntrepriseGroupe);
            when(entrepriseRepository.save(any(Entreprise.class))).thenReturn(savedEntrepriseGroupe);
            when(passwordEncoder.encode(inscriptionGroupeRequest.getMotDePasse())).thenReturn("$2a$10$hash");
            when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(savedUtilisateurGroupe);

            // Act
            InscriptionResponse response = authService.inscrireGroupe(inscriptionGroupeRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("paul@distribo.cm");
            assertThat(response.getGroupId()).isEqualTo(2L);
            assertThat(response.getMessage()).contains("Votre groupe a été créé");
            assertThat(response.getMessage()).contains("Créez votre première filiale");

            verify(tenantGroupRepository).save(argThat(g ->
                    g.getNomGroupe().equals("Distribo Sarl") &&
                    g.getLimiteFiliales() == 5 &&
                    g.getPlanAbonnement() == PlanAbonnement.GRATUIT));
            verify(entrepriseRepository).save(argThat(e ->
                    e.getNom().equals("Distribo Sarl") &&
                    e.getAdresseVille().equals("Yaoundé") &&
                    "M123456789".equals(e.getNif()) &&
                    "699000001".equals(e.getTelephone()) &&
                    "contact@distribo.cm".equals(e.getEmail())));
            verify(utilisateurRepository).save(argThat(u ->
                    u.getEmail().equals("paul@distribo.cm") &&
                    u.getRole() == RoleUtilisateur.ADMIN_GROUPE &&
                    u.getScope() == ScopeUtilisateur.GROUPE));
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            InscriptionSuccessEvent event = eventCaptor.getValue();
            assertThat(event.getEmail()).isEqualTo("paul@distribo.cm");
            assertThat(event.getPrenom()).isEqualTo("Paul");
            assertThat(event.getNomEntreprise()).isEqualTo("Distribo Sarl");
        }

        @Test
        @DisplayName("❌ Lève BusinessException AUTH_EMAIL_ALREADY_EXISTS quand l'email admin existe déjà")
        void shouldThrowWhenEmailAdminAlreadyExists() {
            // Arrange
            when(utilisateurRepository.existsByEmail(inscriptionGroupeRequest.getEmailAdmin())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.inscrireGroupe(inscriptionGroupeRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_EMAIL_ALREADY_EXISTS)
                    .hasMessageContaining("déjà utilisé");

            verify(tenantGroupRepository, never()).save(any());
            verify(entrepriseRepository, never()).save(any());
            verify(utilisateurRepository, never()).save(any());
        }
    }

    // ========================================================================
    // US-008 — Connexion JWT
    // ========================================================================

    @Nested
    @DisplayName("Connexion JWT (US-008)")
    class Login {

        @Test
        @DisplayName("✅ Retourne LoginResponse avec accessToken et refreshToken quand credentials valides")
        void shouldReturnTokensWhenCredentialsValid() {
            // Arrange
            when(utilisateurRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(savedUtilisateur));
            when(passwordEncoder.matches(loginRequest.getMotDePasse(), savedUtilisateur.getMotDePasse())).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(1L, 1L, 1L, "ADMIN_GROUPE", "GROUPE"))
                    .thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("refresh-token");
            when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800L);
            when(jwtProperties.getAccessTokenExpiration()).thenReturn(900L);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // Act
            LoginResponse response = authService.login(loginRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getExpiresIn()).isEqualTo(900);
            assertThat(response.getRole()).isEqualTo("ADMIN_GROUPE");
            assertThat(response.getScope()).isEqualTo("GROUPE");
        }

        @Test
        @DisplayName("❌ Lève AUTH_INVALID_CREDENTIALS quand l'email n'existe pas")
        void shouldThrowWhenEmailNotFound() {
            // Arrange
            when(utilisateurRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("❌ Lève AUTH_INVALID_CREDENTIALS quand le mot de passe est incorrect")
        void shouldThrowWhenPasswordIncorrect() {
            // Arrange
            when(utilisateurRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(savedUtilisateur));
            when(passwordEncoder.matches(loginRequest.getMotDePasse(), savedUtilisateur.getMotDePasse())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("❌ Lève AUTH_ACCOUNT_DISABLED quand le compte est inactif")
        void shouldThrowWhenAccountDisabled() {
            // Arrange
            savedUtilisateur.setActif(false);
            when(utilisateurRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(savedUtilisateur));
            when(passwordEncoder.matches(loginRequest.getMotDePasse(), savedUtilisateur.getMotDePasse())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_ACCOUNT_DISABLED);
        }

        @Test
        @DisplayName("❌ Lève AUTH_TENANT_SUSPENDED quand le groupe est suspendu")
        void shouldThrowWhenTenantSuspended() {
            // Arrange
            savedGroupe.setActif(false);
            when(utilisateurRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(savedUtilisateur));
            when(passwordEncoder.matches(loginRequest.getMotDePasse(), savedUtilisateur.getMotDePasse())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_TENANT_SUSPENDED);
        }
    }

    // ========================================================================
    // US-009 — Refresh token
    // ========================================================================

    @Nested
    @DisplayName("Refresh token (US-009)")
    class RefreshToken {

        @BeforeEach
        void setUp() {
            lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            lenient().when(jwtProperties.getAccessTokenExpiration()).thenReturn(900L);
        }

        @Test
        @DisplayName("✅ Retourne RefreshTokenResponse avec nouveau accessToken quand refresh token valide")
        void shouldReturnNewAccessTokenWhenRefreshTokenValid() {
            // Arrange
            when(jwtTokenProvider.getUserIdFromToken("valid-refresh-token")).thenReturn(1L);
            when(valueOperations.get("refresh:1")).thenReturn("valid-refresh-token");
            when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(savedUtilisateur));
            when(jwtTokenProvider.generateAccessToken(1L, 1L, 1L, "ADMIN_GROUPE", "GROUPE"))
                    .thenReturn("new-access-token");

            // Act
            RefreshTokenResponse response = authService.refreshAccessToken(refreshTokenRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getExpiresIn()).isEqualTo(900);

            verify(redisTemplate.opsForValue()).get("refresh:1");
        }

        @Test
        @DisplayName("❌ Lève AUTH_INVALID_CREDENTIALS quand le refresh token n'est pas dans Redis")
        void shouldThrowWhenRefreshTokenNotFoundInRedis() {
            // Arrange
            when(jwtTokenProvider.getUserIdFromToken("valid-refresh-token")).thenReturn(1L);
            when(valueOperations.get("refresh:1")).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshAccessToken(refreshTokenRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("❌ Lève AUTH_INVALID_CREDENTIALS quand le refresh token en Redis ne correspond pas")
        void shouldThrowWhenRefreshTokenMismatch() {
            // Arrange
            when(jwtTokenProvider.getUserIdFromToken("valid-refresh-token")).thenReturn(1L);
            when(valueOperations.get("refresh:1")).thenReturn("different-refresh-token");

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshAccessToken(refreshTokenRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("❌ Lève AUTH_INVALID_CREDENTIALS quand l'utilisateur n'existe pas (userId du token)")
        void shouldThrowWhenUserNotFound() {
            // Arrange
            when(jwtTokenProvider.getUserIdFromToken("valid-refresh-token")).thenReturn(999L);
            when(valueOperations.get("refresh:999")).thenReturn("valid-refresh-token");
            when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshAccessToken(refreshTokenRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("❌ Lève AUTH_INVALID_CREDENTIALS quand le token JWT est expiré")
        void shouldThrowWhenRefreshTokenExpired() {
            // Arrange
            when(jwtTokenProvider.getUserIdFromToken("valid-refresh-token"))
                    .thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expiré"));

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshAccessToken(refreshTokenRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
    }

    // ========================================================================
    // US-010 — Déconnexion (révocation du refresh token)
    // ========================================================================

    @Nested
    @DisplayName("Déconnexion (US-010)")
    class Logout {

        @BeforeEach
        void setUp() {
            // Simuler un utilisateur authentifié dans le SecurityContext
            StockMasterPrincipal principal = new StockMasterPrincipal(1L, mock(Claims.class));
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        @AfterEach
        void tearDown() {
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("✅ Supprime le refresh token, blackliste le jti et vide le contexte de sécurité")
        void shouldDeleteRefreshTokenAndClearContext() {
            // Arrange
            Claims mockClaims = mock(Claims.class);
            when(mockClaims.get("jti", String.class)).thenReturn("test-jti-123");
            when(mockClaims.getExpiration()).thenReturn(java.util.Date.from(
                    java.time.Instant.now().plusSeconds(900)));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(redisTemplate.delete("refresh:1")).thenReturn(true);

            // Recréer le principal avec les claims mockés
            StockMasterPrincipal principal = new StockMasterPrincipal(1L, mockClaims);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Act
            authService.logout();

            // Assert
            verify(redisTemplate.opsForValue()).set(
                    eq("blacklist:jti:test-jti-123"),
                    eq("true"),
                    anyLong(),
                    eq(java.util.concurrent.TimeUnit.SECONDS));
            verify(redisTemplate).delete("refresh:1");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("✅ Ne lève pas d'erreur si le refresh token n'existe pas dans Redis")
        void shouldNotThrowWhenNoRefreshTokenInRedis() {
            // Arrange
            Claims mockClaims = mock(Claims.class);
            when(mockClaims.get("jti", String.class)).thenReturn("test-jti-123");
            when(mockClaims.getExpiration()).thenReturn(java.util.Date.from(
                    java.time.Instant.now().plusSeconds(900)));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(redisTemplate.delete("refresh:1")).thenReturn(false);

            StockMasterPrincipal principal = new StockMasterPrincipal(1L, mockClaims);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Act (ne doit pas lever d'exception)
            authService.logout();

            // Assert
            verify(redisTemplate).delete("refresh:1");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("❌ Lève AUTH_TOKEN_INVALID quand aucun utilisateur n'est authentifié")
        void shouldThrowWhenNotAuthenticated() {
            // Arrange
            SecurityContextHolder.clearContext();

            // Act & Assert
            assertThatThrownBy(() -> authService.logout())
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_TOKEN_INVALID);

            verify(redisTemplate, never()).delete(anyString());
        }
    }

    // ========================================================================
    // US-011 — Mot de passe oublié
    // ========================================================================

    @Nested
    @DisplayName("Mot de passe oublié (US-011)")
    class ForgotPassword {

        @Test
        @DisplayName("✅ Génère un token et le stocke dans Redis quand l'email existe")
        void shouldGenerateResetTokenWhenEmailExists() {
            // Arrange
            when(utilisateurRepository.findByEmail("jean.kamga@epicerie.cm"))
                    .thenReturn(Optional.of(savedUtilisateur));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // Act
            authService.forgotPassword(forgotPasswordRequest);

            // Assert
            verify(utilisateurRepository).findByEmail("jean.kamga@epicerie.cm");
            verify(redisTemplate.opsForValue()).set(
                    startsWith("reset:"),
                    eq("1"),
                    eq(900L),
                    eq(TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("✅ Ne fait rien (pas d'erreur) quand l'email n'existe pas")
        void shouldNotThrowWhenEmailNotFound() {
            // Arrange
            when(utilisateurRepository.findByEmail("jean.kamga@epicerie.cm"))
                    .thenReturn(Optional.empty());

            // Act (ne doit pas lever d'exception)
            authService.forgotPassword(forgotPasswordRequest);

            // Assert
            verify(utilisateurRepository).findByEmail("jean.kamga@epicerie.cm");
            verify(redisTemplate, never()).opsForValue();
        }
    }

    // ========================================================================
    // US-012 — Réinitialisation du mot de passe
    // ========================================================================

    @Nested
    @DisplayName("Réinitialisation mot de passe (US-012)")
    class ResetPassword {

        private ResetPasswordRequest resetPasswordRequest;

        @BeforeEach
        void setUp() {
            resetPasswordRequest = ResetPasswordRequest.builder()
                    .token("valid-reset-token")
                    .nouveauMotDePasse("NewPass@2026")
                    .build();

            lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        @DisplayName("✅ Hache le nouveau mot de passe, sauvegarde, supprime le token et révoque refresh")
        void shouldResetPasswordWhenTokenValid() {
            // Arrange
            when(valueOperations.get("reset:valid-reset-token")).thenReturn("1");
            when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(savedUtilisateur));
            when(passwordEncoder.encode("NewPass@2026")).thenReturn("$2a$10$newhash");

            // Act
            authService.resetPassword(resetPasswordRequest);

            // Assert
            verify(passwordEncoder).encode("NewPass@2026");
            verify(utilisateurRepository).save(argThat(u ->
                    u.getMotDePasse().equals("$2a$10$newhash")));
            verify(redisTemplate).delete("reset:valid-reset-token");
            verify(redisTemplate).delete("refresh:1");
        }

        @Test
        @DisplayName("❌ Lève AUTH_RESET_TOKEN_INVALID quand le token est invalide ou expiré")
        void shouldThrowWhenResetTokenInvalid() {
            // Arrange
            when(valueOperations.get("reset:valid-reset-token")).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> authService.resetPassword(resetPasswordRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_RESET_TOKEN_INVALID);

            verify(utilisateurRepository, never()).save(any());
        }
    }

    // ========================================================================
    // US-013 — Changement de mot de passe (utilisateur connecté)
    // ========================================================================

    @Nested
    @DisplayName("Changement mot de passe (US-013)")
    class ChangePassword {

        private ChangePasswordRequest changePasswordRequest;

        @BeforeEach
        void setUp() {
            changePasswordRequest = ChangePasswordRequest.builder()
                    .ancienMotDePasse("MotDePasse@2026")
                    .nouveauMotDePasse("NewPass@2026")
                    .build();

            // Simuler un utilisateur authentifié
            StockMasterPrincipal principal = new StockMasterPrincipal(1L, mock(io.jsonwebtoken.Claims.class));
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);

            lenient().when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(savedUtilisateur));
            lenient().when(passwordEncoder.matches("MotDePasse@2026", savedUtilisateur.getMotDePasse())).thenReturn(true);
        }

        @AfterEach
        void tearDown() {
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("✅ Vérifie l'ancien mot de passe, hache le nouveau, sauvegarde et révoque refresh")
        void shouldChangePasswordWhenOldPasswordValid() {
            // Arrange
            String ancienHash = savedUtilisateur.getMotDePasse();
            when(passwordEncoder.encode("NewPass@2026")).thenReturn("$2a$10$newhash");
            when(redisTemplate.delete("refresh:1")).thenReturn(true);

            // Act
            authService.changePassword(changePasswordRequest);

            // Assert
            verify(passwordEncoder).matches("MotDePasse@2026", ancienHash);
            verify(passwordEncoder).encode("NewPass@2026");
            verify(utilisateurRepository).save(argThat(u ->
                    u.getMotDePasse().equals("$2a$10$newhash")));
            verify(redisTemplate).delete("refresh:1");
        }

        @Test
        @DisplayName("❌ Lève SEC_INVALID_PASSWORD quand l'ancien mot de passe est incorrect")
        void shouldThrowWhenOldPasswordIncorrect() {
            // Arrange
            changePasswordRequest.setAncienMotDePasse("WrongPass@2026");
            when(passwordEncoder.matches("WrongPass@2026", savedUtilisateur.getMotDePasse())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.changePassword(changePasswordRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_INVALID_PASSWORD);

            verify(passwordEncoder, never()).encode(any());
            verify(utilisateurRepository, never()).save(any());
            verify(redisTemplate, never()).delete(anyString());
        }

        @Test
        @DisplayName("❌ Lève AUTH_TOKEN_INVALID quand aucun utilisateur n'est authentifié")
        void shouldThrowWhenNotAuthenticated() {
            // Arrange
            SecurityContextHolder.clearContext();

            // Act & Assert
            assertThatThrownBy(() -> authService.changePassword(changePasswordRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_TOKEN_INVALID);
        }
    }
}
