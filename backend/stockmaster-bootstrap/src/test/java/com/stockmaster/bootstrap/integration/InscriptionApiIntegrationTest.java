package com.stockmaster.bootstrap.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration de l'inscription (US-006 mono-boutique, US-007 groupe)
 * sur PostgreSQL réel.
 *
 * <p>Le bug #1237 (500 {@code SYS_001} à l'inscription) ne pouvait pas être vu
 * par les tests unitaires : {@code AuthMapper} est mocké, donc aucun INSERT
 * n'atteint la contrainte {@code code_filiale NOT NULL} posée par
 * {@code V5__schema_decisionnel.sql} (A-10 / DEC-015). Ces tests traversent
 * HTTP → service → MapStruct → PostgreSQL, puis relisent la ligne créée.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("🧪 Intégration — inscription (US-006/US-007), PostgreSQL réel")
class InscriptionApiIntegrationTest extends ApiIntegrationTestSupport {

    private final List<Long> groupesCrees = new ArrayList<>();

    @AfterAll
    void nettoyerLaBase() {
        String marques = entreprisesCreees.stream().map(id -> "?").collect(Collectors.joining(", "));
        if (!marques.isEmpty()) {
            Object[] ids = entreprisesCreees.toArray();
            jdbcTemplate.update("DELETE FROM utilisateur WHERE entreprise_id IN (" + marques + ")", ids);
            jdbcTemplate.update("DELETE FROM entreprise WHERE id IN (" + marques + ")", ids);
        }
        if (!groupesCrees.isEmpty()) {
            String groupes = groupesCrees.stream().map(id -> "?").collect(Collectors.joining(", "));
            jdbcTemplate.update("DELETE FROM tenant_group WHERE id IN (" + groupes + ")", groupesCrees.toArray());
        }
    }

    @Test
    @DisplayName("#1237 — entreprise unique : 201 et code_filiale=SIEGE persisté pour la maison mère")
    void entrepriseUniquePersisteCodeFilialeSiege() throws Exception {
        String suffixe = suffixe();
        String email = "it-inscription-unique-" + suffixe + "@stockmaster.test";

        Map<String, Object> inscription = new HashMap<>();
        inscription.put("nomEntreprise", "IT Inscription " + suffixe);
        inscription.put("email", email);
        inscription.put("telephone", "+2376" + chiffres(8));
        inscription.put("adminNom", "Admin");
        inscription.put("adminPrenom", "Integration");
        inscription.put("motDePasse", MOT_DE_PASSE);

        ResponseEntity<String> reponse = restTemplate.postForEntity(
                "/api/v1/auth/inscription/entreprise-unique", json(inscription, null), String.class);
        assertThat(reponse.getStatusCode())
                .as("Inscription entreprise unique : %s", reponse.getBody())
                .isEqualTo(HttpStatus.CREATED);

        Long entrepriseId = jdbcTemplate.queryForObject(
                "SELECT id FROM entreprise WHERE email = ?", Long.class, email);
        entreprisesCreees.add(entrepriseId);

        Map<String, Object> ligne = jdbcTemplate.queryForMap(
                "SELECT type_entreprise, code_filiale, site_operationnel, group_id FROM entreprise WHERE id = ?",
                entrepriseId);
        assertThat(ligne.get("type_entreprise")).isEqualTo("MERE");
        assertThat(ligne.get("code_filiale"))
                .as("A-10/DEC-015 — le siège doit porter SIEGE, sinon l'INSERT viole NOT NULL")
                .isEqualTo("SIEGE");
        assertThat(ligne.get("site_operationnel")).isEqualTo(true);
        assertThat(ligne.get("group_id")).isNotNull();
        groupesCrees.add(((Number) ligne.get("group_id")).longValue());
    }

    @Test
    @DisplayName("#1237 — inscription groupe : 201, siège SIEGE persisté et admin connectable")
    void inscriptionGroupePersisteCodeFilialeSiege() throws Exception {
        String suffixe = suffixe();
        String emailEntreprise = "it-inscription-groupe-" + suffixe + "@stockmaster.test";
        String emailAdmin = "it-inscription-groupe-admin-" + suffixe + "@stockmaster.test";

        Map<String, Object> inscription = new HashMap<>();
        inscription.put("nomGroupe", "IT Groupe " + suffixe);
        inscription.put("villeSiege", "Douala");
        inscription.put("telephone", "+2376" + chiffres(8));
        inscription.put("emailEntreprise", emailEntreprise);
        inscription.put("nom", "Admin");
        inscription.put("prenom", "Integration");
        inscription.put("emailAdmin", emailAdmin);
        inscription.put("motDePasse", MOT_DE_PASSE);

        ResponseEntity<String> reponse = restTemplate.postForEntity(
                "/api/v1/auth/inscription/groupe", json(inscription, null), String.class);
        assertThat(reponse.getStatusCode())
                .as("Inscription groupe : %s", reponse.getBody())
                .isEqualTo(HttpStatus.CREATED);

        Long entrepriseId = jdbcTemplate.queryForObject(
                "SELECT id FROM entreprise WHERE email = ?", Long.class, emailEntreprise);
        entreprisesCreees.add(entrepriseId);

        Map<String, Object> ligne = jdbcTemplate.queryForMap(
                "SELECT type_entreprise, code_filiale, group_id FROM entreprise WHERE id = ?", entrepriseId);
        assertThat(ligne.get("type_entreprise")).isEqualTo("MERE");
        assertThat(ligne.get("code_filiale")).isEqualTo("SIEGE");
        groupesCrees.add(((Number) ligne.get("group_id")).longValue());

        ResponseEntity<String> login = restTemplate.postForEntity(
                "/api/v1/auth/login",
                json(Map.of("email", emailAdmin, "motDePasse", MOT_DE_PASSE), null),
                String.class);
        assertThat(login.getStatusCode()).as("Login admin : %s", login.getBody()).isEqualTo(HttpStatus.OK);
        JsonNode donnees = data(login);
        assertThat(donnees.path("utilisateur").path("entrepriseId").asLong()).isEqualTo(entrepriseId);
    }

    @Test
    @DisplayName("#1237 — aucune entreprise sans code_filiale en base")
    void aucunCodeFilialeNullEnBase() {
        Integer nulls = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM entreprise WHERE code_filiale IS NULL", Integer.class);
        assertThat(nulls).isZero();
    }
}
