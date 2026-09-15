package com.stockmaster.utilisateur.service;

import com.stockmaster.shared.config.PaginationProperties;
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
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.utilisateur.dto.response.UtilisateurListResponse;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US-023 — lister les utilisateurs du périmètre. Contrats verrouillés :
 *
 * <ul>
 *   <li><b>Périmètre Groupe↔Filiale</b> (critères d'acceptation US-023) :
 *       Admin Groupe → tous les utilisateurs de toutes ses filiales
 *       ({@code filialeId} optionnel pour resserrer) ; Admin Filiale →
 *       uniquement les utilisateurs de SA filiale — l'appel d'un Admin Filiale
 *       avec le {@code filialeId} d'une AUTRE filiale est refusé
 *       {@code 403 SEC_ACCESS_DENIED} (jamais révéler les données d'une autre
 *       filiale) ;</li>
 *   <li><b>4 rôles métier interdits</b> : CAISSIER / COMMERCIAL / RESP_ACHATS /
 *       GESTIONNAIRE_STOCK → {@code 403 SEC_ACCESS_DENIED}, qu'ils passent ou non
 *       leur propre {@code filialeId} (l'auto-scope ne les légitime pas) ;</li>
 *   <li><b>filiale hors groupe</b> → 403 (jamais 404 : l'existence de la filiale
 *       n'est pas un secret intra-groupe, mais son contenu est refusé) ;</li>
 *   <li><b>filtres optionnels</b> {@code role}, {@code actif}, {@code filialeId},
 *       pagination bornée à {@code stockmaster.pagination.max-page-size} ;</li>
 *   <li><b>le mot de passe n'est JAMAIS retourné</b> — {@code UtilisateurListResponse}
 *       ne porte aucun champ secret ;</li>
 *   <li><b>défense en profondeur</b> : une ligne échappée au filtre périmètre
 *       (autre groupe) est écartée et journalisée, pas servie.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UtilisateurListService — US-023 : lister les utilisateurs du périmètre")
class UtilisateurListServiceTest {

    @Mock
    private TenantGroupRepository groupeRepository;
    @Mock
    private EntrepriseRepository entrepriseRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    /** Vraie instance (POJO @ConfigurationProperties) — pas de stubbing nécessaire. */
    @Spy
    private final PaginationProperties paginationProperties = new PaginationProperties();
    @InjectMocks
    private UtilisateurListService utilisateurListService;

    // ─── fixtures ───────────────────────────────────────────────────────────

    private static final Long GROUP_ID         = 1L;
    private static final Long FILIALE_ID       = 10L;
    private static final Long AUTRE_FILIALE_ID = 11L;
    private static final Long HORS_GROUPE_ID   = 99L;

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
        TenantGroup g = new TenantGroup();
        g.setId(GROUP_ID);
        e.setGroupe(g);
        return e;
    }

    private Utilisateur utilisateur(Long id, Long entrepriseId, RoleUtilisateur role, String motDePasse) {
        return Utilisateur.builder()
                .entreprise(filiale(entrepriseId))
                .scope(ScopeUtilisateur.FILIALE)
                .role(role)
                .prenom("Prenom" + id)
                .nom("Nom" + id)
                .email("user" + id + "@boutique.cm")
                .motDePasse(motDePasse)
                .actif(true)
                .emailVerifie(true)
                .build();
    }

    @BeforeEach
    void setupGroupe() {
        lenient().when(groupeRepository.findById(GROUP_ID)).thenReturn(Optional.of(groupeActif()));
    }

    // ─── périmètre Admin Filiale ────────────────────────────────────────────

    @Nested
    @DisplayName("Périmètre Admin Filiale")
    class PerimetreAdminFiliale {

        @Test
        @DisplayName("✅ l'Admin Filiale voit les utilisateurs de sa filiale (sans paramètre)")
        void shouldListerSaFilialeSansParametre() {
            Utilisateur u = utilisateur(200L, FILIALE_ID, RoleUtilisateur.CAISSIER, "$2a$secret");
            // l'entreprise de l'Admin Filiale vient du JWT (signé) — aucune lecture entrepriseRepository
            when(utilisateurRepository.findByEntrepriseId(FILIALE_ID, null, null, PageRequest.of(0, 20)))
                    .thenReturn(new PageImpl<>(List.of(u)));

            PageResponse<UtilisateurListResponse> page = utilisateurListService.lister(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID),
                    null, null, null, PageRequest.of(0, 20));

            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent()).hasSize(1);
            verify(utilisateurRepository).findByEntrepriseId(FILIALE_ID, null, null, PageRequest.of(0, 20));
        }

        @Test
        @DisplayName("❌ 403 — l'Admin Filiale qui demande une AUTRE filiale du groupe (refus avant toute I/O)")
        void shouldRefuserAutreFilialeDuGroupe() {
            assertThatThrownBy(() -> utilisateurListService.lister(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID),
                    null, null, AUTRE_FILIALE_ID, PageRequest.of(0, 20)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.SEC_ACCESS_DENIED);

            verify(utilisateurRepository, never()).findByEntrepriseId(any(), isNull(), isNull(), any());
            verify(utilisateurRepository, never()).findByEntrepriseGroupeId(any(), isNull(), isNull(), isNull(), any());
        }

        @Test
        @DisplayName("❌ 403 — Admin Groupe demandant une filiale HORS groupe (ni 404 : contenu refusé, existence non confirmée)")
        void shouldRefuserFilialeHorsGroupe() {
            Entreprise horsGroupe = filiale(HORS_GROUPE_ID);
            TenantGroup autreGroupe = new TenantGroup();
            autreGroupe.setId(777L);
            horsGroupe.setGroupe(autreGroupe);
            when(entrepriseRepository.findById(HORS_GROUPE_ID)).thenReturn(Optional.of(horsGroupe));

            assertThatThrownBy(() -> utilisateurListService.lister(
                    principal(RoleUtilisateur.ADMIN_GROUPE, GROUP_ID, FILIALE_ID),
                    null, null, HORS_GROUPE_ID, PageRequest.of(0, 20)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
        }
    }

    // ─── périmètre Admin Groupe ─────────────────────────────────────────────

    @Nested
    @DisplayName("Périmètre Admin Groupe")
    class PerimetreAdminGroupe {

        @Test
        @DisplayName("✅ sans filialeId : toutes les filiales du groupe")
        void shouldListerToutLeGroupeSansFiltre() {
            when(utilisateurRepository.findByEntrepriseGroupeId(GROUP_ID, null, null, null, PageRequest.of(0, 20)))
                    .thenReturn(new PageImpl<>(List.of(
                            utilisateur(200L, FILIALE_ID, RoleUtilisateur.CAISSIER, "$2a$x"),
                            utilisateur(201L, AUTRE_FILIALE_ID, RoleUtilisateur.COMMERCIAL, "$2a$y"))));

            PageResponse<UtilisateurListResponse> page = utilisateurListService.lister(
                    principal(RoleUtilisateur.ADMIN_GROUPE, GROUP_ID, FILIALE_ID),
                    null, null, null, PageRequest.of(0, 20));

            assertThat(page.getTotalElements()).isEqualTo(2);
            verify(utilisateurRepository).findByEntrepriseGroupeId(GROUP_ID, null, null, null, PageRequest.of(0, 20));
        }

        @Test
        @DisplayName("✅ avec filialeId d'une filiale du groupe : vue resserrée autorisée")
        void shouldAutoriserVueResserreeSurFilialeDuGroupe() {
            when(entrepriseRepository.findById(AUTRE_FILIALE_ID)).thenReturn(Optional.of(filiale(AUTRE_FILIALE_ID)));
            when(utilisateurRepository.findByEntrepriseId(AUTRE_FILIALE_ID, null, null, PageRequest.of(0, 20)))
                    .thenReturn(new PageImpl<>(List.of()));

            PageResponse<UtilisateurListResponse> page = utilisateurListService.lister(
                    principal(RoleUtilisateur.ADMIN_GROUPE, GROUP_ID, FILIALE_ID),
                    null, null, AUTRE_FILIALE_ID, PageRequest.of(0, 20));

            assertThat(page.getContent()).isEmpty();
            verify(utilisateurRepository).findByEntrepriseId(AUTRE_FILIALE_ID, null, null, PageRequest.of(0, 20));
        }
    }

    // ─── rôles métier interdits ─────────────────────────────────────────────

    @Nested
    @DisplayName("Rôles métier interdits")
    class RolesMetierInterdits {

        @ParameterizedTest(name = "❌ {0} sans filialeId → 403 (auto-scope refusé, lecture doit être explicite)")
        @EnumSource(value = RoleUtilisateur.class,
                names = {"CAISSIER", "COMMERCIAL", "RESP_ACHATS", "GESTIONNAIRE_STOCK"})
        void shouldRefuserRoleMetierSansFiliale(RoleUtilisateur role) {
            assertThatThrownBy(() -> utilisateurListService.lister(
                    principal(role, GROUP_ID, FILIALE_ID), null, null, null, PageRequest.of(0, 20)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
        }

        @Test
        @DisplayName("❌ 403 — un CAISSIER passant son propre filialeId reste refusé (garde rôle avant toute I/O)")
        void shouldRefuserCaissierMemeAvecSonFilialeId() {
            assertThatThrownBy(() -> utilisateurListService.lister(
                    principal(RoleUtilisateur.CAISSIER, GROUP_ID, FILIALE_ID),
                    null, null, FILIALE_ID, PageRequest.of(0, 20)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
        }
    }

    // ─── garde-fous transverses ─────────────────────────────────────────────

    @Nested
    @DisplayName("Garde-fous transverses")
    class GardeFous {

        @Test
        @DisplayName("❌ GRP_002 — principal sans groupId (token corrompu / Super Admin)")
        void shouldRefuserPrincipalSansGroupId() {
            assertThatThrownBy(() -> utilisateurListService.lister(
                    principal(RoleUtilisateur.ADMIN_GROUPE, null, FILIALE_ID), null, null, null, PageRequest.of(0, 20)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }

        @Test
        @DisplayName("✅ pagination bornée : size=10000 → limitée à max-page-size (100)")
        void shouldBornerTaillePage() {
            when(utilisateurRepository.findByEntrepriseId(FILIALE_ID, null, null, PageRequest.of(0, 100)))
                    .thenReturn(new PageImpl<>(List.of()));

            utilisateurListService.lister(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID),
                    null, null, null, PageRequest.of(0, 10_000));

            verify(utilisateurRepository).findByEntrepriseId(FILIALE_ID, null, null, PageRequest.of(0, 100));
        }

        @Test
        @DisplayName("🛡️ défense en profondeur : une ligne hors groupe échappée au filtre est écartée, pas servie")
        void shouldEcarterLigneHorsGroupe() {
            Utilisateur fantome = utilisateur(300L, FILIALE_ID, RoleUtilisateur.CAISSIER, "$2a$s");
            TenantGroup autreGroupe = new TenantGroup();
            autreGroupe.setId(777L);
            fantome.getEntreprise().setGroupe(autreGroupe); // ligne incohérente échappée au filtre SQL

            when(utilisateurRepository.findByEntrepriseId(FILIALE_ID, null, null, PageRequest.of(0, 20)))
                    .thenReturn(new PageImpl<>(List.of(fantome)));

            PageResponse<UtilisateurListResponse> page = utilisateurListService.lister(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID),
                    null, null, null, PageRequest.of(0, 20));

            assertThat(page.getContent()).isEmpty();
        }

        @Test
        @DisplayName("✅ le mot de passe n'apparaît jamais dans la réponse (aucun champ secret)")
        void shouldNeJamaisExposerLeMotDePasse() {
            Utilisateur u = utilisateur(200L, FILIALE_ID, RoleUtilisateur.CAISSIER, "$2a$SECRET_HASH");
            when(utilisateurRepository.findByEntrepriseId(FILIALE_ID, null, null, PageRequest.of(0, 20)))
                    .thenReturn(new PageImpl<>(List.of(u)));

            PageResponse<UtilisateurListResponse> page = utilisateurListService.lister(
                    principal(RoleUtilisateur.ADMIN_FILIALE, GROUP_ID, FILIALE_ID),
                    null, null, null, PageRequest.of(0, 20));

            String json = page.getContent().get(0).toString();
            assertThat(json).doesNotContain("$2a$SECRET_HASH");
            assertThat(page.getContent().get(0).getRole()).isEqualTo(RoleUtilisateur.CAISSIER);
        }
    }
}
