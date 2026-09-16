package com.stockmaster.utilisateur.service;

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
import com.stockmaster.utilisateur.dto.request.CreerEmployeRequest;
import com.stockmaster.utilisateur.dto.response.EmployeResponse;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US-022 — créer un employé. Contrats verrouillés :
 * <ul>
 *   <li>scope forcé {@code FILIALE}, entreprise_id depuis le JWT (jamais du corps) ;</li>
 *   <li>rôle limité à {@code GESTIONNAIRE_STOCK | RESP_ACHATS | COMMERCIAL | CAISSIER} —
 *       tout rôle {@code ADMIN_*} → {@code 403 SEC_ACCESS_DENIED} ;</li>
 *   <li>email unique plateforme → {@code 409 AUTH_008} ;</li>
 *   <li>US-101 : limite {@code limite_utilisateurs} lue du plan (jamais en dur) ;</li>
 *   <li>compte créé {@code actif=true, emailVerifie=true} (Option A — provisoire en main propre) ;</li>
 *   <li>mot de passe haché BCrypt, jamais retourné en réponse.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeService — US-022 : créer un employé")
class EmployeServiceTest {

    @Mock
    private EntrepriseRepository entrepriseRepository;
    @Mock
    private TenantGroupRepository groupeRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Spy
    private final ControleLimiteUtilisateursService controleLimiteUtilisateurs =
            new ControleLimiteUtilisateursService();
    /** Vraie instance — garde-fous purs, aucun stubbing nécessaire. */
    @Spy
    private final PerimetreAdminGuard perimetreAdminGuard = new PerimetreAdminGuard();
    @InjectMocks
    private EmployeService employeService;

    @Captor
    private ArgumentCaptor<Utilisateur> utilisateurCaptor;

    // ─── fixtures ───────────────────────────────────────────────────────────

    private static final Long GROUP_ID      = 1L;
    private static final Long ENTREPRISE_ID = 10L;

    /** Construit un principal avec le pattern réel : Claims mock + StockMasterPrincipal(userId, claims). */
    private StockMasterPrincipal principalFiliale() {
        Claims claims = mock(Claims.class);
        // lenient : les tests fail-fast (rôle interdit) ne consomment pas les claims
        lenient().when(claims.get("groupId", Long.class)).thenReturn(GROUP_ID);
        lenient().when(claims.get("entrepriseId", Long.class)).thenReturn(ENTREPRISE_ID);
        return new StockMasterPrincipal(99L, claims);
    }

    /** Principal sans groupId — simule un token corrompu ou un Super Admin. */
    private StockMasterPrincipal principalSansGroupId() {
        Claims claims = mock(Claims.class);
        lenient().when(claims.get("groupId", Long.class)).thenReturn(null);
        lenient().when(claims.get("entrepriseId", Long.class)).thenReturn(ENTREPRISE_ID);
        return new StockMasterPrincipal(99L, claims);
    }

    private Entreprise entrepriseFiliale() {
        Entreprise e = new Entreprise();
        e.setId(ENTREPRISE_ID);
        e.setTypeEntreprise(TypeEntreprise.FILIALE);
        TenantGroup g = new TenantGroup();
        g.setId(GROUP_ID);
        g.setLimiteUtilisateurs(10);
        g.setPlanAbonnement(PlanAbonnement.GRATUIT);
        e.setGroupe(g);
        return e;
    }

    private TenantGroup groupeActif() {
        TenantGroup g = new TenantGroup();
        g.setId(GROUP_ID);
        g.setLimiteUtilisateurs(10);
        g.setPlanAbonnement(PlanAbonnement.GRATUIT);
        return g;
    }

    private CreerEmployeRequest requeteValide(RoleUtilisateur role) {
        CreerEmployeRequest r = new CreerEmployeRequest();
        r.setPrenom("Claude");
        r.setNom("Fotso");
        r.setEmail("claude.fotso@boutique.cm");
        r.setRole(role);
        r.setMotDePasseProvisoire("Prov@2026");
        return r;
    }

    @BeforeEach
    void setupPasswordEncoder() {
        // lenient : inutile dans les tests fail-fast (rôle interdit, isolation) qui ne hashent jamais
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");
    }

    // ─── cas nominal ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cas nominal")
    class CasNominal {

        @ParameterizedTest(name = "✅ crée un employé avec le rôle {0}")
        @EnumSource(value = RoleUtilisateur.class,
                names = {"GESTIONNAIRE_STOCK", "RESP_ACHATS", "COMMERCIAL", "CAISSIER"})
        void shouldCreerEmployeWhenRoleAutorise(RoleUtilisateur role) {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseFiliale()));
            when(utilisateurRepository.existsByEmail("claude.fotso@boutique.cm")).thenReturn(false);
            when(groupeRepository.findById(GROUP_ID)).thenReturn(Optional.of(groupeActif()));
            when(utilisateurRepository.countByEntrepriseGroupeIdAndActifTrueAndSupprimeFalse(GROUP_ID)).thenReturn(3L);
            when(utilisateurRepository.save(any())).thenAnswer(inv -> {
                Utilisateur u = inv.getArgument(0);
                u.setId(200L);
                return u;
            });

            EmployeResponse response = employeService.creer(principalFiliale(), requeteValide(role));

            assertThat(response.getId()).isEqualTo(200L);
            assertThat(response.getEmail()).isEqualTo("claude.fotso@boutique.cm");
            assertThat(response.getRole()).isEqualTo(role);
            assertThat(response.getActif()).isTrue();
            assertThat(response.getEntrepriseId()).isEqualTo(ENTREPRISE_ID);

            verify(utilisateurRepository).save(utilisateurCaptor.capture());
            Utilisateur sauvegarde = utilisateurCaptor.getValue();
            assertThat(sauvegarde.getScope()).isEqualTo(ScopeUtilisateur.FILIALE);
            assertThat(sauvegarde.getActif()).isTrue();
            assertThat(sauvegarde.getEmailVerifie()).isTrue();
            assertThat(sauvegarde.getMotDePasse()).isEqualTo("$2a$hashed"); // jamais en clair
        }
    }

    // ─── rôle interdit ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Rôle interdit")
    class RoleInterdit {

        @ParameterizedTest(name = "❌ refuse le rôle ADMIN {0}")
        @EnumSource(value = RoleUtilisateur.class,
                names = {"ADMIN_GROUPE", "ADMIN_FILIALE", "SUPER_ADMIN"})
        void shouldThrowWhenRoleAdminInterdit(RoleUtilisateur roleAdmin) {
            // fail-fast avant toute requête BDD
            assertThatThrownBy(() ->
                    employeService.creer(principalFiliale(), requeteValide(roleAdmin)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.SEC_ACCESS_DENIED);

            verify(utilisateurRepository, never()).save(any());
        }
    }

    // ─── email dupliqué ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Email dupliqué")
    class EmailDuplique {

        @Test
        @DisplayName("❌ lance AUTH_008 si l'email existe déjà (plateforme)")
        void shouldThrowWhenEmailExiste() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseFiliale()));
            when(utilisateurRepository.existsByEmail("claude.fotso@boutique.cm")).thenReturn(true);

            assertThatThrownBy(() ->
                    employeService.creer(principalFiliale(), requeteValide(RoleUtilisateur.CAISSIER)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);

            verify(utilisateurRepository, never()).save(any());
        }
    }

    // ─── limite utilisateurs ────────────────────────────────────────────────

    @Nested
    @DisplayName("Limite utilisateurs (US-101)")
    class LimiteUtilisateurs {

        @Test
        @DisplayName("❌ lance USR_001 quand la limite du plan est atteinte")
        void shouldThrowWhenLimiteAtteinte() {
            TenantGroup groupe = groupeActif();
            groupe.setLimiteUtilisateurs(5);

            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseFiliale()));
            when(utilisateurRepository.existsByEmail(anyString())).thenReturn(false);
            when(groupeRepository.findById(GROUP_ID)).thenReturn(Optional.of(groupe));
            when(utilisateurRepository.countByEntrepriseGroupeIdAndActifTrueAndSupprimeFalse(GROUP_ID)).thenReturn(5L);

            assertThatThrownBy(() ->
                    employeService.creer(principalFiliale(), requeteValide(RoleUtilisateur.COMMERCIAL)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USR_USER_LIMIT_REACHED);

            verify(utilisateurRepository, never()).save(any());
        }
    }

    // ─── isolation multi-tenant ─────────────────────────────────────────────

    @Nested
    @DisplayName("Isolation multi-tenant")
    class IsolationMultiTenant {

        @Test
        @DisplayName("❌ lance GRP_CROSS_GROUP_FORBIDDEN si le principal n'a pas de groupId")
        void shouldThrowWhenPrincipalSansGroupId() {
            // rôle interdit est vérifié avant — donc on utilise un rôle valide
            assertThatThrownBy(() ->
                    employeService.creer(principalSansGroupId(), requeteValide(RoleUtilisateur.CAISSIER)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }

        @Test
        @DisplayName("❌ lance RES_ENTITY_NOT_FOUND si l'entreprise du JWT est supprimée")
        void shouldThrowWhenEntrepriseSupprimee() {
            Entreprise supprimee = entrepriseFiliale();
            supprimee.setSupprime(true);
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(supprimee));

            assertThatThrownBy(() ->
                    employeService.creer(principalFiliale(), requeteValide(RoleUtilisateur.CAISSIER)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
        }
    }
}
