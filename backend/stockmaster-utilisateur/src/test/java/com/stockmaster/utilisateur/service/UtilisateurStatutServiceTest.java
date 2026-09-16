package com.stockmaster.utilisateur.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.read.ListAppender;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.domain.enums.PlanAbonnement;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.UtilisateurRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.shared.security.TokenRevocationPort;
import com.stockmaster.utilisateur.dto.response.UtilisateurListResponse;
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
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US-025 — activer/désactiver un utilisateur. Contrats verrouillés :
 *
 * <ul>
 *   <li><b>Périmètre</b> : cible inexistante / soft-supprimée / hors périmètre →
 *       {@code 404 RES_001} uniforme (jamais révéler l'existence — miroir US-024) ;
 *       cible {@code ADMIN_*} → {@code 403 SEC_001} (un admin ne peut pas être
 *       désactivé via cet endpoint) ;</li>
 *   <li><b>Révocation</b> (critère d'acceptation US-025) : {@code actif=false} →
 *       {@link TokenRevocationPort#revoquerSessionsDe(Long)} appelé — les tokens
 *       JWT de la cible sont révoqués dans Redis <b>immédiatement</b> ;</li>
 *   <li><b>Réactivation</b> : {@code actif=true} → aucune révocation ;</li>
 *   <li><b>Historique conservé</b> (critère d'acceptation US-025) : la cible est
 *       modifiée, jamais soft-supprimée, jamais recréée — ses mouvements de stock
 *       restent rattachés à son {@code utilisateur_id}.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UtilisateurStatutService — US-025 : activer/désactiver un utilisateur")
class UtilisateurStatutServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private TokenRevocationPort tokenRevocationPort;
    /** Vraie instance — garde-fous purs, aucun stubbing nécessaire. */
    @Spy
    private final PerimetreAdminGuard perimetreAdminGuard = new PerimetreAdminGuard();
    @InjectMocks
    private UtilisateurStatutService utilisateurStatutService;

    @Captor
    private ArgumentCaptor<Utilisateur> utilisateurCaptor;

    // ─── fixtures ───────────────────────────────────────────────────────────

    private static final Long GROUP_ID         = 1L;
    private static final Long FILIALE_ID       = 10L;
    private static final Long AUTRE_FILIALE_ID = 11L;
    private static final Long CIBLE_ID         = 200L;

    private ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> logWatcher;

    @BeforeEach
    void capturerLogs() {
        logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(UtilisateurStatutService.class)).addAppender(logWatcher);
    }

    private StockMasterPrincipal principal(RoleUtilisateur role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        // lenient : les tests fail-fast (rôle interdit) ne consomment pas tous les claims
        lenient().when(claims.get("groupId", Long.class)).thenReturn(groupId);
        lenient().when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        lenient().when(claims.get("role", String.class)).thenReturn(role.name());
        return new StockMasterPrincipal(42L, claims);
    }

    private TenantGroup groupeActif() {
        TenantGroup g = new TenantGroup();
        g.setId(GROUP_ID);
        g.setLimiteUtilisateurs(10);
        g.setPlanAbonnement(PlanAbonnement.GRATUIT);
        return g;
    }

    private Entreprise filiale(Long id) {
        Entreprise e = new Entreprise();
        e.setId(id);
        e.setNom("Filiale " + id);
        e.setTypeEntreprise(TypeEntreprise.FILIALE);
        e.setGroupe(groupeActif());
        return e;
    }

    private Utilisateur cible(RoleUtilisateur role, boolean actif) {
        Utilisateur u = new Utilisateur();
        u.setId(CIBLE_ID);
        u.setEmail("employe@boutique.cm");
        u.setPrenom("Jean");
        u.setNom("Mbarga");
        u.setRole(role);
        u.setActif(actif);
        u.setEntreprise(filiale(FILIALE_ID));
        return u;
    }

    private void cibleTrouvee(Utilisateur u) {
        lenient().when(utilisateurRepository.findById(CIBLE_ID)).thenReturn(Optional.of(u));
        lenient().when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ─── désactivation — cas nominal ────────────────────────────────────────

    @Nested
    @DisplayName("Désactivation — Admin Filiale")
    class Desactivation {

        @Test
        @DisplayName("✅ actif=false → sauvegardé, sessions révoquées (port appelé), historique intact")
        void shouldDesactiverEtRevoquerSessions() {
            Utilisateur employe = cible(RoleUtilisateur.CAISSIER, true);
            cibleTrouvee(employe);

            UtilisateurListResponse response = utilisateurStatutService.changerStatut(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID),
                    CIBLE_ID, false);

            assertThat(response.getActif()).isFalse();
            verify(utilisateurRepository).save(utilisateurCaptor.capture());
            assertThat(utilisateurCaptor.getValue().getActif()).isFalse();
            // Critère d'acceptation US-025 : token JWT révoqué dans Redis
            verify(tokenRevocationPort).revoquerSessionsDe(CIBLE_ID);
            // Critère d'acceptation US-025 : historique conservé — jamais de suppression
            verify(utilisateurRepository, never()).delete(any(Utilisateur.class));
            assertThat(employe.getSupprime()).isNull();
        }

        @Test
        @DisplayName("✅ log INFO explicite avec l'id cible et l'admin")
        void shouldJournaliserLaDesactivation() {
            Utilisateur employe = cible(RoleUtilisateur.CAISSIER, true);
            cibleTrouvee(employe);

            utilisateurStatutService.changerStatut(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID),
                    CIBLE_ID, false);

            assertThat(logWatcher.list)
                    .anySatisfy(e -> {
                        assertThat(e.getLevel()).isEqualTo(Level.INFO);
                        assertThat(e.getFormattedMessage()).contains(String.valueOf(CIBLE_ID));
                    });
        }

        @ParameterizedTest
        @EnumSource(value = RoleUtilisateur.class, names = {"ADMIN_GROUPE", "ADMIN_FILIALE"})
        @DisplayName("❌ 403 — cible ADMIN_* : jamais désactivable via US-025")
        void shouldRefuserCibleAdmin(RoleUtilisateur roleCible) {
            cibleTrouvee(cible(roleCible, true));

            assertThatThrownBy(() -> utilisateurStatutService.changerStatut(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID),
                    CIBLE_ID, false))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED));

            verify(utilisateurRepository, never()).save(any(Utilisateur.class));
            verify(tokenRevocationPort, never()).revoquerSessionsDe(any());
        }
    }

    // ─── réactivation ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Réactivation")
    class Reactivation {

        @Test
        @DisplayName("✅ actif=true → sauvegardé actif, AUCUNE révocation de session")
        void shouldReactiverSansRevoquer() {
            Utilisateur employe = cible(RoleUtilisateur.GESTIONNAIRE_STOCK, false);
            cibleTrouvee(employe);

            UtilisateurListResponse response = utilisateurStatutService.changerStatut(
                    principal(RoleUtilisateur.ADMIN_GROUPE, GROUP_ID, FILIALE_ID),
                    CIBLE_ID, true);

            assertThat(response.getActif()).isTrue();
            verify(tokenRevocationPort, never()).revoquerSessionsDe(any());
        }
    }

    // ─── périmètre — 404 uniforme (miroir US-024) ───────────────────────────

    @Nested
    @DisplayName("Périmètre — 404 uniforme")
    class Perimetre {

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = RoleUtilisateur.class, names = {"GESTIONNAIRE_STOCK", "RESP_ACHATS", "COMMERCIAL", "CAISSIER"})
        @DisplayName("❌ 403 — rôle métier refusé (défense en profondeur après @PreAuthorize)")
        void shouldRefuserRoleMetier(RoleUtilisateur role) {
            assertThatThrownBy(() -> utilisateurStatutService.changerStatut(
                    principal(role, GROUP_ID, FILIALE_ID), CIBLE_ID, false))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED));

            verify(utilisateurRepository, never()).findById(any());
        }

        @Test
        @DisplayName("❌ 404 — cible d'une autre filiale (Admin Filiale) — existence non révélée")
        void shouldRefuserCibleAutreFiliale() {
            Utilisateur ailleurs = cible(RoleUtilisateur.CAISSIER, true);
            ailleurs.setEntreprise(filiale(AUTRE_FILIALE_ID));
            cibleTrouvee(ailleurs);

            assertThatThrownBy(() -> utilisateurStatutService.changerStatut(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID),
                    CIBLE_ID, false))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND));

            verify(tokenRevocationPort, never()).revoquerSessionsDe(any());
        }

        @Test
        @DisplayName("❌ 404 — cible d'un autre groupe (Admin Groupe) — existence non révélée")
        void shouldRefuserCibleAutreGroupe() {
            Utilisateur ailleurs = cible(RoleUtilisateur.CAISSIER, true);
            Entreprise autreGroupe = filiale(AUTRE_FILIALE_ID);
            TenantGroup groupeEtranger = new TenantGroup();
            groupeEtranger.setId(99L);
            autreGroupe.setGroupe(groupeEtranger);
            ailleurs.setEntreprise(autreGroupe);
            cibleTrouvee(ailleurs);

            assertThatThrownBy(() -> utilisateurStatutService.changerStatut(
                    principal(RoleUtilisateur.ADMIN_GROUPE, GROUP_ID, null),
                    CIBLE_ID, false))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND));
        }

        @Test
        @DisplayName("❌ 404 — cible inexistante")
        void shouldRefuserCibleInexistante() {
            when(utilisateurRepository.findById(CIBLE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> utilisateurStatutService.changerStatut(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID),
                    CIBLE_ID, false))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND));
        }

        @Test
        @DisplayName("❌ 404 — cible soft-supprimée — existence non révélée")
        void shouldRefuserCibleSoftSupprimee() {
            Utilisateur supprime = cible(RoleUtilisateur.CAISSIER, true);
            supprime.setSupprime(true);
            cibleTrouvee(supprime);

            assertThatThrownBy(() -> utilisateurStatutService.changerStatut(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID),
                    CIBLE_ID, false))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND));
        }
    }
}
