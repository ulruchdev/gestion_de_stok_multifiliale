package com.stockmaster.utilisateur.service;

import com.stockmaster.notification.port.CanalNotification;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.domain.enums.PlanAbonnement;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.domain.enums.ScopeUtilisateur;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.repository.UtilisateurRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.dto.request.AdminFilialeCreateRequest;
import com.stockmaster.utilisateur.dto.response.UtilisateurAdminResponse;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US-021 — créer un Admin Filiale. Contrats verrouillés :
 * <ul>
 *   <li>rôle FORCÉ {@code ADMIN_FILIALE}, scope {@code FILIALE} (jamais lu de la requête) ;</li>
 *   <li>isolation multi-tenant : la filiale doit appartenir au groupe du JWT →
 *       {@code 404} uniforme (inexistante, soft-supprimée, autre groupe, maison mère) ;</li>
 *   <li>email unique plateforme → {@code 409 AUTH_008} ;</li>
 *   <li>US-101 : limite lue de {@code tenant_group.limite_utilisateurs} (jamais en dur),
 *       plan expiré dégradé GRATUIT (10, DEC-015), dépassement → {@code 403 USR_001} ;</li>
 *   <li>compte créé {@code actif=false, emailVerifie=false} — mot de passe aléatoire
 *       haché (aucune connexion possible avant activation) ;</li>
 *   <li>token d'invitation en Redis ({@code invitation:{token}} → userId, TTL 48 h) ;</li>
 *   <li>échec d'envoi email → avalé : le compte existe quand même (philosophie US-006).</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminFilialeService — US-021 : créer un Admin Filiale")
class AdminFilialeServiceTest {

    private static final String INVITATION_TTL_HEURES = "48";

    @Mock
    private EntrepriseRepository entrepriseRepository;

    @Mock
    private TenantGroupRepository groupeRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private CanalNotification canalEmail;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * Spy et non mock : la logique de limite (US-101) est le vrai comportement
     * à couvrir de bout en bout — seul l'environnement (repos, Redis, canal) est mocké.
     */
    @Spy
    private final ControleLimiteUtilisateursService controleLimiteUtilisateurs =
            new ControleLimiteUtilisateursService();

    @InjectMocks
    private AdminFilialeService service;

    @Captor
    private ArgumentCaptor<Utilisateur> utilisateurCaptor;

    private StockMasterPrincipal principalWithGroup(Long groupId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        return new StockMasterPrincipal(9L, claims);
    }

    private TenantGroup groupe(Integer limiteUtilisateurs, LocalDate expiration) {
        return TenantGroup.builder()
                .id(1L)
                .nomGroupe("Distribo")
                .planAbonnement(PlanAbonnement.PRO)
                .actif(true)
                .dateExpirationPlan(expiration)
                .limiteFiliales(15)
                .limiteUtilisateurs(limiteUtilisateurs)
                .supprime(false)
                .build();
    }

    private Entreprise filialeDuGroupe(Long groupeId) {
        return Entreprise.builder()
                .id(5L)
                .groupe(TenantGroup.builder().id(groupeId).build())
                .nom("Boutique Akwa")
                .typeEntreprise(TypeEntreprise.FILIALE)
                .actif(true)
                .siteOperationnel(true)
                .codeFiliale("DLA01")
                .supprime(false)
                .build();
    }

    private AdminFilialeCreateRequest requeteValide() {
        AdminFilialeCreateRequest request = new AdminFilialeCreateRequest();
        request.setFilialeId(5L);
        request.setPrenom("Marie");
        request.setNom("Ngono");
        request.setEmail("marie.ngono@distribo.cm");
        return request;
    }

    /**
     * Aiguillage partagé du happy path — stubs {@code lenient()} car certains tests
     * redéfinissent le décompte d'utilisateurs ou s'arrêtent avant une étape ;
     * la stricte sémantique de Mockito reste réservée aux stubs propres à chaque test.
     */
    private void aiguillageHappyPath(TenantGroup groupe, Entreprise filiale) {
        lenient().when(entrepriseRepository.findById(5L)).thenReturn(Optional.of(filiale));
        lenient().when(utilisateurRepository.existsByEmail("marie.ngono@distribo.cm")).thenReturn(false);
        lenient().when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupe));
        lenient().when(utilisateurRepository.countByEntrepriseGroupeIdAndActifTrueAndSupprimeFalse(1L)).thenReturn(3L);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hache");
        lenient().when(utilisateurRepository.save(any(Utilisateur.class)))
                .thenAnswer(invocation -> {
                    Utilisateur u = invocation.getArgument(0, Utilisateur.class);
                    u.setId(12L);
                    return u;
                });
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("✅ crée l'admin : rôle forcé ADMIN_FILIALE, scope FILIALE, actif=false, mot de passe haché")
    void shouldCreateAdminFilialeWithForcedRole() {
        aiguillageHappyPath(groupe(50, null), filialeDuGroupe(1L));

        UtilisateurAdminResponse response = service.creer(principalWithGroup(1L), requeteValide());

        verify(utilisateurRepository).save(utilisateurCaptor.capture());
        Utilisateur cree = utilisateurCaptor.getValue();
        assertThat(cree.getRole()).isEqualTo(RoleUtilisateur.ADMIN_FILIALE);
        assertThat(cree.getScope()).isEqualTo(ScopeUtilisateur.FILIALE);
        assertThat(cree.getActif()).isFalse();
        assertThat(cree.getEmailVerifie()).isFalse();
        assertThat(cree.getMotDePasse()).isEqualTo("$2a$10$hache");
        assertThat(cree.getEntreprise().getId()).isEqualTo(5L);
        assertThat(cree.getEmail()).isEqualTo("marie.ngono@distribo.cm");
        assertThat(response.getRole()).isEqualTo(RoleUtilisateur.ADMIN_FILIALE);
        assertThat(response.getActif()).isFalse();
        assertThat(response.getFilialeId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("✅ token d'invitation en Redis (TTL 48 h) + email via le canal DEC-014")
    void shouldStoreInvitationTokenAndSendEmail() {
        aiguillageHappyPath(groupe(50, null), filialeDuGroupe(1L));

        service.creer(principalWithGroup(1L), requeteValide());

        verify(valueOperations).set(anyString(), eq("12"), eq(java.time.Duration.ofHours(48)));
        verify(canalEmail).envoyer(eq(5L), eq(12L), anyString(),
                anyString(), anyString());
    }

    @Test
    @DisplayName("❌ filiale d'un autre groupe → 404 uniforme (jamais révéler l'existence)")
    void shouldRejectFilialeFromAnotherGroup() {
        when(entrepriseRepository.findById(5L)).thenReturn(Optional.of(filialeDuGroupe(99L)));

        assertThatThrownBy(() -> service.creer(principalWithGroup(1L), requeteValide()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND));
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("❌ filiale inexistante → 404")
    void shouldRejectUnknownFiliale() {
        when(entrepriseRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.creer(principalWithGroup(1L), requeteValide()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND));
    }

    @Test
    @DisplayName("❌ filiale soft-supprimée → 404")
    void shouldRejectSoftDeletedFiliale() {
        Entreprise supprimee = filialeDuGroupe(1L);
        supprimee.setSupprime(true);
        when(entrepriseRepository.findById(5L)).thenReturn(Optional.of(supprimee));

        assertThatThrownBy(() -> service.creer(principalWithGroup(1L), requeteValide()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND));
    }

    @Test
    @DisplayName("❌ id = maison mère (type MERE) → 404 (pas une filiale)")
    void shouldRejectMaisonMere() {
        Entreprise mere = filialeDuGroupe(1L);
        mere.setTypeEntreprise(TypeEntreprise.MERE);
        when(entrepriseRepository.findById(5L)).thenReturn(Optional.of(mere));

        assertThatThrownBy(() -> service.creer(principalWithGroup(1L), requeteValide()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND));
    }

    @Test
    @DisplayName("❌ email déjà pris (unicité plateforme) → 409 AUTH_008")
    void shouldRejectDuplicateEmail() {
        when(entrepriseRepository.findById(5L)).thenReturn(Optional.of(filialeDuGroupe(1L)));
        when(utilisateurRepository.existsByEmail("marie.ngono@distribo.cm")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(principalWithGroup(1L), requeteValide()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS));
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("❌ US-101 : limite du plan atteinte (50/50 PRO) → 403 USR_001")
    void shouldRejectWhenUserLimitReached() {
        aiguillageHappyPath(groupe(50, null), filialeDuGroupe(1L));
        when(utilisateurRepository.countByEntrepriseGroupeIdAndActifTrueAndSupprimeFalse(1L)).thenReturn(50L);

        assertThatThrownBy(() -> service.creer(principalWithGroup(1L), requeteValide()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.USR_USER_LIMIT_REACHED));
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
        verify(canalEmail, never()).envoyer(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("❌ US-101 : plan expiré → dégradé GRATUIT (10) même si limite stockée = 50")
    void shouldApplyExpiredPlanDegradation() {
        aiguillageHappyPath(groupe(50, LocalDate.now().minusDays(1)), filialeDuGroupe(1L));
        when(utilisateurRepository.countByEntrepriseGroupeIdAndActifTrueAndSupprimeFalse(1L)).thenReturn(15L);

        assertThatThrownBy(() -> service.creer(principalWithGroup(1L), requeteValide()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.USR_USER_LIMIT_REACHED));
    }

    @Test
    @DisplayName("❌ US-101 : limite absente (null) → refus fail-safe")
    void shouldRefuseWhenLimitMissing() {
        aiguillageHappyPath(groupe(null, null), filialeDuGroupe(1L));

        assertThatThrownBy(() -> service.creer(principalWithGroup(1L), requeteValide()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.USR_USER_LIMIT_REACHED));
    }

    @Test
    @DisplayName("✅ échec d'envoi email → avalé, le compte est créé quand même (US-006)")
    void shouldNotFailWhenEmailSendingFails() {
        aiguillageHappyPath(groupe(50, null), filialeDuGroupe(1L));
        doThrow(new RuntimeException("SMTP down"))
                .when(canalEmail).envoyer(any(), any(), any(), any(), any());

        assertThatCode(() -> service.creer(principalWithGroup(1L), requeteValide()))
                .doesNotThrowAnyException();
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("❌ principal sans groupId → GRP_CROSS_GROUP_FORBIDDEN")
    void shouldRejectPrincipalWithoutGroup() {
        assertThatThrownBy(() -> service.creer(principalWithGroup(null), requeteValide()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN));
    }

    @Test
    @DisplayName("✅ l'email d'invitation contient le token et le rôle attribué")
    void shouldIncludeTokenAndRoleInInvitationEmail() {
        aiguillageHappyPath(groupe(50, null), filialeDuGroupe(1L));

        service.creer(principalWithGroup(1L), requeteValide());

        ArgumentCaptor<String> sujet = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> corps = ArgumentCaptor.forClass(String.class);
        verify(canalEmail).envoyer(any(), any(), any(), sujet.capture(), corps.capture());
        assertThat(sujet.getValue()).contains("Boutique Akwa");
        assertThat(corps.getValue()).contains("ADMIN_FILIALE");
        assertThat(corps.getValue()).contains(INVITATION_TTL_HEURES);
    }
}
