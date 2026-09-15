package com.stockmaster.utilisateur.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.read.ListAppender;
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
import com.stockmaster.utilisateur.dto.request.UtilisateurUpdateRequest;
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
 * US-024 — modifier un utilisateur. Contrats verrouillés :
 *
 * <ul>
 *   <li><b>Périmètre</b> (critère d'acceptation US-024) : la cible doit appartenir
 *       au périmètre de l'admin connecté — hors périmètre, inexistante ou
 *       soft-supprimée → {@code 404 RES_001} uniforme (jamais révéler l'existence) ;</li>
 *   <li><b>Classe modifiable</b> : cible et nouveau rôle limités aux 4 rôles
 *       métier — jamais un {@code ADMIN_*} (pas d'auto-promotion via US-024)
 *       → {@code 403 SEC_001} ;</li>
 *   <li><b>Email</b> : unicité plateforme hors la cible elle-même
 *       ({@code existsByEmailAndIdNot}, miroir US-018) → {@code 409 AUTH_008} ;
 *       {@code emailVerifie} inchangé (le flux de vérification arrive avec
 *       l'EPIC 12 — le réinitialiser verrouillerait le compte sans recours) ;</li>
 *   <li><b>Journalisation</b> : changement de rôle → log INFO explicite ;</li>
 *   <li><b>Sémantique PATCH</b> (endpoint PUT, convention US-018) : champ null =
 *       inchangé ; le mot de passe n'est jamais touché par cet endpoint.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UtilisateurUpdateService — US-024 : modifier un utilisateur")
class UtilisateurUpdateServiceTest {

    @Mock
    private TenantGroupRepository groupeRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @InjectMocks
    private UtilisateurUpdateService utilisateurUpdateService;

    @Captor
    private ArgumentCaptor<Utilisateur> utilisateurCaptor;

    // ─── fixtures ───────────────────────────────────────────────────────────

    private static final Long GROUP_ID         = 1L;
    private static final Long FILIALE_ID       = 10L;
    private static final Long AUTRE_FILIALE_ID = 11L;
    private static final Long CIBLE_ID         = 200L;

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

    /** Employé cible par défaut : CAISSIER de la filiale 10, id {@link #CIBLE_ID}. */
    private Utilisateur cible(RoleUtilisateur role) {
        return Utilisateur.builder()
                .id(CIBLE_ID)
                .entreprise(filiale(FILIALE_ID))
                .scope(ScopeUtilisateur.FILIALE)
                .role(role)
                .prenom("Claude")
                .nom("Fotso")
                .email("claude.fotso@boutique.cm")
                .motDePasse("$2a$HASH_EXISTANT")
                .actif(true)
                .emailVerifie(true)
                .build();
    }

    private UtilisateurUpdateRequest requeteVide() {
        return new UtilisateurUpdateRequest();
    }

    @BeforeEach
    void setupCible() {
        lenient().when(utilisateurRepository.findById(CIBLE_ID))
                .thenAnswer(inv -> Optional.of(cible(RoleUtilisateur.CAISSIER)));
        // save renvoie l'entité passée (comportement JPA réel)
        lenient().when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ─── cas nominaux ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cas nominaux")
    class CasNominaux {

        @Test
        @DisplayName("✅ l'Admin Filiale modifie nom/prénom d'un employé de sa filiale")
        void shouldModifierNomPrenom() {
            UtilisateurUpdateRequest req = requeteVide();
            req.setPrenom("Jean");
            req.setNom("Mbarga");

            UtilisateurListResponse response = utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID), CIBLE_ID, req);

            assertThat(response.getPrenom()).isEqualTo("Jean");
            assertThat(response.getNom()).isEqualTo("Mbarga");
            assertThat(response.getEmail()).isEqualTo("claude.fotso@boutique.cm");
            assertThat(response.getRole()).isEqualTo(RoleUtilisateur.CAISSIER);

            verify(utilisateurRepository).save(utilisateurCaptor.capture());
            Utilisateur sauvegarde = utilisateurCaptor.getValue();
            assertThat(sauvegarde.getMotDePasse()).isEqualTo("$2a$HASH_EXISTANT"); // jamais touché
        }

        @Test
        @DisplayName("✅ l'Admin Groupe modifie un employé d'une de ses filiales")
        void shouldModifierDepuisAdminGroupe() {
            UtilisateurUpdateRequest req = requeteVide();
            req.setNom("Nkoulou");

            utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_GROUPE, GROUP_ID, FILIALE_ID), CIBLE_ID, req);

            verify(utilisateurRepository).save(any());
        }

        @Test
        @DisplayName("✅ email libre → modifié, unicité vérifiée hors la cible")
        void shouldModifierEmailLibre() {
            UtilisateurUpdateRequest req = requeteVide();
            req.setEmail("nouveau@boutique.cm");
            when(utilisateurRepository.existsByEmailAndIdNot("nouveau@boutique.cm", CIBLE_ID)).thenReturn(false);

            UtilisateurListResponse response = utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID), CIBLE_ID, req);

            assertThat(response.getEmail()).isEqualTo("nouveau@boutique.cm");
            verify(utilisateurRepository).save(utilisateurCaptor.capture());
            // emailVerifie inchangé — décision documentée (flux de vérification = EPIC 12)
            assertThat(utilisateurCaptor.getValue().getEmailVerifie()).isTrue();
        }

        @Test
        @DisplayName("✅ email identique à l'actuel → accepté (pas un conflit)")
        void shouldAccepterEmailInchange() {
            UtilisateurUpdateRequest req = requeteVide();
            req.setEmail("claude.fotso@boutique.cm");
            // pas de lookup d'unicité : l'email fourni est identique à l'actuel (court-circuit)
            utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID), CIBLE_ID, req);

            verify(utilisateurRepository).save(any());
        }

        @Test
        @DisplayName("✅ changement de rôle vers un rôle métier autorisé")
        void shouldChangerRoleMetier() {
            UtilisateurUpdateRequest req = requeteVide();
            req.setRole(RoleUtilisateur.COMMERCIAL);

            UtilisateurListResponse response = utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID), CIBLE_ID, req);

            assertThat(response.getRole()).isEqualTo(RoleUtilisateur.COMMERCIAL);
        }

        @Test
        @DisplayName("📜 critère US-024 : le changement de rôle est journalisé en INFO")
        void shouldJournaliserChangementRole() {
            Logger logger = (Logger) LoggerFactory.getLogger(UtilisateurUpdateService.class);
            ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new ListAppender<>();
            appender.start();
            logger.addAppender(appender);
            try {
                UtilisateurUpdateRequest req = requeteVide();
                req.setRole(RoleUtilisateur.GESTIONNAIRE_STOCK);
                utilisateurUpdateService.modifier(
                        principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID), CIBLE_ID, req);
            } finally {
                logger.detachAppender(appender);
            }

            assertThat(appender.list).anyMatch(e ->
                    e.getLevel() == Level.INFO
                            && e.getFormattedMessage().contains("rôle")
                            && e.getFormattedMessage().contains(String.valueOf(CIBLE_ID)));
        }
    }

    // ─── email dupliqué ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Email dupliqué")
    class EmailDuplique {

        @Test
        @DisplayName("❌ 409 AUTH_008 — nouvel email pris par un autre utilisateur")
        void shouldRefuserEmailPris() {
            UtilisateurUpdateRequest req = requeteVide();
            req.setEmail("deja.pris@boutique.cm");
            when(utilisateurRepository.existsByEmailAndIdNot("deja.pris@boutique.cm", CIBLE_ID)).thenReturn(true);

            assertThatThrownBy(() -> utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID), CIBLE_ID, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);

            verify(utilisateurRepository, never()).save(any());
        }
    }

    // ─── périmètre ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Périmètre")
    class Perimetre {

        @Test
        @DisplayName("❌ 404 — cible dans une AUTRE filiale (Admin Filiale) — jamais révéler l'existence")
        void shouldRefuserCibleAutreFiliale() {
            Utilisateur cibleAilleurs = cible(RoleUtilisateur.CAISSIER);
            cibleAilleurs.setEntreprise(filiale(AUTRE_FILIALE_ID));
            when(utilisateurRepository.findById(CIBLE_ID)).thenReturn(Optional.of(cibleAilleurs));

            assertThatThrownBy(() -> utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID), CIBLE_ID, requeteVide()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(utilisateurRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 404 — cible d'un autre groupe (Admin Groupe)")
        void shouldRefuserCibleAutreGroupe() {
            Utilisateur cibleAilleurs = cible(RoleUtilisateur.CAISSIER);
            TenantGroup autreGroupe = new TenantGroup();
            autreGroupe.setId(777L);
            Entreprise autreFiliale = filiale(AUTRE_FILIALE_ID);
            autreFiliale.setGroupe(autreGroupe);
            cibleAilleurs.setEntreprise(autreFiliale);
            when(utilisateurRepository.findById(CIBLE_ID)).thenReturn(Optional.of(cibleAilleurs));

            assertThatThrownBy(() -> utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_GROUPE, GROUP_ID, FILIALE_ID), CIBLE_ID, requeteVide()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ 404 — cible inexistante")
        void shouldRefuserCibleInexistante() {
            when(utilisateurRepository.findById(CIBLE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID), CIBLE_ID, requeteVide()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ 404 — cible soft-supprimée")
        void shouldRefuserCibleSupprimee() {
            Utilisateur supprimee = cible(RoleUtilisateur.CAISSIER);
            supprimee.setSupprime(true);
            when(utilisateurRepository.findById(CIBLE_ID)).thenReturn(Optional.of(supprimee));

            assertThatThrownBy(() -> utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID), CIBLE_ID, requeteVide()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
        }
    }

    // ─── classe non modifiable ──────────────────────────────────────────────

    @Nested
    @DisplayName("Classe non modifiable")
    class ClasseNonModifiable {

        @ParameterizedTest(name = "❌ 403 — la cible ADMIN {0} n'est pas modifiable via US-024")
        @EnumSource(value = RoleUtilisateur.class,
                names = {"ADMIN_GROUPE", "ADMIN_FILIALE", "SUPER_ADMIN"})
        void shouldRefuserCibleAdmin(RoleUtilisateur roleAdmin) {
            Utilisateur admin = cible(roleAdmin);
            admin.setEntreprise(filiale(FILIALE_ID)); // dans le périmètre, mais hors classe
            when(utilisateurRepository.findById(CIBLE_ID)).thenReturn(Optional.of(admin));

            assertThatThrownBy(() -> utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID), CIBLE_ID, requeteVide()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
        }

        @ParameterizedTest(name = "❌ 403 — promotion vers {0} interdite via US-024")
        @EnumSource(value = RoleUtilisateur.class,
                names = {"ADMIN_GROUPE", "ADMIN_FILIALE", "SUPER_ADMIN"})
        void shouldRefuserPromotionVersAdmin(RoleUtilisateur roleAdmin) {
            UtilisateurUpdateRequest req = requeteVide();
            req.setRole(roleAdmin);

            assertThatThrownBy(() -> utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID), CIBLE_ID, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.SEC_ACCESS_DENIED);

            verify(utilisateurRepository, never()).save(any());
        }
    }

    // ─── garde-fous transverses ─────────────────────────────────────────────

    @Nested
    @DisplayName("Garde-fous transverses")
    class GardeFous {

        @ParameterizedTest(name = "❌ 403 — le rôle métier {0} ne peut pas modifier (défense en profondeur)")
        @EnumSource(value = RoleUtilisateur.class,
                names = {"CAISSIER", "COMMERCIAL", "RESP_ACHATS", "GESTIONNAIRE_STOCK"})
        void shouldRefuserRoleMetier(RoleUtilisateur role) {
            assertThatThrownBy(() -> utilisateurUpdateService.modifier(
                    principal(role, GROUP_ID, FILIALE_ID), CIBLE_ID, requeteVide()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
        }

        @Test
        @DisplayName("❌ GRP_002 — principal sans groupId (token corrompu / Super Admin)")
        void shouldRefuserPrincipalSansGroupId() {
            assertThatThrownBy(() -> utilisateurUpdateService.modifier(
                    principal(RoleUtilisateur.ADMIN_GROUPE, null, FILIALE_ID), CIBLE_ID, requeteVide()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
    }
}
