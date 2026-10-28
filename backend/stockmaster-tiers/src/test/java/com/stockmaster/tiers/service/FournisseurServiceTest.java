package com.stockmaster.tiers.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmaster.shared.config.PaginationProperties;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.tiers.domain.entity.Fournisseur;
import com.stockmaster.tiers.dto.request.CreerFournisseurRequest;
import com.stockmaster.tiers.dto.response.FournisseurResponse;
import com.stockmaster.tiers.mapper.FournisseurMapper;
import com.stockmaster.tiers.mapper.FournisseurMapperImpl;
import com.stockmaster.tiers.port.CommandesFournisseurPort;
import com.stockmaster.tiers.repository.FournisseurRepository;
import io.jsonwebtoken.Claims;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    /** US-039 — port inter-module (implémenté dans stockmaster-achat, DEC-019). */
    @Mock
    private CommandesFournisseurPort commandesFournisseurPort;
    /** Vraie instance — valeurs par défaut du repo (maxPageSize = 100). */
    @Spy
    private final PaginationProperties paginationProperties = new PaginationProperties();
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

    /**
     * Valide le corps avec une {@code ValidatorFactory} ouverte en
     * try-with-resources : la fabrique est fermée à chaque appel (jamais de
     * ressource laissée ouverte par le test).
     */
    private Set<ConstraintViolation<CreerFournisseurRequest>> valider(CreerFournisseurRequest request) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(request);
        }
    }

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
            StockMasterPrincipal acteur = principal(role, GROUP_ID, ENTREPRISE_ID);
            CreerFournisseurRequest requete = requeteValide();

            // S5778 : une seule invocation dans le lambda d'assertThatThrownBy
            assertThatThrownBy(() -> fournisseurService.creer(acteur, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_ACCESS_DENIED);

            verify(entrepriseRepository, never()).findById(any());
            verify(fournisseurRepository, never()).save(any(Fournisseur.class));
        }

        @Test
        @DisplayName("❌ 403 SEC_001 — principal sans rôle")
        void shouldThrowWhenRoleAbsent() {
            StockMasterPrincipal acteur = principal(null, GROUP_ID, ENTREPRISE_ID);
            CreerFournisseurRequest requete = requeteValide();

            assertThatThrownBy(() -> fournisseurService.creer(acteur, requete))
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
            assertThat(champs).isNotEmpty()
                    .doesNotContain("entrepriseId", "entreprise", "groupId");

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

            StockMasterPrincipal acteur = principalAchats();
            CreerFournisseurRequest requete = requeteValide();

            assertThatThrownBy(() -> fournisseurService.creer(acteur, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(fournisseurRepository, never()).save(any(Fournisseur.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — entreprise du JWT introuvable")
        void shouldThrow404WhenEntrepriseIntrouvable() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.empty());

            StockMasterPrincipal acteur = principalAchats();
            CreerFournisseurRequest requete = requeteValide();

            assertThatThrownBy(() -> fournisseurService.creer(acteur, requete))
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

            StockMasterPrincipal acteur = principalAchats();
            CreerFournisseurRequest requete = requeteValide();

            assertThatThrownBy(() -> fournisseurService.creer(acteur, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — principal sans groupId (JWT incomplet)")
        void shouldThrow403WhenPrincipalSansGroupId() {
            StockMasterPrincipal acteur = principal("RESP_ACHATS", null, ENTREPRISE_ID);
            CreerFournisseurRequest requete = requeteValide();

            assertThatThrownBy(() -> fournisseurService.creer(acteur, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);

            verify(entrepriseRepository, never()).findById(any());
        }

        @Test
        @DisplayName("❌ 403 SEC_001 — principal sans entrepriseId (JWT incomplet)")
        void shouldThrow403WhenPrincipalSansEntrepriseId() {
            StockMasterPrincipal acteur = principal("RESP_ACHATS", GROUP_ID, null);
            CreerFournisseurRequest requete = requeteValide();

            assertThatThrownBy(() -> fournisseurService.creer(acteur, requete))
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

            assertThat(valider(r)).isEmpty();
        }

        @Test
        @DisplayName("✅ telephone absent (optionnel) → aucune violation")
        void shouldAccepterTelephoneAbsent() {
            CreerFournisseurRequest r = requeteValide();
            r.setTelephone(null);

            assertThat(valider(r)).isEmpty();
        }
    }

    // ─── US-037 — lister / rechercher ───────────────────────────────────

    @Nested
    @DisplayName("US-037 — lister / rechercher (GET /api/v1/fournisseurs)")
    class Lister {

        private Fournisseur fournisseur(long id, String raisonSociale, String ville) {
            Fournisseur f = Fournisseur.builder()
                    .raisonSociale(raisonSociale)
                    .adresseVille(ville)
                    .entreprise(entrepriseA())
                    .build();
            f.setId(id);
            return f;
        }

        private Page<Fournisseur> pageDe(int numero, int taille, long total, Fournisseur... elements) {
            return new PageImpl<>(List.of(elements), PageRequest.of(numero, taille), total);
        }

        @Test
        @DisplayName("✅ 200 — recherche paginée scopée au tenant du JWT, tri raisonSociale ASC")
        void shouldListerFournisseursDuTenantQuandSearchPresent() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(fournisseurRepository.rechercherParEntrepriseEtTexte(
                    eq(ENTREPRISE_ID), eq("somiref"), any(Pageable.class)))
                    .thenReturn(pageDe(0, 20, 2,
                            fournisseur(1L, "SOMIREF Distribution", "Douala"),
                            fournisseur(2L, "Somiref Plus", "Yaoundé")));

            // search entouré d'espaces : le service doit le trimmer
            PageResponse<FournisseurResponse> reponse =
                    fournisseurService.lister(principalAchats(), "  somiref  ", 0, 20);

            assertThat(reponse.getContent()).hasSize(2);
            assertThat(reponse.getPage()).isZero();
            assertThat(reponse.getSize()).isEqualTo(20);
            assertThat(reponse.getTotalElements()).isEqualTo(2);
            assertThat(reponse.getTotalPages()).isEqualTo(1);
            assertThat(reponse.getContent().get(0).getRaisonSociale()).isEqualTo("SOMIREF Distribution");
            assertThat(reponse.getContent().get(0).getAdresseVille()).isEqualTo("Douala");
            assertThat(reponse.getContent().get(0).getEntrepriseId()).isEqualTo(ENTREPRISE_ID);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(fournisseurRepository).rechercherParEntrepriseEtTexte(
                    eq(ENTREPRISE_ID), eq("somiref"), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageSize()).isEqualTo(20);
            assertThat(pageable.getSort().stream().map(Sort.Order::getProperty))
                    .containsExactly("raisonSociale");
            assertThat(pageable.getSort().getOrderFor("raisonSociale").getDirection())
                    .isEqualTo(Sort.Direction.ASC);
        }

        @ParameterizedTest(name = "✅ search [{0}] → listing complet paginé")
        @NullAndEmptySource
        @ValueSource(strings = "   ")
        @DisplayName("✅ 200 — search absent ou blanc → tous les fournisseurs du tenant")
        void shouldListerToutQuandSearchVide(String search) {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(fournisseurRepository.findAllByEntrepriseIdAndSupprimeFalse(
                    eq(ENTREPRISE_ID), any(Pageable.class)))
                    .thenReturn(pageDe(0, 20, 1, fournisseur(1L, "SARL Import Golfe", "Douala")));

            PageResponse<FournisseurResponse> reponse =
                    fournisseurService.lister(principalAchats(), search, 0, 20);

            assertThat(reponse.getContent()).hasSize(1);
            assertThat(reponse.getTotalElements()).isEqualTo(1);
            verify(fournisseurRepository).findAllByEntrepriseIdAndSupprimeFalse(
                    eq(ENTREPRISE_ID), any(Pageable.class));
            verify(fournisseurRepository, never())
                    .rechercherParEntrepriseEtTexte(any(), any(), any());
        }

        @Test
        @DisplayName("✅ 200 — recherche sans résultat → page vide, jamais 404")
        void shouldRetournerPageVideQuandAucunResultat() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(fournisseurRepository.rechercherParEntrepriseEtTexte(
                    eq(ENTREPRISE_ID), eq("zzz"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            PageResponse<FournisseurResponse> reponse =
                    fournisseurService.lister(principalAchats(), "zzz", 0, 20);

            assertThat(reponse.getContent()).isEmpty();
            assertThat(reponse.getTotalElements()).isZero();
            assertThat(reponse.getTotalPages()).isZero();
        }

        @Test
        @DisplayName("✅ isolation tenant — la requête part toujours pour l'entreprise du JWT")
        void shouldToujoursFiltrerSurLEntrepriseDuJwt() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(fournisseurRepository.rechercherParEntrepriseEtTexte(any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            fournisseurService.lister(principalAchats(), "somiref", 0, 20);

            verify(fournisseurRepository).rechercherParEntrepriseEtTexte(
                    eq(ENTREPRISE_ID), anyString(), any(Pageable.class));
            verify(fournisseurRepository, never())
                    .rechercherParEntrepriseEtTexte(eq(ENTREPRISE_ID_AUTRE_TENANT), any(), any());
            verify(fournisseurRepository, never())
                    .findAllByEntrepriseIdAndSupprimeFalse(eq(ENTREPRISE_ID_AUTRE_TENANT), any());
        }

        @Test
        @DisplayName("❌ 404 — un utilisateur du tenant B ne lit aucun fournisseur du tenant A")
        void shouldRefuserLectureQuandTenantEtranger() {
            Entreprise entrepriseB = entrepriseA();
            entrepriseB.setId(ENTREPRISE_ID_AUTRE_TENANT);
            entrepriseB.getGroupe().setId(999L);
            when(entrepriseRepository.findById(ENTREPRISE_ID_AUTRE_TENANT))
                    .thenReturn(Optional.of(entrepriseB));
            StockMasterPrincipal acteurB = principal("RESP_ACHATS", GROUP_ID, ENTREPRISE_ID_AUTRE_TENANT);

            assertThatThrownBy(() -> fournisseurService.lister(acteurB, "somiref", 0, 20))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(fournisseurRepository, never()).rechercherParEntrepriseEtTexte(any(), any(), any());
            verify(fournisseurRepository, never()).findAllByEntrepriseIdAndSupprimeFalse(any(), any());
        }

        @ParameterizedTest(name = "❌ 403 — rôle {0}")
        @ValueSource(strings = {"CAISSIER", "COMMERCIAL", "GESTIONNAIRE_STOCK"})
        @DisplayName("❌ 403 SEC_001 — lecture hors périmètre achats")
        void shouldRefuserLectureWhenRoleNonAutorise(String role) {
            StockMasterPrincipal acteur = principal(role, GROUP_ID, ENTREPRISE_ID);

            assertThatThrownBy(() -> fournisseurService.lister(acteur, "somiref", 0, 20))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_ACCESS_DENIED);

            verify(fournisseurRepository, never()).rechercherParEntrepriseEtTexte(any(), any(), any());
            verify(fournisseurRepository, never()).findAllByEntrepriseIdAndSupprimeFalse(any(), any());
        }

        @Test
        @DisplayName("✅ bornes — page négative → 0, size 999 → plafonné, tri fixe raisonSociale ASC")
        void shouldBorerPagination() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(fournisseurRepository.findAllByEntrepriseIdAndSupprimeFalse(
                    any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            fournisseurService.lister(principalAchats(), null, -5, 999);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(fournisseurRepository).findAllByEntrepriseIdAndSupprimeFalse(
                    eq(ENTREPRISE_ID), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(100); // stockmaster.pagination.max-page-size
            assertThat(pageable.getSort().getOrderFor("raisonSociale").getDirection())
                    .isEqualTo(Sort.Direction.ASC);
        }
    }

    private Set<String> champsEnErreur(CreerFournisseurRequest request) {
        return valider(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    // ─── US-039 — supprimer (archiver) un fournisseur ──────────────────

    /**
     * US-039 — {@code DELETE /api/v1/fournisseurs/{id}}. Contrats verrouillés
     * (arbitrages validés le 28 oct. 2026) :
     *
     * <ul>
     *   <li><b>Contradiction A — option (c)</b> : 409 {@code RES_005} si une
     *       commande est <b>active</b> ({@code COMMANDEE} | {@code PARTIELLEMENT_RECUE}) ;
     *       archivage autorisé si les commandes sont closes
     *       ({@code RECEPTIONNEE} | {@code ANNULEE}) — CA5 et CA7 réconciliés ;</li>
     *   <li><b>Contradiction B</b> : CA3 ({@code ligne.receptionne=true})
     *       abandonné — la colonne n'existe pas ; le critère est dérivé de l'état ;</li>
     *   <li><b>Soft delete uniquement</b> : aucune ligne n'est supprimée
     *       physiquement ({@code delete*} jamais appelé) ;</li>
     *   <li><b>1.c</b> : 200 + {@code FournisseurResponse} (convention US-029/035) ;</li>
     *   <li><b>1.e/1.f/1.g</b> : 404 {@code RES_001} uniforme — id inconnu, autre
     *       tenant, ou déjà archivé (idempotence).</li>
     * </ul>
     */
    @Nested
    @DisplayName("US-039 — supprimer (archiver) un fournisseur")
    class SupprimerFournisseur {

        private static final Long ID_CIBLE = 400L;

        private Fournisseur fournisseurActif() {
            Fournisseur f = new Fournisseur();
            f.setId(ID_CIBLE);
            f.setEntreprise(entrepriseA());
            f.setRaisonSociale("SOMIREF Distribution");
            f.setAdresseVille("Douala");
            f.setSupprime(false);
            return f;
        }

        @Test
        @DisplayName("✅ 200 — aucune commande : supprime=true, ligne conservée en base")
        void shouldArchiverQuandAucuneCommande() {
            Fournisseur existant = fournisseurActif();
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(fournisseurRepository.findByIdAndSupprimeFalse(ID_CIBLE))
                    .thenReturn(Optional.of(existant));
            when(commandesFournisseurPort.aDesCommandesActives(ID_CIBLE)).thenReturn(false);
            when(fournisseurRepository.save(any(Fournisseur.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            FournisseurResponse reponse = fournisseurService.supprimer(principalAchats(), ID_CIBLE);

            assertThat(reponse.getId()).isEqualTo(ID_CIBLE);
            assertThat(reponse.getSupprime()).isTrue();
            assertThat(reponse.getRaisonSociale()).isEqualTo("SOMIREF Distribution");
            assertThat(reponse.getEntrepriseId()).isEqualTo(ENTREPRISE_ID);

            verify(fournisseurRepository).save(fournisseurCaptor.capture());
            assertThat(fournisseurCaptor.getValue().getSupprime()).isTrue();
            // Soft delete : aucune suppression physique, jamais.
            verify(fournisseurRepository, never()).delete(any(Fournisseur.class));
            verify(fournisseurRepository, never()).deleteById(any());
            verify(fournisseurRepository, never()).deleteAll();
        }

        @Test
        @DisplayName("❌ 409 RES_005 — commande active (COMMANDEE/PARTIELLEMENT_RECUE) : rien n'est modifié")
        void shouldRefuserQuandCommandeActive() {
            Fournisseur existant = fournisseurActif();
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(fournisseurRepository.findByIdAndSupprimeFalse(ID_CIBLE))
                    .thenReturn(Optional.of(existant));
            when(commandesFournisseurPort.aDesCommandesActives(ID_CIBLE)).thenReturn(true);

            assertThatThrownBy(() -> fournisseurService.supprimer(principalAchats(), ID_CIBLE))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_HAS_DEPENDENCIES);

            verify(fournisseurRepository, never()).save(any(Fournisseur.class));
            assertThat(existant.getSupprime()).isFalse();
        }

        @ParameterizedTest(name = "✅ 200 — commandes closes ({0}) n''empêchent pas l''archivage")
        @ValueSource(strings = {"RECEPTIONNEE", "ANNULEE"})
        @DisplayName("✅ 200 — CA5 : une commande reçue ou annulée ne bloque pas")
        void shouldArchiverQuandCommandesCloses(String etat) {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(fournisseurRepository.findByIdAndSupprimeFalse(ID_CIBLE))
                    .thenReturn(Optional.of(fournisseurActif()));
            when(commandesFournisseurPort.aDesCommandesActives(ID_CIBLE)).thenReturn(false);
            when(fournisseurRepository.save(any(Fournisseur.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            FournisseurResponse reponse = fournisseurService.supprimer(principalAchats(), ID_CIBLE);

            assertThat(reponse.getSupprime()).isTrue();
            verify(commandesFournisseurPort).aDesCommandesActives(ID_CIBLE);
        }

        @ParameterizedTest(name = "❌ 403 — rôle {0}")
        @ValueSource(strings = {"CAISSIER", "COMMERCIAL", "GESTIONNAIRE_STOCK"})
        @DisplayName("❌ 403 SEC_001 — rôle hors périmètre achats, avant toute I/O")
        void shouldRefuserQuandRoleNonAutorise(String role) {
            StockMasterPrincipal acteur = principal(role, GROUP_ID, ENTREPRISE_ID);

            assertThatThrownBy(() -> fournisseurService.supprimer(acteur, ID_CIBLE))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_ACCESS_DENIED);

            verify(entrepriseRepository, never()).findById(any());
            verify(fournisseurRepository, never()).findByIdAndSupprimeFalse(any());
            verify(commandesFournisseurPort, never()).aDesCommandesActives(any());
            verify(fournisseurRepository, never()).save(any(Fournisseur.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — id inexistant, aucune dépendance n'est même interrogée")
        void shouldRetourner404QuandIdInexistant() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(fournisseurRepository.findByIdAndSupprimeFalse(9999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> fournisseurService.supprimer(principalAchats(), 9999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(commandesFournisseurPort, never()).aDesCommandesActives(any());
            verify(fournisseurRepository, never()).save(any(Fournisseur.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — déjà archivé : idempotence, indistinguable d'un id inconnu")
        void shouldRetourner404QuandDejaArchive() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(fournisseurRepository.findByIdAndSupprimeFalse(ID_CIBLE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> fournisseurService.supprimer(principalAchats(), ID_CIBLE))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(fournisseurRepository, never()).save(any(Fournisseur.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — IDOR : tenant A sur un fournisseur du tenant B, rien n'est écrit")
        void shouldRefuserSuppressionInterTenant() {
            Entreprise entrepriseB = new Entreprise();
            entrepriseB.setId(ENTREPRISE_ID_AUTRE_TENANT);
            TenantGroup groupeB = new TenantGroup();
            groupeB.setId(777L);
            entrepriseB.setGroupe(groupeB);

            Fournisseur fournisseurDeB = fournisseurActif();
            fournisseurDeB.setEntreprise(entrepriseB);

            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(fournisseurRepository.findByIdAndSupprimeFalse(ID_CIBLE))
                    .thenReturn(Optional.of(fournisseurDeB));

            assertThatThrownBy(() -> fournisseurService.supprimer(principalAchats(), ID_CIBLE))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(commandesFournisseurPort, never()).aDesCommandesActives(any());
            verify(fournisseurRepository, never()).save(any(Fournisseur.class));
            assertThat(fournisseurDeB.getSupprime()).isFalse();
        }

        @Test
        @DisplayName("❌ 404 RES_001 — entreprise du JWT hors groupe : refus avant lecture")
        void shouldRefuserQuandEntrepriseHorsGroupe() {
            Entreprise entrepriseAutreGroupe = entrepriseA();
            entrepriseAutreGroupe.getGroupe().setId(999L);
            when(entrepriseRepository.findById(ENTREPRISE_ID))
                    .thenReturn(Optional.of(entrepriseAutreGroupe));

            assertThatThrownBy(() -> fournisseurService.supprimer(principalAchats(), ID_CIBLE))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(fournisseurRepository, never()).findByIdAndSupprimeFalse(any());
            verify(fournisseurRepository, never()).save(any(Fournisseur.class));
        }
    }
}
