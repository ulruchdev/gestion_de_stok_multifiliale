package com.stockmaster.tiers.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.tiers.domain.entity.Fournisseur;
import com.stockmaster.tiers.dto.request.CreerFournisseurRequest;
import com.stockmaster.tiers.dto.response.FournisseurResponse;
import com.stockmaster.tiers.mapper.FournisseurMapper;
import com.stockmaster.tiers.mapper.FournisseurMapperImpl;
import com.stockmaster.tiers.repository.FournisseurRepository;
import io.jsonwebtoken.Claims;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US-036 — créer un fournisseur. Contrats verrouillés :
 *
 * <ul>
 *   <li><b>CA1</b> : seuls {@code RESP_ACHATS | ADMIN_FILIALE | ADMIN_GROUPE} —
 *       garde-fou fail-fast dans le service (défense en profondeur derrière le
 *       {@code @PreAuthorize} du contrôleur) ;</li>
 *   <li><b>CA2</b> : {@code entreprise_id} vient du JWT (jamais du corps) — le
 *       DTO n'expose aucun champ tenant, un {@code entrepriseId} forcé dans le
 *       payload est ignoré ;</li>
 *   <li><b>CA3</b> : {@code raisonSociale} obligatoire (max 200 = colonne V1),
 *       {@code telephone} optionnel mais au format E.164 si fourni.</li>
 * </ul>
 *
 * <p>Le {@link FournisseurMapper} est utilisé <b>en vraie instance</b>
 * ({@link FournisseurMapperImpl}, généré par MapStruct) : c'est lui qui
 * prouve que le champ {@code entrepriseId} de la réponse vient bien de
 * l'entité rattachée au tenant du JWT.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FournisseurService — US-036 : créer un fournisseur")
class FournisseurServiceTest {

    @Mock
    private FournisseurRepository fournisseurRepository;
    @Mock
    private EntrepriseRepository entrepriseRepository;
    /** Vraie instance MapStruct — mapping request → entité → réponse vérifiable. */
    @Spy
    private final FournisseurMapper fournisseurMapper = new FournisseurMapperImpl();
    @InjectMocks
    private FournisseurService fournisseurService;

    @Captor
    private ArgumentCaptor<Fournisseur> fournisseurCaptor;

    private static final Long GROUP_ID = 1L;
    private static final Long ENTREPRISE_ID = 10L;
    /** Autre tenant — ne doit JAMAIS être utilisé par le service. */
    private static final Long ENTREPRISE_ID_AUTRE_TENANT = 20L;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    // ─── fixtures ────────────────────────────────────────────────────────────

    private StockMasterPrincipal principal(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        lenient().when(claims.get("role", String.class)).thenReturn(role);
        lenient().when(claims.get("groupId", Long.class)).thenReturn(groupId);
        lenient().when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        return new StockMasterPrincipal(99L, claims);
    }

    private StockMasterPrincipal principalAchats() {
        return principal("RESP_ACHATS", GROUP_ID, ENTREPRISE_ID);
    }

    private Entreprise entrepriseA() {
        Entreprise e = new Entreprise();
        e.setId(ENTREPRISE_ID);
        e.setTypeEntreprise(TypeEntreprise.FILIALE);
        TenantGroup g = new TenantGroup();
        g.setId(GROUP_ID);
        e.setGroupe(g);
        return e;
    }

    private CreerFournisseurRequest requeteValide() {
        CreerFournisseurRequest r = new CreerFournisseurRequest();
        r.setRaisonSociale("SARL Import Golfe");
        r.setTelephone("+237691234567");
        r.setNif("M0126000001");
        r.setContact("Jean Etoundi");
        r.setEmail("achats@importgolfe.cm");
        r.setAdresseVille("Douala");
        return r;
    }

    /** Sauvegarde simulée : MapStruct ne génère pas l'id, le repository si. */
    private void stubSauvegarde() {
        when(fournisseurRepository.save(any(Fournisseur.class))).thenAnswer(inv -> {
            Fournisseur f = inv.getArgument(0);
            f.setId(300L);
            return f;
        });
    }

    // ─── cas nominal ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cas nominal (CA1 + CA2)")
    class CasNominal {

        @ParameterizedTest(name = "✅ crée un fournisseur avec le rôle {0}")
        @ValueSource(strings = {"RESP_ACHATS", "ADMIN_FILIALE", "ADMIN_GROUPE"})
        @DisplayName("✅ 201 — fournisseur rattaché à l'entreprise du JWT")
        void shouldCreerFournisseurWhenRoleAutorise(String role) {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            stubSauvegarde();

            FournisseurResponse response = fournisseurService.creer(
                    principal(role, GROUP_ID, ENTREPRISE_ID), requeteValide());

            assertThat(response.getId()).isEqualTo(300L);
            assertThat(response.getRaisonSociale()).isEqualTo("SARL Import Golfe");
            assertThat(response.getTelephone()).isEqualTo("+237691234567");
            assertThat(response.getAdresseVille()).isEqualTo("Douala");
            assertThat(response.getEntrepriseId()).isEqualTo(ENTREPRISE_ID);

            verify(fournisseurRepository).save(fournisseurCaptor.capture());
            Fournisseur sauvegarde = fournisseurCaptor.getValue();
            assertThat(sauvegarde.getEntreprise()).isNotNull();
            assertThat(sauvegarde.getEntreprise().getId()).isEqualTo(ENTREPRISE_ID);
            assertThat(sauvegarde.getRaisonSociale()).isEqualTo("SARL Import Golfe");
            assertThat(sauvegarde.getTelephone()).isEqualTo("+237691234567");
            assertThat(sauvegarde.getSupprime()).isFalse();
        }
    }

    // ─── CA1 — RBAC ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CA1 — rôles non autorisés")
    class RolesRefuses {

        @ParameterizedTest(name = "❌ 403 — rôle {0}")
        @ValueSource(strings = {"CAISSIER", "COMMERCIAL", "GESTIONNAIRE_STOCK"})
        @DisplayName("❌ 403 SEC_001 — rôle hors périmètre achats, avant toute I/O")
        void shouldThrowWhenRoleNonAutorise(String role) {
            assertThatThrownBy(() -> fournisseurService.creer(
                    principal(role, GROUP_ID, ENTREPRISE_ID), requeteValide()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_ACCESS_DENIED);

            verify(entrepriseRepository, never()).findById(any());
            verify(fournisseurRepository, never()).save(any(Fournisseur.class));
        }

        @Test
        @DisplayName("❌ 403 SEC_001 — principal sans rôle")
        void shouldThrowWhenRoleAbsent() {
            assertThatThrownBy(() -> fournisseurService.creer(
                    principal(null, GROUP_ID, ENTREPRISE_ID), requeteValide()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_ACCESS_DENIED);
        }
    }

    // ─── CA2 — isolation multi-tenant ────────────────────────────────────────

    @Nested
    @DisplayName("CA2 — isolation tenant")
    class IsolationTenant {

        @Test
        @DisplayName("✅ le fournisseur reste sur le tenant du JWT même si le payload force entrepriseId")
        void shouldIgnorerEntrepriseIdForceDansLePayload() throws Exception {
            // Le corps contient un entrepriseId d'un AUTRE tenant — il n'existe
            // aucun champ correspondant dans le DTO, la désérialisation l'ignore.
            String corps = "{\"raisonSociale\":\"SARL Import Golfe\","
                    + "\"telephone\":\"+237691234567\","
                    + "\"entrepriseId\":" + ENTREPRISE_ID_AUTRE_TENANT + "}";
            CreerFournisseurRequest request =
                    objectMapper.readValue(corps, CreerFournisseurRequest.class);

            Set<String> champs = Arrays.stream(CreerFournisseurRequest.class.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());
            assertThat(champs).doesNotContain("entrepriseId", "entreprise", "groupId");

            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            stubSauvegarde();

            fournisseurService.creer(principalAchats(), request);

            verify(fournisseurRepository).save(fournisseurCaptor.capture());
            assertThat(fournisseurCaptor.getValue().getEntreprise().getId())
                    .isEqualTo(ENTREPRISE_ID);
            verify(entrepriseRepository, never()).findById(ENTREPRISE_ID_AUTRE_TENANT);
        }

        @Test
        @DisplayName("❌ 404 RES_001 — entreprise du JWT hors du groupe du JWT")
        void shouldThrow404WhenEntrepriseHorsGroupe() {
            Entreprise horsGroupe = entrepriseA();
            horsGroupe.getGroupe().setId(999L);
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(horsGroupe));

            assertThatThrownBy(() -> fournisseurService.creer(principalAchats(), requeteValide()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(fournisseurRepository, never()).save(any(Fournisseur.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — entreprise du JWT introuvable")
        void shouldThrow404WhenEntrepriseIntrouvable() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> fournisseurService.creer(principalAchats(), requeteValide()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(fournisseurRepository, never()).save(any(Fournisseur.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — entreprise soft-supprimée")
        void shouldThrow404WhenEntrepriseSupprimee() {
            Entreprise supprimee = entrepriseA();
            supprimee.setSupprime(true);
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(supprimee));

            assertThatThrownBy(() -> fournisseurService.creer(principalAchats(), requeteValide()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — principal sans groupId (JWT incomplet)")
        void shouldThrow403WhenPrincipalSansGroupId() {
            assertThatThrownBy(() -> fournisseurService.creer(
                    principal("RESP_ACHATS", null, ENTREPRISE_ID), requeteValide()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);

            verify(entrepriseRepository, never()).findById(any());
        }

        @Test
        @DisplayName("❌ 403 SEC_001 — principal sans entrepriseId (JWT incomplet)")
        void shouldThrow403WhenPrincipalSansEntrepriseId() {
            assertThatThrownBy(() -> fournisseurService.creer(
                    principal("RESP_ACHATS", GROUP_ID, null), requeteValide()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_ACCESS_DENIED);

            verify(entrepriseRepository, never()).findById(any());
        }
    }

    // ─── CA3 — validation du corps ───────────────────────────────────────────

    @Nested
    @DisplayName("CA3 — validation du corps (jakarta.validation)")
    class Validations {

        @Test
        @DisplayName("❌ raisonSociale vide ou en blanc → violation")
        void shouldRefuserRaisonSocialeVide() {
            CreerFournisseurRequest r = requeteValide();
            r.setRaisonSociale("   ");

            assertThat(champsEnErreur(r)).contains("raisonSociale");
        }

        @Test
        @DisplayName("❌ raisonSociale > 200 caractères (colonne V1) → violation")
        void shouldRefuserRaisonSocialeTropLongue() {
            CreerFournisseurRequest r = requeteValide();
            r.setRaisonSociale("x".repeat(201));

            assertThat(champsEnErreur(r)).contains("raisonSociale");
        }

        @Test
        @DisplayName("❌ telephone non camerounais / non E.164 → violation")
        void shouldRefuserTelephoneHorsFormat() {
            CreerFournisseurRequest r = requeteValide();
            r.setTelephone("691234567");

            assertThat(champsEnErreur(r)).contains("telephone");
        }

        @Test
        @DisplayName("✅ telephone au format E.164 camerounais → aucune violation")
        void shouldAccepterTelephoneCameroonValide() {
            CreerFournisseurRequest r = requeteValide();
            r.setTelephone("+237691234567");

            assertThat(validator.validate(r)).isEmpty();
        }

        @Test
        @DisplayName("✅ telephone absent (optionnel) → aucune violation")
        void shouldAccepterTelephoneAbsent() {
            CreerFournisseurRequest r = requeteValide();
            r.setTelephone(null);

            assertThat(validator.validate(r)).isEmpty();
        }
    }

    private Set<String> champsEnErreur(CreerFournisseurRequest request) {
        return validator.validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
