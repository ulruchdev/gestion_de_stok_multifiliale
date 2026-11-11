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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration du CRUD fournisseurs (US-036 → US-039) sur PostgreSQL réel.
 *
 * <p>Les tests unitaires de {@code FournisseurServiceTest} mockent le repository
 * et le port {@code CommandesFournisseurPort} : ils ne prouvent ni le scoping
 * {@code entreprise_id} du JPQL, ni l'état réellement écrit en base, ni la
 * lecture des états de {@code commande_fournisseur} par l'adaptateur
 * {@code achat}. Ce test traverse HTTP → filtre JWT → {@code @PreAuthorize} →
 * service → repository/port → PostgreSQL, puis relit les tables.</p>
 *
 * <p><b>Règle US-039 propre aux fournisseurs</b> (contrairement aux clients) :
 * seules les commandes <b>actives</b> ({@code COMMANDEE},
 * {@code PARTIELLEMENT_RECUE}) bloquent l'archivage ; une commande
 * {@code ANNULEE} ou {@code RECEPTIONNEE} ne l'empêche pas. Les deux cas sont
 * vérifiés contre le schéma réel (contrainte {@code chk_etat_commande_fournisseur}).</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("🧪 Intégration — CRUD fournisseurs (US-036 → US-039), PostgreSQL réel")
class FournisseurApiIntegrationTest extends ApiIntegrationTestSupport {

    private Tenant tenantA;
    private Tenant tenantB;

    @BeforeAll
    void creerDeuxTenants() throws Exception {
        tenantA = inscrireEtConnecter("Fournisseurs A");
        tenantB = inscrireEtConnecter("Fournisseurs B");
    }

    @AfterAll
    void nettoyerLaBase() {
        nettoyer("ligne_commande_fournisseur", "commande_fournisseur", "fournisseur", "utilisateur");
    }

    // ─── US-036 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("US-036 — validation puis création : la fiche est persistée dans le tenant du JWT")
    void creerAvecValidation() throws Exception {
        // CA3 — raisonSociale obligatoire : 400 SYS_005, rien n'est écrit.
        ResponseEntity<String> sansNom = echanger(HttpMethod.POST, "/api/v1/fournisseurs",
                Map.of("telephone", "+23767700" + chiffres(4)), tenantA);
        assertThat(sansNom.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(erreurCode(sansNom)).isEqualTo("SYS_005");

        // CA3 — téléphone hors format E.164 : refusé lui aussi.
        ResponseEntity<String> telephoneInvalide = echanger(HttpMethod.POST, "/api/v1/fournisseurs",
                Map.of("raisonSociale", "Invalide" + suffixe(), "telephone", "677-00-00"), tenantA);
        assertThat(telephoneInvalide.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(erreurCode(telephoneInvalide)).isEqualTo("SYS_005");

        String suffixe = suffixe();
        String raisonSociale = "Ets Njangi" + suffixe;
        String nif = "NIF-IT-" + suffixe;
        ResponseEntity<String> creation = echanger(HttpMethod.POST, "/api/v1/fournisseurs",
                Map.of(
                        "raisonSociale", raisonSociale,
                        "nif", nif,
                        "contact", "Mme Fouda",
                        "telephone", "+23767800" + chiffres(4),
                        "email", "fournisseur-" + suffixe + "@stockmaster.test",
                        "adresseVille", "Douala"
                ), tenantA);

        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode fournisseur = data(creation);
        long fournisseurId = fournisseur.path("id").asLong();

        assertThat(fournisseurId).isPositive();
        assertThat(fournisseur.path("entrepriseId").asLong()).isEqualTo(tenantA.entrepriseId());
        assertThat(fournisseur.path("supprime").asBoolean()).isFalse();
        assertThat(fournisseur.path("raisonSociale").asText()).isEqualTo(raisonSociale);

        // État réel en base : une ligne, bon tenant, non archivée.
        Map<String, Object> ligne = jdbcTemplate.queryForMap(
                "SELECT entreprise_id, raison_sociale, nif, supprime FROM fournisseur WHERE id = ?",
                fournisseurId);
        assertThat(((Number) ligne.get("entreprise_id")).longValue()).isEqualTo(tenantA.entrepriseId());
        assertThat(ligne.get("raison_sociale")).isEqualTo(raisonSociale);
        assertThat(ligne.get("nif")).isEqualTo(nif);
        assertThat(ligne.get("supprime")).isEqualTo(false);
    }

    // ─── US-037 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("US-037 — recherche par nom et par ville, insensible à la casse et scopée au tenant")
    void rechercherParNomEtVille() throws Exception {
        String suffixe = suffixe();
        String raisonSociale = "Quincaillerie " + suffixe;
        String ville = "Bafouss" + suffixe.substring(0, 6);

        ResponseEntity<String> creation = echanger(HttpMethod.POST, "/api/v1/fournisseurs",
                Map.of("raisonSociale", raisonSociale, "adresseVille", ville,
                        "telephone", "+23767900" + chiffres(4)),
                tenantA);
        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long fournisseurId = data(creation).path("id").asLong();

        // Par nom, en minuscules (LOWER ... LIKE).
        ResponseEntity<String> parNom = echanger(HttpMethod.GET,
                "/api/v1/fournisseurs?search=" + raisonSociale.toLowerCase() + "&page=0&size=10", null, tenantA);
        assertThat(parNom.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data(parNom).path("totalElements").asLong()).isEqualTo(1);
        assertThat(data(parNom).path("content").get(0).path("id").asLong()).isEqualTo(fournisseurId);

        // Par ville — le second critère du JPQL.
        ResponseEntity<String> parVille = echanger(HttpMethod.GET,
                "/api/v1/fournisseurs?search=" + ville.toLowerCase(), null, tenantA);
        assertThat(data(parVille).path("totalElements").asLong())
                .as("recherche par ville : %s", data(parVille))
                .isEqualTo(1);
        assertThat(data(parVille).path("content").get(0).path("id").asLong()).isEqualTo(fournisseurId);

        // Recherche sans résultat : page vide, pas d'erreur.
        ResponseEntity<String> aucun = echanger(HttpMethod.GET,
                "/api/v1/fournisseurs?search=INTROUVABLE" + suffixe, null, tenantA);
        assertThat(aucun.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data(aucun).path("totalElements").asLong()).isZero();
        assertThat(data(aucun).path("content")).isEmpty();
    }

    // ─── Isolation multi-tenant ──────────────────────────────────────────────

    @Test
    @DisplayName("Isolation tenant — un fournisseur du tenant A est invisible et non modifiable depuis le tenant B")
    void isolationEntreTenants() throws Exception {
        String suffixe = suffixe();
        String raisonSociale = "Confidentiel " + suffixe;

        ResponseEntity<String> creation = echanger(HttpMethod.POST, "/api/v1/fournisseurs",
                Map.of("raisonSociale", raisonSociale, "telephone", "+23768000" + chiffres(4)),
                tenantA);
        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long fournisseurId = data(creation).path("id").asLong();

        // B ne le voit pas...
        ResponseEntity<String> rechercheB = echanger(HttpMethod.GET,
                "/api/v1/fournisseurs?search=" + raisonSociale, null, tenantB);
        assertThat(rechercheB.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data(rechercheB).path("totalElements").asLong()).isZero();

        // ...ne peut ni le modifier, ni l'archiver : 404 uniforme, jamais 403.
        ResponseEntity<String> modificationB = echanger(HttpMethod.PUT,
                "/api/v1/fournisseurs/" + fournisseurId, Map.of("raisonSociale", "Pirate"), tenantB);
        assertThat(modificationB.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(erreurCode(modificationB)).isEqualTo("RES_001");

        ResponseEntity<String> suppressionB = echanger(HttpMethod.DELETE,
                "/api/v1/fournisseurs/" + fournisseurId, null, tenantB);
        assertThat(suppressionB.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(erreurCode(suppressionB)).isEqualTo("RES_001");

        // La ligne de A est intacte.
        Map<String, Object> ligne = jdbcTemplate.queryForMap(
                "SELECT entreprise_id, raison_sociale, supprime FROM fournisseur WHERE id = ?",
                fournisseurId);
        assertThat(((Number) ligne.get("entreprise_id")).longValue()).isEqualTo(tenantA.entrepriseId());
        assertThat(ligne.get("raison_sociale")).isEqualTo(raisonSociale);
        assertThat(ligne.get("supprime")).isEqualTo(false);
    }

    // ─── US-038 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("US-038 — la modification PATCH sémantique persiste les champs fournis et conserve les autres")
    void modifierPersisteEnBase() throws Exception {
        String suffixe = suffixe();
        String raisonSociale = "Transports " + suffixe;
        String telephone = "+23768100" + chiffres(4);

        ResponseEntity<String> creation = echanger(HttpMethod.POST, "/api/v1/fournisseurs",
                Map.of("raisonSociale", raisonSociale, "telephone", telephone,
                        "contact", "M. Essomba", "adresseVille", "Yaoundé"),
                tenantA);
        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long fournisseurId = data(creation).path("id").asLong();

        ResponseEntity<String> modification = echanger(HttpMethod.PUT,
                "/api/v1/fournisseurs/" + fournisseurId,
                Map.of("contact", "Mme Ngo", "email", "contact-" + suffixe + "@stockmaster.test"),
                tenantA);

        assertThat(modification.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode modifie = data(modification);
        assertThat(modifie.path("contact").asText()).isEqualTo("Mme Ngo");
        assertThat(modifie.path("email").asText()).isEqualTo("contact-" + suffixe + "@stockmaster.test");
        // Champs non fournis : conservés (PATCH sémantique).
        assertThat(modifie.path("raisonSociale").asText()).isEqualTo(raisonSociale);
        assertThat(modifie.path("telephone").asText()).isEqualTo(telephone);
        assertThat(modifie.path("adresseVille").asText()).isEqualTo("Yaoundé");

        Map<String, Object> ligne = jdbcTemplate.queryForMap(
                "SELECT contact, email, raison_sociale, telephone, adresse_ville "
                        + "FROM fournisseur WHERE id = ?", fournisseurId);
        assertThat(ligne.get("contact")).isEqualTo("Mme Ngo");
        assertThat(ligne.get("raison_sociale")).isEqualTo(raisonSociale);
        assertThat(ligne.get("telephone")).isEqualTo(telephone);
        assertThat(ligne.get("adresse_ville")).isEqualTo("Yaoundé");
    }

    // ─── US-039 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("US-039 — 409 RES_005 tant qu'une commande fournisseur est active (COMMANDEE)")
    void suppressionBloqueeParCommandeActive() throws Exception {
        String suffixe = suffixe();
        ResponseEntity<String> creation = echanger(HttpMethod.POST, "/api/v1/fournisseurs",
                Map.of("raisonSociale", "En attente " + suffixe, "telephone", "+23768200" + chiffres(4)),
                tenantA);
        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long fournisseurId = data(creation).path("id").asLong();

        // Commande active : la marchandise est encore attendue.
        jdbcTemplate.update("""
                INSERT INTO commande_fournisseur (entreprise_id, fournisseur_id, code, etat_commande)
                VALUES (?, ?, ?, 'COMMANDEE')
                """, tenantA.entrepriseId(), fournisseurId, "CMD-F-IT-" + suffixe);

        ResponseEntity<String> suppression = echanger(HttpMethod.DELETE,
                "/api/v1/fournisseurs/" + fournisseurId, null, tenantA);

        assertThat(suppression.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(erreurCode(suppression)).isEqualTo("RES_005");

        // Aucune écriture : la fiche reste active.
        Boolean supprime = jdbcTemplate.queryForObject(
                "SELECT supprime FROM fournisseur WHERE id = ?", Boolean.class, fournisseurId);
        assertThat(supprime).isFalse();
    }

    @Test
    @DisplayName("US-039 — une commande close (ANNULEE) n'empêche pas l'archivage logique")
    void suppressionAutoriseeAvecCommandeClose() throws Exception {
        String suffixe = suffixe();
        String raisonSociale = "AArchiver " + suffixe;
        ResponseEntity<String> creation = echanger(HttpMethod.POST, "/api/v1/fournisseurs",
                Map.of("raisonSociale", raisonSociale, "telephone", "+23768300" + chiffres(4)),
                tenantA);
        assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long fournisseurId = data(creation).path("id").asLong();

        // Commande ANNULEE : close, elle ne doit pas bloquer (contrairement aux clients).
        jdbcTemplate.update("""
                INSERT INTO commande_fournisseur (entreprise_id, fournisseur_id, code, etat_commande)
                VALUES (?, ?, ?, 'ANNULEE')
                """, tenantA.entrepriseId(), fournisseurId, "CMD-F-IT-" + suffixe);

        ResponseEntity<String> suppression = echanger(HttpMethod.DELETE,
                "/api/v1/fournisseurs/" + fournisseurId, null, tenantA);

        assertThat(suppression.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data(suppression).path("supprime").asBoolean()).isTrue();

        // Soft delete : la ligne et sa commande existent toujours.
        Boolean supprime = jdbcTemplate.queryForObject(
                "SELECT supprime FROM fournisseur WHERE id = ?", Boolean.class, fournisseurId);
        assertThat(supprime).isTrue();
        Integer commandes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM commande_fournisseur WHERE fournisseur_id = ?",
                Integer.class, fournisseurId);
        assertThat(commandes).isEqualTo(1);

        // La fiche disparaît des recherches et des listes.
        ResponseEntity<String> recherche = echanger(HttpMethod.GET,
                "/api/v1/fournisseurs?search=" + raisonSociale, null, tenantA);
        assertThat(data(recherche).path("totalElements").asLong()).isZero();

        ResponseEntity<String> liste = echanger(HttpMethod.GET,
                "/api/v1/fournisseurs?page=0&size=100", null, tenantA);
        assertThat(data(liste).path("content").findValuesAsText("id"))
                .doesNotContain(String.valueOf(fournisseurId));

        // Rejouer le DELETE ne révèle rien et ne réécrit rien : 404 RES_001.
        ResponseEntity<String> secondDelete = echanger(HttpMethod.DELETE,
                "/api/v1/fournisseurs/" + fournisseurId, null, tenantA);
        assertThat(secondDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(erreurCode(secondDelete)).isEqualTo("RES_001");
    }
}
