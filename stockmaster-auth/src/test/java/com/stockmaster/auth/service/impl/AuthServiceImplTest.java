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
import com.stockmaster.auth.dto.request.LoginRequest;
import com.stockmaster.auth.dto.response.InscriptionResponse;
import com.stockmaster.auth.dto.response.LoginResponse;
import com.stockmaster.auth.event.InscriptionSuccessEvent;
import com.stockmaster.auth.mapper.AuthMapper;
import com.stockmaster.auth.repository.EntrepriseRepository;
import com.stockmaster.auth.repository.TenantGroupRepository;
import com.stockmaster.auth.repository.UtilisateurRepository;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl — Tests unitaires")
class AuthServiceImplTest {

    @Mock
    private TenantGroupRepository tenantGroupRepository;

    @Mock
    private EntrepriseRepository entrepriseRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<TenantGroup> tenantGroupCaptor;

    @Captor
    private ArgumentCaptor<Utilisateur> utilisateurCaptor;

    @Captor
    private ArgumentCaptor<InscriptionSuccessEvent> eventCaptor;

    // ====================================================================
    // US-006 — Inscription entreprise unique
    // ====================================================================

    @Nested
    @DisplayName("Inscription entreprise unique")
    class InscriptionTests {

        private static final String EMAIL = "jean.kamga@test.cm";
        private static final String MOT_DE_PASSE = "Test@2026";
        private static final String MOT_DE_PASSE_HASH = "$2a$10$hashfactice";
        private static final Long GROUP_ID = 1L;
        private static final Long ENTREPRISE_ID = 10L;
        private static final Long USER_ID = 100L;

        private InscriptionEntrepriseUniqueRequest createValidRequest() {
            return InscriptionEntrepriseUniqueRequest.builder()
                    .nomBoutique("Épicerie Centrale")
                    .ville("Douala")
                    .quartier("Akwa")
                    .prenom("Jean")
                    .nom("Kamga")
                    .email(EMAIL)
                    .motDePasse(MOT_DE_PASSE)
                    .build();
        }

        private TenantGroup createSavedGroup() {
            return TenantGroup.builder()
                    .id(GROUP_ID)
                    .nomGroupe("Épicerie Centrale")
                    .planAbonnement(PlanAbonnement.GRATUIT)
                    .limiteFiliales(1)
                    .actif(true)
                    .build();
        }

        private Entreprise createSavedEntreprise(TenantGroup groupe) {
            Entreprise entreprise = new Entreprise();
            entreprise.setId(ENTREPRISE_ID);
            entreprise.setGroupe(groupe);
            entreprise.setTypeEntreprise(TypeEntreprise.MERE);
            entreprise.setNom("Épicerie Centrale");
            return entreprise;
        }

        @Test
        @DisplayName("✅ Crée un TenantGroup, une Entreprise et un Utilisateur admin")
        void shouldCreateEntrepriseWhenEmailIsUnique() {
            // given
            InscriptionEntrepriseUniqueRequest request = createValidRequest();
            when(utilisateurRepository.existsByEmail(EMAIL)).thenReturn(false);

            TenantGroup savedGroup = createSavedGroup();
            when(tenantGroupRepository.save(any(TenantGroup.class))).thenReturn(savedGroup);

            Entreprise savedEntreprise = createSavedEntreprise(savedGroup);
            when(authMapper.toEntreprise(request)).thenReturn(new Entreprise());
            when(entrepriseRepository.save(any(Entreprise.class))).thenReturn(savedEntreprise);

            when(passwordEncoder.encode(MOT_DE_PASSE)).thenReturn(MOT_DE_PASSE_HASH);
            when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
                Utilisateur u = invocation.getArgument(0);
                u.setId(USER_ID);
                return u;
            });

            // when
            InscriptionResponse response = authService.inscrireEntrepriseUnique(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            assertThat(response.getGroupId()).isEqualTo(GROUP_ID);
            assertThat(response.getMessage()).contains("créé");

            // Vérifier TenantGroup créé avec les bons champs
            verify(tenantGroupRepository).save(tenantGroupCaptor.capture());
            assertThat(tenantGroupCaptor.getValue().getNomGroupe()).isEqualTo("Épicerie Centrale");
            assertThat(tenantGroupCaptor.getValue().getPlanAbonnement()).isEqualTo(PlanAbonnement.GRATUIT);
            assertThat(tenantGroupCaptor.getValue().getLimiteFiliales()).isEqualTo(1);
            assertThat(tenantGroupCaptor.getValue().getActif()).isTrue();

            // Vérifier Utilisateur créé avec ADMIN_GROUPE
            verify(utilisateurRepository).save(utilisateurCaptor.capture());
            assertThat(utilisateurCaptor.getValue().getRole()).isEqualTo(RoleUtilisateur.ADMIN_GROUPE);
            assertThat(utilisateurCaptor.getValue().getScope()).isEqualTo(ScopeUtilisateur.GROUPE);
            assertThat(utilisateurCaptor.getValue().getEmail()).isEqualTo(EMAIL);
            assertThat(utilisateurCaptor.getValue().getMotDePasse()).isEqualTo(MOT_DE_PASSE_HASH);
            assertThat(utilisateurCaptor.getValue().getActif()).isTrue();

            // Vérifier événement publié
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getEmail()).isEqualTo(EMAIL);
            assertThat(eventCaptor.getValue().getPrenom()).isEqualTo("Jean");
        }

        @Test
        @DisplayName("❌ Lève BusinessException AUTH_EMAIL_ALREADY_EXISTS si email déjà utilisé")
        void shouldThrowWhenEmailAlreadyExists() {
            // given
            InscriptionEntrepriseUniqueRequest request = createValidRequest();
            when(utilisateurRepository.existsByEmail(EMAIL)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.inscrireEntrepriseUnique(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_EMAIL_ALREADY_EXISTS)
                    .hasMessageContaining("Cet email");

            // Vérifier aucune sauvegarde
            verify(tenantGroupRepository, never()).save(any());
            verify(entrepriseRepository, never()).save(any());
            verify(utilisateurRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    // ====================================================================
    // US-008 — Connexion JWT
    // ====================================================================

    @Nested
    @DisplayName("Connexion JWT")
    class LoginTests {

        private static final String EMAIL = "jean.kamga@test.cm";
        private static final String MOT_DE_PASSE = "Test@2026";
        private static final String ACCESS_TOKEN = "eyJ.access.token";
        private static final String REFRESH_TOKEN = "eyJ.refresh.token";
        private static final Long USER_ID = 100L;
        private static final Long ENTREPRISE_ID = 10L;
        private static final Long GROUP_ID = 1L;

        private TenantGroup createActiveGroup() {
            return TenantGroup.builder()
                    .id(GROUP_ID)
                    .nomGroupe("Épicerie Centrale")
                    .actif(true)
                    .build();
        }

        private Entreprise createActiveEntreprise(TenantGroup groupe) {
            Entreprise entreprise = new Entreprise();
            entreprise.setId(ENTREPRISE_ID);
            entreprise.setGroupe(groupe);
            entreprise.setNom("Épicerie Centrale");
            return entreprise;
        }

        private Utilisateur createActiveUtilisateur(Entreprise entreprise) {
            return Utilisateur.builder()
                    .id(USER_ID)
                    .entreprise(entreprise)
                    .email(EMAIL)
                    .motDePasse(MOT_DE_PASSE)
                    .role(RoleUtilisateur.ADMIN_GROUPE)
                    .scope(ScopeUtilisateur.GROUPE)
                    .actif(true)
                    .build();
        }

        private LoginRequest createValidLoginRequest() {
            return LoginRequest.builder()
                    .email(EMAIL)
                    .motDePasse(MOT_DE_PASSE)
                    .build();
        }

        @Test
        @DisplayName("✅ Retourne LoginResponse avec tokens quand credentials valides")
        void shouldReturnLoginResponseWhenCredentialsValid() {
            // given
            LoginRequest request = createValidLoginRequest();
            TenantGroup groupe = createActiveGroup();
            Entreprise entreprise = createActiveEntreprise(groupe);
            Utilisateur utilisateur = createActiveUtilisateur(entreprise);

            when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utilisateur));
            when(passwordEncoder.matches(MOT_DE_PASSE, utilisateur.getMotDePasse())).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(USER_ID, ENTREPRISE_ID, GROUP_ID,
                    RoleUtilisateur.ADMIN_GROUPE.name(), ScopeUtilisateur.GROUPE.name()))
                    .thenReturn(ACCESS_TOKEN);
            when(jwtTokenProvider.generateRefreshToken(USER_ID)).thenReturn(REFRESH_TOKEN);

            // when
            LoginResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(response.getExpiresIn()).isEqualTo(900);
            assertThat(response.getRole()).isEqualTo(RoleUtilisateur.ADMIN_GROUPE.name());
            assertThat(response.getScope()).isEqualTo(ScopeUtilisateur.GROUPE.name());
        }

        @Test
        @DisplayName("❌ Lève AUTH_INVALID_CREDENTIALS si email introuvable")
        void shouldThrowWhenEmailNotFound() {
            // given
            LoginRequest request = createValidLoginRequest();
            when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_INVALID_CREDENTIALS);

            verify(jwtTokenProvider, never()).generateAccessToken(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("❌ Lève AUTH_INVALID_CREDENTIALS si mot de passe incorrect")
        void shouldThrowWhenPasswordIncorrect() {
            // given
            LoginRequest request = createValidLoginRequest();
            TenantGroup groupe = createActiveGroup();
            Entreprise entreprise = createActiveEntreprise(groupe);
            Utilisateur utilisateur = createActiveUtilisateur(entreprise);

            when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utilisateur));
            when(passwordEncoder.matches(MOT_DE_PASSE, utilisateur.getMotDePasse())).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_INVALID_CREDENTIALS);

            verify(jwtTokenProvider, never()).generateAccessToken(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("❌ Lève AUTH_ACCOUNT_DISABLED si compte inactif")
        void shouldThrowWhenAccountDisabled() {
            // given
            LoginRequest request = createValidLoginRequest();
            TenantGroup groupe = createActiveGroup();
            Entreprise entreprise = createActiveEntreprise(groupe);
            Utilisateur utilisateur = createActiveUtilisateur(entreprise);
            utilisateur.setActif(false); // ← Compte désactivé

            when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utilisateur));
            when(passwordEncoder.matches(MOT_DE_PASSE, utilisateur.getMotDePasse())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_ACCOUNT_DISABLED);

            verify(jwtTokenProvider, never()).generateAccessToken(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("❌ Lève AUTH_TENANT_SUSPENDED si groupe suspendu")
        void shouldThrowWhenGroupSuspended() {
            // given
            LoginRequest request = createValidLoginRequest();
            TenantGroup groupe = createActiveGroup();
            groupe.setActif(false); // ← Groupe suspendu
            Entreprise entreprise = createActiveEntreprise(groupe);
            Utilisateur utilisateur = createActiveUtilisateur(entreprise);

            when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utilisateur));
            when(passwordEncoder.matches(MOT_DE_PASSE, utilisateur.getMotDePasse())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_TENANT_SUSPENDED);

            verify(jwtTokenProvider, never()).generateAccessToken(any(), any(), any(), any(), any());
        }
    }
}
