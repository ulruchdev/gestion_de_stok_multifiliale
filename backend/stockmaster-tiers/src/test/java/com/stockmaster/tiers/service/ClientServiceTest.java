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
import com.stockmaster.tiers.domain.entity.Client;
import com.stockmaster.tiers.dto.request.CreerClientRequest;
import com.stockmaster.tiers.dto.request.ModifierClientRequest;
import com.stockmaster.tiers.dto.response.ClientResponse;
import com.stockmaster.tiers.mapper.ClientMapper;
import com.stockmaster.tiers.mapper.ClientMapperImpl;
import com.stockmaster.tiers.port.CommandesClientPort;
import com.stockmaster.tiers.repository.ClientRepository;
import io.jsonwebtoken.Claims;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
 * US-040 → US-043 — CRUD clients (EPIC 6, Sprint 5).
 *
 * <p>Contrats verrouillés (arbitrages du 9 nov. 2026) :</p>
 * <ul>
 *   <li><b>RBAC</b> : {@code COMMERCIAL | ADMIN_FILIALE | ADMIN_GROUPE} en
 *       écriture ; le {@code CAISSIER} est admis <b>en lecture seule</b>
 *       (recherche d'un client au comptoir) ;</li>
 *   <li><b>Isolation tenant</b> : {@code entreprise_id} vient du JWT, jamais du
 *       corps ; 404 {@code RES_001} uniforme hors tenant ;</li>
 *   <li><b>Suppression</b> : soft delete, {@code 409 RES_005} dès qu'une
 *       commande client existe, quel que soit son état (y compris annulée) ;</li>
 *   <li><b>Recherche US-041</b> : nom, prénom ou téléphone, tri fixe
 *       {@code nom ASC}, size borné à 100.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService — US-040→043 : CRUD clients")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private EntrepriseRepository entrepriseRepository;
    /** US-043 — port inter-module (implémenté dans stockmaster-vente, DEC-019). */
    @Mock
    private CommandesClientPort commandesClientPort;
    /** Vraie instance — valeurs par défaut du repo (maxPageSize = 100). */
    @Spy
    private final PaginationProperties paginationProperties = new PaginationProperties();
    /** Vraie instance MapStruct — mapping vérifiable de bout en bout. */
    @Spy
    private final ClientMapper clientMapper = new ClientMapperImpl();

    private ClientService clientService;

    @Captor
    private ArgumentCaptor<Client> clientCaptor;

    private static final Long GROUP_ID = 1L;
    private static final Long ENTREPRISE_ID = 10L;
    /** Autre tenant — ne doit JAMAIS être utilisé par le service. */
    private static final Long ENTREPRISE_ID_AUTRE_TENANT = 20L;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setUp() {
        clientService = new ClientService(
                clientRepository,
                new TiersPerimetreGuard(entrepriseRepository),
                clientMapper,
                paginationProperties,
                commandesClientPort);
    }

    // ─── fixtures ────────────────────────────────────────────────────────────

    private StockMasterPrincipal principal(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        lenient().when(claims.get("role", String.class)).thenReturn(role);
        lenient().when(claims.get("groupId", Long.class)).thenReturn(groupId);
        lenient().when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        return new StockMasterPrincipal(99L, claims);
    }

    private StockMasterPrincipal principalCommercial() {
        return principal("COMMERCIAL", GROUP_ID, ENTREPRISE_ID);
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

    private CreerClientRequest requeteValide() {
        CreerClientRequest r = new CreerClientRequest();
        r.setNom("Martin");
        r.setPrenom("Alice");
        r.setTelephone("+237691234567");
        r.setEmail("alice.martin@example.cm");
        r.setAdresseVille("Douala");
        r.setAdresseQuartier("Bonapriso");
        return r;
    }

    private void stubSauvegarde() {
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> {
            Client c = inv.getArgument(0);
            c.setId(300L);
            return c;
        });
    }

    private Set<String> champsEnErreur(CreerClientRequest request) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(request).stream()
                    .map(v -> ((ConstraintViolation<?>) v).getPropertyPath().toString())
                    .collect(Collectors.toSet());
        }
    }

    private Set<String> champsEnErreur(ModifierClientRequest request) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(request).stream()
                    .map(v -> ((ConstraintViolation<?>) v).getPropertyPath().toString())
                    .collect(Collectors.toSet());
        }
    }

    // ─── US-040 — créer un client ────────────────────────────────────────────

    @Nested
    @DisplayName("US-040 — créer un client")
    class CreerClient {

        @ParameterizedTest(name = "✅ crée un client avec le rôle {0}")
        @ValueSource(strings = {"COMMERCIAL", "ADMIN_FILIALE", "ADMIN_GROUPE"})
        @DisplayName("✅ 201 — client rattaché à l'entreprise du JWT")
        void shouldCreerClientWhenRoleAutorise(String role) {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            stubSauvegarde();

            ClientResponse response = clientService.creer(
                    principal(role, GROUP_ID, ENTREPRISE_ID), requeteValide());

            assertThat(response.getId()).isEqualTo(300L);
            assertThat(response.getNom()).isEqualTo("Martin");
            assertThat(response.getPrenom()).isEqualTo("Alice");
            assertThat(response.getTelephone()).isEqualTo("+237691234567");
            assertThat(response.getEmail()).isEqualTo("alice.martin@example.cm");
            assertThat(response.getAdresseVille()).isEqualTo("Douala");
            assertThat(response.getAdresseQuartier()).isEqualTo("Bonapriso");
            assertThat(response.getEntrepriseId()).isEqualTo(ENTREPRISE_ID);
            assertThat(response.getSupprime()).isFalse();

            verify(clientRepository).save(clientCaptor.capture());
            Client sauvegarde = clientCaptor.getValue();
            assertThat(sauvegarde.getEntreprise().getId()).isEqualTo(ENTREPRISE_ID);
            assertThat(sauvegarde.getNom()).isEqualTo("Martin");
            assertThat(sauvegarde.getSupprime()).isFalse();
        }

        @ParameterizedTest(name = "❌ 403 — rôle {0}")
        @ValueSource(strings = {"CAISSIER", "RESP_ACHATS", "GESTIONNAIRE_STOCK"})
        @DisplayName("❌ 403 SEC_001 — rôle hors périmètre commercial, avant toute I/O")
        void shouldThrowWhenRoleNonAutorise(String role) {
            StockMasterPrincipal acteur = principal(role, GROUP_ID, ENTREPRISE_ID);
            CreerClientRequest requete = requeteValide();

            assertThatThrownBy(() -> clientService.creer(acteur, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_ACCESS_DENIED);

            verify(entrepriseRepository, never()).findById(any());
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("❌ 403 SEC_001 — principal sans rôle")
        void shouldThrowWhenRoleAbsent() {
            StockMasterPrincipal acteur = principal(null, GROUP_ID, ENTREPRISE_ID);
            CreerClientRequest requete = requeteValide();

            assertThatThrownBy(() -> clientService.creer(acteur, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_ACCESS_DENIED);
        }

        @Test
        @DisplayName("✅ le client reste sur le tenant du JWT même si le payload force entrepriseId")
        void shouldIgnorerEntrepriseIdForceDansLePayload() throws Exception {
            String corps = "{\"nom\":\"Martin\","
                    + "\"telephone\":\"+237691234567\","
                    + "\"entrepriseId\":" + ENTREPRISE_ID_AUTRE_TENANT + "}";
            CreerClientRequest request =
                    objectMapper.readValue(corps, CreerClientRequest.class);

            Set<String> champs = Arrays.stream(CreerClientRequest.class.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());
            assertThat(champs).isNotEmpty()
                    .doesNotContain("entrepriseId", "entreprise", "groupId");

            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            stubSauvegarde();

            clientService.creer(principalCommercial(), request);

            verify(clientRepository).save(clientCaptor.capture());
            assertThat(clientCaptor.getValue().getEntreprise().getId())
                    .isEqualTo(ENTREPRISE_ID);
            verify(entrepriseRepository, never()).findById(ENTREPRISE_ID_AUTRE_TENANT);
        }

        @Test
        @DisplayName("❌ 404 RES_001 — entreprise du JWT hors du groupe du JWT")
        void shouldThrow404WhenEntrepriseHorsGroupe() {
            Entreprise horsGroupe = entrepriseA();
            horsGroupe.getGroupe().setId(999L);
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(horsGroupe));

            StockMasterPrincipal acteur = principalCommercial();
            CreerClientRequest requete = requeteValide();

            assertThatThrownBy(() -> clientService.creer(acteur, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — entreprise du JWT introuvable")
        void shouldThrow404WhenEntrepriseIntrouvable() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.empty());

            StockMasterPrincipal acteur = principalCommercial();
            CreerClientRequest requete = requeteValide();

            assertThatThrownBy(() -> clientService.creer(acteur, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — entreprise soft-supprimée")
        void shouldThrow404WhenEntrepriseSupprimee() {
            Entreprise supprimee = entrepriseA();
            supprimee.setSupprime(true);
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(supprimee));

            StockMasterPrincipal acteur = principalCommercial();
            CreerClientRequest requete = requeteValide();

            assertThatThrownBy(() -> clientService.creer(acteur, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — principal sans groupId (JWT incomplet)")
        void shouldThrow403WhenPrincipalSansGroupId() {
            StockMasterPrincipal acteur = principal("COMMERCIAL", null, ENTREPRISE_ID);
            CreerClientRequest requete = requeteValide();

            assertThatThrownBy(() -> clientService.creer(acteur, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);

            verify(entrepriseRepository, never()).findById(any());
        }

        @Test
        @DisplayName("❌ 403 SEC_001 — principal sans entrepriseId (JWT incomplet)")
        void shouldThrow403WhenPrincipalSansEntrepriseId() {
            StockMasterPrincipal acteur = principal("COMMERCIAL", GROUP_ID, null);
            CreerClientRequest requete = requeteValide();

            assertThatThrownBy(() -> clientService.creer(acteur, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_ACCESS_DENIED);

            verify(entrepriseRepository, never()).findById(any());
        }

        @Test
        @DisplayName("❌ 400 — nom vide ou en blanc")
        void shouldRefuserNomVide() {
            CreerClientRequest r = requeteValide();
            r.setNom("   ");

            assertThat(champsEnErreur(r)).contains("nom");
        }

        @Test
        @DisplayName("❌ 400 — nom > 100 caractères (colonne V1)")
        void shouldRefuserNomTropLong() {
            CreerClientRequest r = requeteValide();
            r.setNom("x".repeat(101));

            assertThat(champsEnErreur(r)).contains("nom");
        }

        @Test
        @DisplayName("❌ 400 — téléphone hors format E.164")
        void shouldRefuserTelephoneHorsFormat() {
            CreerClientRequest r = requeteValide();
            r.setTelephone("691234567");

            assertThat(champsEnErreur(r)).contains("telephone");
        }

        @Test
        @DisplayName("✅ téléphone absent (optionnel) → aucune violation")
        void shouldAccepterTelephoneAbsent() {
            CreerClientRequest r = requeteValide();
            r.setTelephone(null);

            assertThat(champsEnErreur(r)).isEmpty();
        }

        @Test
        @DisplayName("❌ 400 — email mal formé")
        void shouldRefuserEmailInvalide() {
            CreerClientRequest r = requeteValide();
            r.setEmail("pas-un-email");

            assertThat(champsEnErreur(r)).contains("email");
        }
    }

    // ─── US-041 — lister / rechercher ────────────────────────────────────────

    @Nested
    @DisplayName("US-041 — lister / rechercher (GET /api/v1/clients)")
    class Lister {

        private Client client(long id, String nom, String prenom, String telephone) {
            Client c = Client.builder()
                    .nom(nom)
                    .prenom(prenom)
                    .telephone(telephone)
                    .entreprise(entrepriseA())
                    .build();
            c.setId(id);
            return c;
        }

        private Page<Client> pageDe(int numero, int taille, long total, Client... elements) {
            return new PageImpl<>(List.of(elements), PageRequest.of(numero, taille), total);
        }

        @Test
        @DisplayName("✅ 200 — recherche par nom/prénom/téléphone, tri nom ASC, tenant du JWT")
        void shouldListerClientsDuTenantQuandSearchPresent() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.rechercherParEntrepriseEtTexte(
                    eq(ENTREPRISE_ID), eq("martin"), any(Pageable.class)))
                    .thenReturn(pageDe(0, 20, 2,
                            client(1L, "Martin", "Alice", "+237691234567"),
                            client(2L, "Martin", "Paul", "+237677889900")));

            PageResponse<ClientResponse> reponse =
                    clientService.lister(principalCommercial(), "  martin  ", 0, 20);

            assertThat(reponse.getContent()).hasSize(2);
            assertThat(reponse.getPage()).isZero();
            assertThat(reponse.getSize()).isEqualTo(20);
            assertThat(reponse.getTotalElements()).isEqualTo(2);
            assertThat(reponse.getTotalPages()).isEqualTo(1);
            assertThat(reponse.getContent().get(0).getNom()).isEqualTo("Martin");
            assertThat(reponse.getContent().get(0).getPrenom()).isEqualTo("Alice");
            assertThat(reponse.getContent().get(0).getEntrepriseId()).isEqualTo(ENTREPRISE_ID);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(clientRepository).rechercherParEntrepriseEtTexte(
                    eq(ENTREPRISE_ID), eq("martin"), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageSize()).isEqualTo(20);
            assertThat(pageable.getSort().stream().map(Sort.Order::getProperty))
                    .containsExactly("nom");
            assertThat(pageable.getSort().getOrderFor("nom").getDirection())
                    .isEqualTo(Sort.Direction.ASC);
        }

        @ParameterizedTest(name = "✅ search [{0}] → listing complet paginé")
        @NullAndEmptySource
        @ValueSource(strings = "   ")
        @DisplayName("✅ 200 — search absent ou blanc → tous les clients du tenant")
        void shouldListerToutQuandSearchVide(String search) {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findAllByEntrepriseIdAndSupprimeFalse(
                    eq(ENTREPRISE_ID), any(Pageable.class)))
                    .thenReturn(pageDe(0, 20, 1,
                            client(1L, "Martin", "Alice", "+237691234567")));

            PageResponse<ClientResponse> reponse =
                    clientService.lister(principalCommercial(), search, 0, 20);

            assertThat(reponse.getContent()).hasSize(1);
            assertThat(reponse.getTotalElements()).isEqualTo(1);
            verify(clientRepository).findAllByEntrepriseIdAndSupprimeFalse(
                    eq(ENTREPRISE_ID), any(Pageable.class));
            verify(clientRepository, never())
                    .rechercherParEntrepriseEtTexte(any(), any(), any());
        }

        @Test
        @DisplayName("✅ 200 — recherche sans résultat → page vide, jamais 404")
        void shouldRetournerPageVideQuandAucunResultat() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.rechercherParEntrepriseEtTexte(
                    eq(ENTREPRISE_ID), eq("zzz"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            PageResponse<ClientResponse> reponse =
                    clientService.lister(principalCommercial(), "zzz", 0, 20);

            assertThat(reponse.getContent()).isEmpty();
            assertThat(reponse.getTotalElements()).isZero();
            assertThat(reponse.getTotalPages()).isZero();
        }

        @Test
        @DisplayName("✅ CAISSIER admis en lecture (arbitrage du 9 nov. 2026)")
        void shouldAutoriserCaissierEnLecture() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findAllByEntrepriseIdAndSupprimeFalse(
                    eq(ENTREPRISE_ID), any(Pageable.class)))
                    .thenReturn(pageDe(0, 20, 1,
                            client(1L, "Martin", "Alice", "+237691234567")));

            PageResponse<ClientResponse> reponse = clientService.lister(
                    principal("CAISSIER", GROUP_ID, ENTREPRISE_ID), null, 0, 20);

            assertThat(reponse.getContent()).hasSize(1);
        }

        @ParameterizedTest(name = "❌ 403 — rôle {0}")
        @ValueSource(strings = {"RESP_ACHATS", "GESTIONNAIRE_STOCK"})
        @DisplayName("❌ 403 SEC_001 — lecture hors périmètre commercial")
        void shouldRefuserLectureWhenRoleNonAutorise(String role) {
            StockMasterPrincipal acteur = principal(role, GROUP_ID, ENTREPRISE_ID);

            assertThatThrownBy(() -> clientService.lister(acteur, "martin", 0, 20))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_ACCESS_DENIED);

            verify(clientRepository, never()).rechercherParEntrepriseEtTexte(any(), any(), any());
            verify(clientRepository, never()).findAllByEntrepriseIdAndSupprimeFalse(any(), any());
        }

        @Test
        @DisplayName("❌ 404 — un utilisateur du tenant B ne lit aucun client du tenant A")
        void shouldRefuserLectureQuandTenantEtranger() {
            Entreprise entrepriseB = entrepriseA();
            entrepriseB.setId(ENTREPRISE_ID_AUTRE_TENANT);
            entrepriseB.getGroupe().setId(999L);
            when(entrepriseRepository.findById(ENTREPRISE_ID_AUTRE_TENANT))
                    .thenReturn(Optional.of(entrepriseB));
            StockMasterPrincipal acteurB = principal("COMMERCIAL", GROUP_ID, ENTREPRISE_ID_AUTRE_TENANT);

            assertThatThrownBy(() -> clientService.lister(acteurB, "martin", 0, 20))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(clientRepository, never()).rechercherParEntrepriseEtTexte(any(), any(), any());
            verify(clientRepository, never()).findAllByEntrepriseIdAndSupprimeFalse(any(), any());
        }

        @Test
        @DisplayName("✅ bornes — page négative → 0, size 999 → plafonné, tri fixe nom ASC")
        void shouldBorerPagination() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findAllByEntrepriseIdAndSupprimeFalse(
                    any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            clientService.lister(principalCommercial(), null, -5, 999);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(clientRepository).findAllByEntrepriseIdAndSupprimeFalse(
                    eq(ENTREPRISE_ID), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(100);
            assertThat(pageable.getSort().getOrderFor("nom").getDirection())
                    .isEqualTo(Sort.Direction.ASC);
        }
    }

    // ─── US-042 — modifier un client ─────────────────────────────────────────

    @Nested
    @DisplayName("US-042 — modifier un client")
    class ModifierClient {

        private static final Long ID_CIBLE = 200L;

        private Client clientExistant() {
            Client c = new Client();
            c.setId(ID_CIBLE);
            c.setEntreprise(entrepriseA());
            c.setNom("Martin");
            c.setPrenom("Alice");
            c.setTelephone("+237691234567");
            c.setEmail("alice.martin@example.cm");
            c.setAdresseVille("Douala");
            c.setAdresseQuartier("Bonapriso");
            c.setSupprime(false);
            return c;
        }

        private void appliquer(ModifierClientRequest r, String champ, String valeur) {
            switch (champ) {
                case "nom" -> r.setNom(valeur);
                case "prenom" -> r.setPrenom(valeur);
                case "telephone" -> r.setTelephone(valeur);
                case "email" -> r.setEmail(valeur);
                case "adresseVille" -> r.setAdresseVille(valeur);
                case "adresseQuartier" -> r.setAdresseQuartier(valeur);
                default -> throw new IllegalArgumentException(champ);
            }
        }

        private String lire(Client c, String champ) {
            return switch (champ) {
                case "nom" -> c.getNom();
                case "prenom" -> c.getPrenom();
                case "telephone" -> c.getTelephone();
                case "email" -> c.getEmail();
                case "adresseVille" -> c.getAdresseVille();
                case "adresseQuartier" -> c.getAdresseQuartier();
                default -> throw new IllegalArgumentException(champ);
            };
        }

        private String lire(ClientResponse r, String champ) {
            return switch (champ) {
                case "nom" -> r.getNom();
                case "prenom" -> r.getPrenom();
                case "telephone" -> r.getTelephone();
                case "email" -> r.getEmail();
                case "adresseVille" -> r.getAdresseVille();
                case "adresseQuartier" -> r.getAdresseQuartier();
                default -> throw new IllegalArgumentException(champ);
            };
        }

        private String valeurNeuve(String champ) {
            return switch (champ) {
                case "nom" -> "Martin Ngo";
                case "prenom" -> "Alicia";
                case "telephone" -> "+237677445566";
                case "email" -> "alicia.martin@example.cm";
                case "adresseVille" -> "Yaounde";
                case "adresseQuartier" -> "Bastos";
                default -> throw new IllegalArgumentException(champ);
            };
        }

        @ParameterizedTest(name = "✅ 200 — seul [{0}] change, les 5 autres champs sont intacts")
        @ValueSource(strings = {"nom", "prenom", "telephone", "email", "adresseVille", "adresseQuartier"})
        @DisplayName("✅ 200 — mise à jour champ par champ (PATCH sémantique), entreprise_id intact")
        void shouldModifierChaqueChampModifiableSansToucherAuxAutres(String champ) {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findByIdAndSupprimeFalse(ID_CIBLE))
                    .thenReturn(Optional.of(clientExistant()));
            when(clientRepository.save(any(Client.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ModifierClientRequest requete = new ModifierClientRequest();
            appliquer(requete, champ, valeurNeuve(champ));

            ClientResponse reponse = clientService.modifier(principalCommercial(), ID_CIBLE, requete);

            verify(clientRepository).save(clientCaptor.capture());
            Client sauvegarde = clientCaptor.getValue();
            assertThat(lire(sauvegarde, champ)).isEqualTo(valeurNeuve(champ));
            assertThat(lire(reponse, champ)).isEqualTo(valeurNeuve(champ));
            assertThat(reponse.getId()).isEqualTo(ID_CIBLE);
            assertThat(reponse.getEntrepriseId()).isEqualTo(ENTREPRISE_ID);
            assertThat(sauvegarde.getEntreprise().getId()).isEqualTo(ENTREPRISE_ID);

            String champTemoin = "prenom".equals(champ) ? "email" : "prenom";
            assertThat(lire(sauvegarde, champTemoin)).isEqualTo(
                    lire(clientExistant(), champTemoin));
        }

        @Test
        @DisplayName("✅ 200 — payload vide (tout null) = aucune modification, aucune exception")
        void shouldNeRienModifierQuandPayloadVide() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findByIdAndSupprimeFalse(ID_CIBLE))
                    .thenReturn(Optional.of(clientExistant()));
            when(clientRepository.save(any(Client.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ClientResponse reponse = clientService.modifier(
                    principalCommercial(), ID_CIBLE, new ModifierClientRequest());

            assertThat(reponse.getNom()).isEqualTo("Martin");
            assertThat(reponse.getTelephone()).isEqualTo("+237691234567");
            assertThat(reponse.getAdresseVille()).isEqualTo("Douala");
        }

        @ParameterizedTest(name = "✅ 200 — nom [{0}] ignoré (blanc = inchangé)")
        @ValueSource(strings = {"", "   "})
        @DisplayName("✅ 200 — nom blanc = inchangé, jamais écrasé à null")
        void shouldIgnorerNomBlanc(String blanc) {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findByIdAndSupprimeFalse(ID_CIBLE))
                    .thenReturn(Optional.of(clientExistant()));
            when(clientRepository.save(any(Client.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ModifierClientRequest requete = new ModifierClientRequest();
            requete.setNom(blanc);

            ClientResponse reponse = clientService.modifier(principalCommercial(), ID_CIBLE, requete);

            assertThat(reponse.getNom()).isEqualTo("Martin");
        }

        @ParameterizedTest(name = "❌ 403 — rôle {0}")
        @ValueSource(strings = {"CAISSIER", "RESP_ACHATS", "GESTIONNAIRE_STOCK"})
        @DisplayName("❌ 403 SEC_001 — rôle hors périmètre commercial, avant toute I/O")
        void shouldRefuserQuandRoleNonAutorise(String role) {
            StockMasterPrincipal acteur = principal(role, GROUP_ID, ENTREPRISE_ID);
            ModifierClientRequest requete = new ModifierClientRequest();
            requete.setAdresseVille("Yaounde");

            assertThatThrownBy(() -> clientService.modifier(acteur, ID_CIBLE, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_ACCESS_DENIED);

            verify(entrepriseRepository, never()).findById(any());
            verify(clientRepository, never()).findByIdAndSupprimeFalse(any());
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — id inexistant, rien n'est écrit")
        void shouldRetourner404QuandIdInexistant() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findByIdAndSupprimeFalse(9999L)).thenReturn(Optional.empty());
            ModifierClientRequest requete = new ModifierClientRequest();
            requete.setAdresseVille("Yaounde");

            assertThatThrownBy(() -> clientService.modifier(principalCommercial(), 9999L, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — client soft-supprimé : indistinguable d'un id inconnu")
        void shouldRetourner404QuandClientSoftSupprime() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findByIdAndSupprimeFalse(ID_CIBLE)).thenReturn(Optional.empty());
            ModifierClientRequest requete = new ModifierClientRequest();
            requete.setAdresseVille("Yaounde");

            assertThatThrownBy(() -> clientService.modifier(principalCommercial(), ID_CIBLE, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — IDOR : tenant A sur un client du tenant B, aucune écriture")
        void shouldRefuserModificationInterTenantSansRienModifier() {
            Entreprise entrepriseB = new Entreprise();
            entrepriseB.setId(ENTREPRISE_ID_AUTRE_TENANT);
            TenantGroup groupeB = new TenantGroup();
            groupeB.setId(777L);
            entrepriseB.setGroupe(groupeB);

            Client clientDeB = clientExistant();
            clientDeB.setEntreprise(entrepriseB);
            clientDeB.setNom("Client du tenant B");

            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findByIdAndSupprimeFalse(ID_CIBLE))
                    .thenReturn(Optional.of(clientDeB));
            ModifierClientRequest requete = new ModifierClientRequest();
            requete.setNom("Piratee");

            assertThatThrownBy(() -> clientService.modifier(principalCommercial(), ID_CIBLE, requete))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(clientRepository, never()).save(any(Client.class));
            assertThat(clientDeB.getNom()).isEqualTo("Client du tenant B");
        }

        @Test
        @DisplayName("✅ le DTO ne porte aucun champ tenant (entreprise_id non modifiable)")
        void shouldNePasExposerDeChampTenant() {
            Set<String> champs = Arrays.stream(ModifierClientRequest.class.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());

            assertThat(champs).containsExactlyInAnyOrder(
                    "nom", "prenom", "telephone", "email", "adresseVille", "adresseQuartier");
            assertThat(champs).doesNotContain("entrepriseId", "entreprise", "groupId", "id", "supprime");
        }

        @Test
        @DisplayName("❌ 400 SYS_005 (validation) — nom blanc refusé par le DTO")
        void shouldRefuserNomBlancALaValidation() {
            ModifierClientRequest requete = new ModifierClientRequest();
            requete.setNom("   ");

            assertThat(champsEnErreur(requete)).containsExactly("nom");
        }

        @Test
        @DisplayName("❌ 400 SYS_005 (validation) — téléphone hors format E.164")
        void shouldRefuserTelephoneInvalideALaValidation() {
            ModifierClientRequest requete = new ModifierClientRequest();
            requete.setTelephone("691234567");

            assertThat(champsEnErreur(requete)).containsExactly("telephone");
        }

        @Test
        @DisplayName("✅ validation — payload vide valide (PATCH : tous les champs optionnels)")
        void shouldAccepterPayloadVideALaValidation() {
            assertThat(champsEnErreur(new ModifierClientRequest())).isEmpty();
        }
    }

    // ─── US-043 — supprimer (archiver) un client ─────────────────────────────

    @Nested
    @DisplayName("US-043 — supprimer (archiver) un client")
    class SupprimerClient {

        private static final Long ID_CIBLE = 400L;

        private Client clientActif() {
            Client c = new Client();
            c.setId(ID_CIBLE);
            c.setEntreprise(entrepriseA());
            c.setNom("Martin");
            c.setPrenom("Alice");
            c.setTelephone("+237691234567");
            c.setSupprime(false);
            return c;
        }

        @Test
        @DisplayName("✅ 200 — aucune commande : supprime=true, ligne conservée en base")
        void shouldArchiverQuandAucuneCommande() {
            Client existant = clientActif();
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findByIdAndSupprimeFalse(ID_CIBLE))
                    .thenReturn(Optional.of(existant));
            when(commandesClientPort.aDesCommandes(ID_CIBLE)).thenReturn(false);
            when(clientRepository.save(any(Client.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ClientResponse reponse = clientService.supprimer(principalCommercial(), ID_CIBLE);

            assertThat(reponse.getId()).isEqualTo(ID_CIBLE);
            assertThat(reponse.getSupprime()).isTrue();
            assertThat(reponse.getNom()).isEqualTo("Martin");
            assertThat(reponse.getEntrepriseId()).isEqualTo(ENTREPRISE_ID);

            verify(clientRepository).save(clientCaptor.capture());
            assertThat(clientCaptor.getValue().getSupprime()).isTrue();
            verify(clientRepository, never()).delete(any(Client.class));
            verify(clientRepository, never()).deleteById(any());
            verify(clientRepository, never()).deleteAll();
        }

        @Test
        @DisplayName("❌ 409 RES_005 — historique de commandes (même annulée) : rien n'est modifié")
        void shouldRefuserQuandCommandeExistante() {
            Client existant = clientActif();
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findByIdAndSupprimeFalse(ID_CIBLE))
                    .thenReturn(Optional.of(existant));
            when(commandesClientPort.aDesCommandes(ID_CIBLE)).thenReturn(true);

            assertThatThrownBy(() -> clientService.supprimer(principalCommercial(), ID_CIBLE))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_HAS_DEPENDENCIES);

            verify(clientRepository, never()).save(any(Client.class));
            assertThat(existant.getSupprime()).isFalse();
        }

        @ParameterizedTest(name = "❌ 403 — rôle {0}")
        @ValueSource(strings = {"CAISSIER", "RESP_ACHATS", "GESTIONNAIRE_STOCK"})
        @DisplayName("❌ 403 SEC_001 — rôle hors périmètre commercial, avant toute I/O")
        void shouldRefuserQuandRoleNonAutorise(String role) {
            StockMasterPrincipal acteur = principal(role, GROUP_ID, ENTREPRISE_ID);

            assertThatThrownBy(() -> clientService.supprimer(acteur, ID_CIBLE))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEC_ACCESS_DENIED);

            verify(entrepriseRepository, never()).findById(any());
            verify(clientRepository, never()).findByIdAndSupprimeFalse(any());
            verify(commandesClientPort, never()).aDesCommandes(any());
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — id inexistant, aucune dépendance n'est même interrogée")
        void shouldRetourner404QuandIdInexistant() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findByIdAndSupprimeFalse(9999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.supprimer(principalCommercial(), 9999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(commandesClientPort, never()).aDesCommandes(any());
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — déjà archivé : idempotence, indistinguable d'un id inconnu")
        void shouldRetourner404QuandDejaArchive() {
            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findByIdAndSupprimeFalse(ID_CIBLE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.supprimer(principalCommercial(), ID_CIBLE))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — IDOR : tenant A sur un client du tenant B, rien n'est écrit")
        void shouldRefuserSuppressionInterTenant() {
            Entreprise entrepriseB = new Entreprise();
            entrepriseB.setId(ENTREPRISE_ID_AUTRE_TENANT);
            TenantGroup groupeB = new TenantGroup();
            groupeB.setId(777L);
            entrepriseB.setGroupe(groupeB);

            Client clientDeB = clientActif();
            clientDeB.setEntreprise(entrepriseB);

            when(entrepriseRepository.findById(ENTREPRISE_ID)).thenReturn(Optional.of(entrepriseA()));
            when(clientRepository.findByIdAndSupprimeFalse(ID_CIBLE))
                    .thenReturn(Optional.of(clientDeB));

            assertThatThrownBy(() -> clientService.supprimer(principalCommercial(), ID_CIBLE))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(commandesClientPort, never()).aDesCommandes(any());
            verify(clientRepository, never()).save(any(Client.class));
            assertThat(clientDeB.getSupprime()).isFalse();
        }

        @Test
        @DisplayName("❌ 404 RES_001 — entreprise du JWT hors groupe : refus avant lecture")
        void shouldRefuserQuandEntrepriseHorsGroupe() {
            Entreprise entrepriseAutreGroupe = entrepriseA();
            entrepriseAutreGroupe.getGroupe().setId(999L);
            when(entrepriseRepository.findById(ENTREPRISE_ID))
                    .thenReturn(Optional.of(entrepriseAutreGroupe));

            assertThatThrownBy(() -> clientService.supprimer(principalCommercial(), ID_CIBLE))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);

            verify(clientRepository, never()).findByIdAndSupprimeFalse(any());
            verify(clientRepository, never()).save(any(Client.class));
        }
    }
}
