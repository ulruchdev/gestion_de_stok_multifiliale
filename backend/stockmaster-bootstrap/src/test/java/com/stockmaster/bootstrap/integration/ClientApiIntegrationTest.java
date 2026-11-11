package com.stockmaster.bootstrap.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration du CRUD clients (US-040 → US-043) sur le PostgreSQL réel.
 *
 * <p>Contrairement à {@code ClientServiceTest} / {@code ClientControllerTest}
 * (mocks), ce test traverse toute la pile : HTTP → filtre JWT →
 * {@code @PreAuthorize} → service → repository → PostgreSQL, puis relit la base
 * via {@code JdbcTemplate} pour vérifier l'état réellement persisté. Il valide
 * en particulier ce que les mocks ne peuvent pas prouver :</p>
 *
 * <ul>
 *   <li>l'isolation multi-tenant en base ({@code entreprise_id} du JWT) ;</li>
 *   <li>le scoping du JPQL de recherche et du soft delete ;</li>
 *   <li>le {@code 409 RES_005} du port {@code CommandesClientPort} lorsque la
 *       table {@code commande_client} contient une commande <b>annulée</b>.</li>
 * </ul>
 *
 * <p>Deux entreprises jetables (UUID) sont créées par {@link ApiIntegrationTestSupport}
 * et toutes leurs lignes sont supprimées en fin de classe.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("🧪 Intégration — CRUD clients (US-040 → US-043), PostgreSQL réel")
class ClientApiIntegrationTest extends ApiIntegrationTestSupport {

    private Tenant tenantA;
    private Tenant tenantB;

    @BeforeAll
    void creerDeuxTenants() throws Exception {
        tenantA = inscrireEtConnecter("Clients A");
        tenantB = inscrireEtConnecter("Clients B");
    }

    @AfterAll
    void nettoyerLaBase() {
        nettoyer("commande_client", "client", "utilisateur");
    }

    // ─── US-040 + US-041 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("US-040/US-041 — le client créé est persisté dans le tenant du JWT et retrouvé par la recherche")
    void creerPuisRechercher() throws Exception {
        String suffixe = suffixe();
        String nom = "Zebulon" + suffixe;
        String telephone = "+23769100" + chiffres(4);

        ResponseEntity<String> creation = echanger(HttpMethod.POST, "/api/v1/clients",
                Map.of(
                        "nom", nom,
                        "prenom", "Alice",
                        "telephone", telephone,
                        "email", "client-" + suffixe + "@stockmaster.test",
                        "adresseVille", "Douala",
                        "adresseQuartier", "Bonapriso"
                ), tenantA);

        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode client = data(creation);
        long clientId = client.path("id").asLong();

        assertThat(clientId).isPositive();
        assertThat(client.path("entrepriseId").asLong()).isEqualTo(tenantA.entrepriseId());
        assertThat(client.path("supprime").asBoolean()).isFalse();
        assertThat(client.path("nom").asText()).isEqualTo(nom);

        // État réel en base : une seule ligne, non supprimée, dans le bon tenant.
        Map<String, Object> ligne = jdbcTemplate.queryForMap(
                "SELECT entreprise_id, supprime, adresse_ville FROM client WHERE id = ?", clientId);
        assertThat(((Number) ligne.get("entreprise_id")).longValue()).isEqualTo(tenantA.entrepriseId());
        assertThat(ligne.get("supprime")).isEqualTo(false);
        assertThat(ligne.get("adresse_ville")).isEqualTo("Douala");

        // Recherche insensible à la casse sur le nom (LOWER ... LIKE), scopée tenant.
        ResponseEntity<String> recherche = echanger(HttpMethod.GET,
                "/api/v1/clients?search=" + nom.toLowerCase() + "&page=0&size=10", null, tenantA);

        assertThat(recherche.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode page = data(recherche);
        assertThat(page.path("totalElements").asLong()).isEqualTo(1);
        assertThat(page.path("content").get(0).path("id").asLong()).isEqualTo(clientId);
        assertThat(page.path("content").get(0).path("entrepriseId").asLong())
                .isEqualTo(tenantA.entrepriseId());
    }

    // ─── Isolation multi-tenant ──────────────────────────────────────────────

    @Test
    @DisplayName("Isolation tenant — un client du tenant A est invisible et non modifiable depuis le tenant B")
    void isolationEntreTenants() throws Exception {
        String suffixe = suffixe();
        String nom = "Confidentiel" + suffixe;

        ResponseEntity<String> creation = echanger(HttpMethod.POST, "/api/v1/clients",
                Map.of("nom", nom, "prenom", "Bob", "telephone", "+23769200" + chiffres(4)),
                tenantA);
        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long clientId = data(creation).path("id").asLong();

        // B ne voit pas le client dans sa recherche...
        ResponseEntity<String> rechercheB = echanger(HttpMethod.GET,
                "/api/v1/clients?search=" + nom, null, tenantB);
        assertThat(rechercheB.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data(rechercheB).path("totalElements").asLong()).isZero();
        assertThat(data(rechercheB).path("content")).isEmpty();

        // ...ne peut pas le modifier (404 uniforme, jamais 403)...
        ResponseEntity<String> modificationB = echanger(HttpMethod.PUT,
                "/api/v1/clients/" + clientId, Map.of("nom", "Pirate"), tenantB);
        assertThat(modificationB.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(erreurCode(modificationB)).isEqualTo("RES_001");

        // ...ni l'archiver.
        ResponseEntity<String> suppressionB = echanger(HttpMethod.DELETE,
                "/api/v1/clients/" + clientId, null, tenantB);
        assertThat(suppressionB.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(erreurCode(suppressionB)).isEqualTo("RES_001");

        // La ligne de A est intacte : bon tenant, nom d'origine, non supprimée.
        Map<String, Object> ligne = jdbcTemplate.queryForMap(
                "SELECT entreprise_id, nom, supprime FROM client WHERE id = ?", clientId);
        assertThat(((Number) ligne.get("entreprise_id")).longValue()).isEqualTo(tenantA.entrepriseId());
        assertThat(ligne.get("nom")).isEqualTo(nom);
        assertThat(ligne.get("supprime")).isEqualTo(false);

        // A, lui, retrouve bien son client.
        ResponseEntity<String> rechercheA = echanger(HttpMethod.GET,
                "/api/v1/clients?search=" + nom, null, tenantA);
        assertThat(data(rechercheA).path("totalElements").asLong()).isEqualTo(1);
    }

    // ─── RBAC — CAISSIER en lecture seule ───────────────────────────────────

    @Test
    @DisplayName("RBAC — un CAISSIER lit les clients mais ne peut pas en créer (403 SEC_001)")
    void caissierLectureSeule() throws Exception {
        String suffixe = suffixe();
        Tenant caissier = creerCaissier(tenantA, suffixe);

        // Le CAISSIER consulte la liste et la recherche : autorisé (comptoir).
        ResponseEntity<String> liste = echanger(HttpMethod.GET,
                "/api/v1/clients?page=0&size=10", null, caissier);
        assertThat(liste.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data(liste).path("content")).isNotNull();

        ResponseEntity<String> recherche = echanger(HttpMethod.GET,
                "/api/v1/clients?search=Caissier" + suffixe, null, caissier);
        assertThat(recherche.getStatusCode()).isEqualTo(HttpStatus.OK);

        // L'écriture est refusée par @PreAuthorize (rôle absent de la liste).
        String nom = "Interdit" + suffixe;
        ResponseEntity<String> creation = echanger(HttpMethod.POST, "/api/v1/clients",
                Map.of("nom", nom, "telephone", "+23769600" + chiffres(4)), caissier);

        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(erreurCode(creation)).isEqualTo("SEC_001");

        // Le refus intervient avant toute écriture : aucun client en base.
        Integer enBase = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM client WHERE nom = ?", Integer.class, nom);
        assertThat(enBase).isZero();
    }

    // ─── US-042 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("US-042 — la modification PATCH sémantique persiste les champs fournis et conserve les autres")
    void modifierPersisteEnBase() throws Exception {
        String suffixe = suffixe();
        String telephone = "+23769300" + chiffres(4);
        ResponseEntity<String> creation = echanger(HttpMethod.POST, "/api/v1/clients",
                Map.of(
                        "nom", "Mireille" + suffixe,
                        "prenom", "Mireille",
                        "telephone", telephone,
                        "adresseVille", "Yaoundé"
                ), tenantA);
        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long clientId = data(creation).path("id").asLong();

        ResponseEntity<String> modification = echanger(HttpMethod.PUT,
                "/api/v1/clients/" + clientId,
                Map.of("adresseVille", "Bafoussam", "email", "mireille-" + suffixe + "@stockmaster.test"),
                tenantA);

        assertThat(modification.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode modifie = data(modification);
        assertThat(modifie.path("adresseVille").asText()).isEqualTo("Bafoussam");
        assertThat(modifie.path("email").asText()).isEqualTo("mireille-" + suffixe + "@stockmaster.test");
        // Champs non fournis : conservés (PATCH sémantique).
        assertThat(modifie.path("telephone").asText()).isEqualTo(telephone);

        Map<String, Object> ligne = jdbcTemplate.queryForMap(
                "SELECT adresse_ville, telephone, nom FROM client WHERE id = ?", clientId);
        assertThat(ligne.get("adresse_ville")).isEqualTo("Bafoussam");
        assertThat(ligne.get("telephone")).isEqualTo(telephone);
        assertThat(ligne.get("nom")).isEqualTo("Mireille" + suffixe);
    }

    // ─── US-043 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("US-043 — 409 RES_005 si une commande existe, même ANNULEE, et la fiche reste active")
    void suppressionBloqueeParCommandeAnnulee() throws Exception {
        String suffixe = suffixe();
        ResponseEntity<String> creation = echanger(HttpMethod.POST, "/api/v1/clients",
                Map.of("nom", "Historique" + suffixe, "telephone", "+23769400" + chiffres(4)),
                tenantA);
        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long clientId = data(creation).path("id").asLong();

        // Commande client volontairement ANNULEE : elle doit quand même bloquer.
        jdbcTemplate.update("""
                INSERT INTO commande_client (entreprise_id, client_id, code, etat_commande, etat_reglement)
                VALUES (?, ?, ?, 'ANNULEE', 'NON_REGLEE')
                """, tenantA.entrepriseId(), clientId, "CMD-IT-" + suffixe);

        ResponseEntity<String> suppression = echanger(HttpMethod.DELETE,
                "/api/v1/clients/" + clientId, null, tenantA);

        assertThat(suppression.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(erreurCode(suppression)).isEqualTo("RES_005");

        // Aucune écriture : la fiche n'a pas été archivée.
        Boolean supprime = jdbcTemplate.queryForObject(
                "SELECT supprime FROM client WHERE id = ?", Boolean.class, clientId);
        assertThat(supprime).isFalse();
    }

    @Test
    @DisplayName("US-043 — le soft delete archive la ligne, la retire des recherches et renvoie supprime=true")
    void suppressionArchiveLogiquement() throws Exception {
        String suffixe = suffixe();
        String nom = "AArchiver" + suffixe;
        ResponseEntity<String> creation = echanger(HttpMethod.POST, "/api/v1/clients",
                Map.of("nom", nom, "telephone", "+23769500" + chiffres(4)),
                tenantA);
        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long clientId = data(creation).path("id").asLong();

        ResponseEntity<String> suppression = echanger(HttpMethod.DELETE,
                "/api/v1/clients/" + clientId, null, tenantA);

        assertThat(suppression.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data(suppression).path("supprime").asBoolean()).isTrue();

        // La ligne existe toujours (soft delete) mais marquée supprimée.
        Boolean supprime = jdbcTemplate.queryForObject(
                "SELECT supprime FROM client WHERE id = ?", Boolean.class, clientId);
        assertThat(supprime).isTrue();

        // Elle disparaît des recherches et des listes.
        ResponseEntity<String> recherche = echanger(HttpMethod.GET,
                "/api/v1/clients?search=" + nom, null, tenantA);
        assertThat(data(recherche).path("totalElements").asLong()).isZero();

        ResponseEntity<String> liste = echanger(HttpMethod.GET,
                "/api/v1/clients?page=0&size=100", null, tenantA);
        assertThat(data(liste).path("content").findValuesAsText("id"))
                .doesNotContain(String.valueOf(clientId));

        // Un second DELETE renvoie 404 (déjà archivé = id inconnu).
        ResponseEntity<String> secondDelete = echanger(HttpMethod.DELETE,
                "/api/v1/clients/" + clientId, null, tenantA);
        assertThat(secondDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(erreurCode(secondDelete)).isEqualTo("RES_001");
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    /** Crée un vrai compte CAISSIER (US-022) dans le tenant puis s'y connecte. */
    private Tenant creerCaissier(Tenant admin, String suffixe) throws Exception {
        String email = "caissier-" + suffixe + "@stockmaster.test";

        Map<String, Object> employe = new HashMap<>();
        employe.put("prenom", "Caissier");
        employe.put("nom", "Integration");
        employe.put("email", email);
        employe.put("role", "CAISSIER");
        employe.put("motDePasseProvisoire", MOT_DE_PASSE);

        ResponseEntity<String> creation = echanger(HttpMethod.POST,
                "/api/v1/utilisateurs/employes", employe, admin);
        assertThat(creation.getStatusCode())
                .as("Création CAISSIER : %s", creation.getBody())
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> login = restTemplate.postForEntity("/api/v1/auth/login",
                json(Map.of("email", email, "motDePasse", MOT_DE_PASSE), null), String.class);
        assertThat(login.getStatusCode())
                .as("Login CAISSIER : %s", login.getBody())
                .isEqualTo(HttpStatus.OK);

        JsonNode donnees = data(login);
        assertThat(donnees.path("role").asText()).isEqualTo("CAISSIER");
        assertThat(donnees.path("utilisateur").path("entrepriseId").asLong())
                .isEqualTo(admin.entrepriseId());

        return new Tenant(donnees.path("accessToken").asText(), admin.entrepriseId());
    }
}
