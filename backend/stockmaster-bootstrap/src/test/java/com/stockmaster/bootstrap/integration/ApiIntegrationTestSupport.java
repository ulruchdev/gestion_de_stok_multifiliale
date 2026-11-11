package com.stockmaster.bootstrap.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Socle commun des tests d'intégration HTTP métier.
 *
 * <p>Démarre l'application complète sur un port aléatoire (profil
 * {@code integration} : Flyway + {@code ddl-auto=validate}) et fournit :</p>
 *
 * <ul>
 *   <li>l'inscription réelle d'entreprises jetables
 *       ({@code POST /api/v1/auth/inscription/entreprise-unique}) suivie d'un
 *       login, pour obtenir de vrais JWT ;</li>
 *   <li>des helpers d'échange HTTP et de lecture d'enveloppe
 *       {@code ApiResponse} / {@code ProblemResponse} ;</li>
 *   <li>un nettoyage par {@code entreprise_id} en fin de classe.</li>
 * </ul>
 *
 * <p>Le rate limiting est désactivé pour ne pas consommer les quotas réels
 * (inscription 3/heure/IP, login 5/900 s) entre deux exécutions.</p>
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "springdoc.api-docs.enabled=false",
                "springdoc.swagger-ui.enabled=false",
                "stockmaster.rate-limiting.enabled=false"
        }
)
@ActiveProfiles("integration")
abstract class ApiIntegrationTestSupport {

    protected static final String MOT_DE_PASSE = "Integr@tion2026";

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    /** Entreprises créées par la classe de test, supprimées en fin de classe. */
    protected final List<Long> entreprisesCreees = new ArrayList<>();

    /** Inscrit une entreprise jetable puis se connecte — renvoie un JWT réel. */
    protected Tenant inscrireEtConnecter(String etiquette) throws Exception {
        String suffixe = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String slug = etiquette.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        String email = "it-" + slug + "-" + suffixe + "@stockmaster.test";

        Map<String, Object> inscription = new HashMap<>();
        inscription.put("nomEntreprise", "IT " + etiquette + " " + suffixe);
        inscription.put("email", email);
        inscription.put("telephone", "+2376" + chiffres(8));
        inscription.put("adminNom", "Admin");
        inscription.put("adminPrenom", "Integration");
        inscription.put("motDePasse", MOT_DE_PASSE);

        ResponseEntity<String> reponseInscription = restTemplate.postForEntity(
                "/api/v1/auth/inscription/entreprise-unique", json(inscription, null), String.class);
        assertThat(reponseInscription.getStatusCode())
                .as("Inscription %s : %s", etiquette, reponseInscription.getBody())
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> reponseLogin = restTemplate.postForEntity(
                "/api/v1/auth/login",
                json(Map.of("email", email, "motDePasse", MOT_DE_PASSE), null),
                String.class);
        assertThat(reponseLogin.getStatusCode())
                .as("Login %s : %s", etiquette, reponseLogin.getBody())
                .isEqualTo(HttpStatus.OK);

        JsonNode donnees = data(reponseLogin);
        long entrepriseId = donnees.path("utilisateur").path("entrepriseId").asLong();
        entreprisesCreees.add(entrepriseId);

        return new Tenant(donnees.path("accessToken").asText(), entrepriseId);
    }

    protected ResponseEntity<String> echanger(HttpMethod methode, String chemin,
                                              Object corps, Tenant tenant) {
        return restTemplate.exchange(chemin, methode, json(corps, tenant.token()), String.class);
    }

    protected HttpEntity<Object> json(Object corps, String token) {
        HttpHeaders entetes = new HttpHeaders();
        entetes.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            entetes.setBearerAuth(token);
        }
        return new HttpEntity<>(corps, entetes);
    }

    protected JsonNode data(ResponseEntity<String> reponse) throws Exception {
        return objectMapper.readTree(reponse.getBody()).path("data");
    }

    protected String erreurCode(ResponseEntity<String> reponse) throws Exception {
        return objectMapper.readTree(reponse.getBody()).path("errorCode").asText();
    }

    protected String suffixe() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    protected String chiffres(int longueur) {
        StringBuilder sb = new StringBuilder(longueur);
        for (int i = 0; i < longueur; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    /**
     * Supprime toutes les lignes des entreprises jetables, dans l'ordre des FK.
     *
     * @param tables tables portant une colonne {@code entreprise_id}, fournies
     *               dans l'ordre des dépendances (enfants avant parents) ;
     *               {@code entreprise} et {@code tenant_group} sont traitées à part
     */
    protected void nettoyer(String... tables) {
        if (entreprisesCreees.isEmpty()) {
            return;
        }
        String marques = entreprisesCreees.stream().map(id -> "?").collect(Collectors.joining(", "));
        Object[] ids = entreprisesCreees.toArray();

        for (String table : tables) {
            jdbcTemplate.update("DELETE FROM " + table + " WHERE entreprise_id IN (" + marques + ")", ids);
        }
        jdbcTemplate.update("DELETE FROM entreprise WHERE id IN (" + marques + ")", ids);
        jdbcTemplate.update("DELETE FROM tenant_group WHERE id IN (" + marques + ")", ids);
    }

    /** Tenant de test : JWT + identifiant d'entreprise extraits d'un login réel. */
    protected record Tenant(String token, long entrepriseId) {
    }
}
