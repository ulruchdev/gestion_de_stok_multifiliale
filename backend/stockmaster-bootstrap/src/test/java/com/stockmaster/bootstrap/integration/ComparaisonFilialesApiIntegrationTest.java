package com.stockmaster.bootstrap.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration de la comparaison inter-filiales (US-F092 / #1255) sur
 * PostgreSQL réel.
 *
 * <p>{@code ReportingServiceTest} mocke {@code LectureVentePort} : aucune
 * requête JPQL n'y est exécutée, donc ni le filtre de période semi-ouvert, ni
 * le filtre de statut, ni les jointures {@code article}/{@code client} ne sont
 * vérifiés. Ce test insère de vraies lignes {@code vente} et
 * {@code ligne_vente}, appelle l'endpoint avec un JWT {@code ADMIN_GROUPE}
 * réel, et contrôle les agrégats calculés de bout en bout.</p>
 *
 * <p>Le groupe et la maison mère sont créés par l'API d'inscription (donc
 * ADMIN_GROUPE authentique) ; les filiales et leurs ventes sont insérées en
 * JDBC, faute d'API de vente (US-064 / #1142 ouverte).</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("🧪 Intégration — comparaison inter-filiales (#1255), PostgreSQL réel")
class ComparaisonFilialesApiIntegrationTest extends ApiIntegrationTestSupport {

    private static final String DEBUT = "2026-11-01";
    private static final String FIN = "2026-11-30";

    private final List<Long> groupesCrees = new ArrayList<>();

    @AfterAll
    void nettoyerLaBase() {
        if (!entreprisesCreees.isEmpty()) {
            String marques = entreprisesCreees.stream().map(id -> "?").collect(Collectors.joining(", "));
            Object[] ids = entreprisesCreees.toArray();
            jdbcTemplate.update("DELETE FROM ligne_vente WHERE entreprise_id IN (" + marques + ")", ids);
            jdbcTemplate.update("DELETE FROM vente WHERE entreprise_id IN (" + marques + ")", ids);
            jdbcTemplate.update("DELETE FROM utilisateur WHERE entreprise_id IN (" + marques + ")", ids);
            // FK entreprise.parent_id : les filiales d'abord, la maison mère ensuite.
            jdbcTemplate.update("DELETE FROM entreprise WHERE id IN (" + marques + ") AND parent_id IS NOT NULL", ids);
            jdbcTemplate.update("DELETE FROM entreprise WHERE id IN (" + marques + ")", ids);
        }
        if (!groupesCrees.isEmpty()) {
            String groupes = groupesCrees.stream().map(id -> "?").collect(Collectors.joining(", "));
            Object[] idsGroupes = groupesCrees.toArray();
            // Catalogue partagé au niveau groupe (DEC-002) : article puis categorie.
            jdbcTemplate.update("DELETE FROM article WHERE group_id IN (" + groupes + ")", idsGroupes);
            jdbcTemplate.update("DELETE FROM categorie WHERE group_id IN (" + groupes + ")", idsGroupes);
            jdbcTemplate.update("DELETE FROM tenant_group WHERE id IN (" + groupes + ")", idsGroupes);
        }
    }

    @Test
    @DisplayName("#1255 — GET /reporting/filiales agrège de vraies ventes par filiale, périmètre et période respectés")
    void agregeLesVentesReellesParFiliale() throws Exception {
        Groupe groupe = creerGroupe("Comparaison");
        Groupe autreGroupe = creerGroupe("Autre");
        String suffixe = suffixe();

        String nomAlpha = "IT Alpha " + suffixe;
        String nomBeta = "IT Beta " + suffixe;
        String nomGamma = "IT Gamma " + suffixe;
        long alpha = insererFiliale(groupe, nomAlpha, "A" + suffixe.substring(0, 6));
        long beta = insererFiliale(groupe, nomBeta, "B" + suffixe.substring(0, 6));
        long gamma = insererFiliale(groupe, nomGamma, "C" + suffixe.substring(0, 6));

        // Catalogue partagé au niveau groupe (DEC-002) : un seul article pour les deux filiales.
        long articleDuGroupe = insererArticle(groupe, "ART-" + suffixe);

        // ── Alpha : 3 ventes PAYEE, dont une exactement à la borne basse (incluse)
        long alpha1 = insererVente(alpha, groupe.utilisateurId(), "PAYEE", Instant.parse("2026-11-10T10:00:00Z"));
        insererLigne(alpha, alpha1, articleDuGroupe, "3", 1000, "19.25");   // HT 3000, TVA 578
        long alpha2 = insererVente(alpha, groupe.utilisateurId(), "PAYEE", Instant.parse("2026-11-15T09:30:00Z"));
        insererLigne(alpha, alpha2, articleDuGroupe, "1", 5000, "19.25");   // HT 5000, TVA 963
        long alpha3 = insererVente(alpha, groupe.utilisateurId(), "PAYEE", Instant.parse("2026-11-01T00:00:00Z"));
        insererLigne(alpha, alpha3, articleDuGroupe, "1", 100, "0");        // HT 100, TVA 0

        // ── Bruit Alpha : vente annulée et vente à la borne haute (exclue)
        long alphaAnnulee = insererVente(alpha, groupe.utilisateurId(), "ANNULEE", Instant.parse("2026-11-12T08:00:00Z"));
        insererLigne(alpha, alphaAnnulee, articleDuGroupe, "10", 99_999, "19.25");
        long alphaHorsPeriode = insererVente(alpha, groupe.utilisateurId(), "PAYEE", Instant.parse("2026-12-01T00:00:00Z"));
        insererLigne(alpha, alphaHorsPeriode, articleDuGroupe, "1", 100_000, "0");

        // ── Beta : une seule vente, deux lignes (une vente comptée une fois)
        long beta1 = insererVente(beta, groupe.utilisateurId(), "PAYEE", Instant.parse("2026-11-20T16:00:00Z"));
        insererLigne(beta, beta1, articleDuGroupe, "1", 2000, "0");          // HT 2000, TVA 0
        insererLigne(beta, beta1, articleDuGroupe, "2", 1000, "10");         // HT 2000, TVA 200
        // Bruit Beta : vente PAYEE juste avant la période
        long betaHorsPeriode = insererVente(beta, groupe.utilisateurId(), "PAYEE", Instant.parse("2026-10-31T23:59:59Z"));
        insererLigne(beta, betaHorsPeriode, articleDuGroupe, "1", 500_000, "0");

        // ── Bruit inter-groupes : grosse vente d'un autre groupe, même dates
        long articleAutre = insererArticle(autreGroupe, "ART-X-" + suffixe);
        long autreVente = insererVente(autreGroupe.mereId(), autreGroupe.utilisateurId(),
                "PAYEE", Instant.parse("2026-11-10T10:00:00Z"));
        insererLigne(autreGroupe.mereId(), autreVente, articleAutre, "1", 999_999, "0");

        ResponseEntity<String> reponse = echanger(HttpMethod.GET,
                "/api/v1/reporting/filiales?debut=" + DEBUT + "&fin=" + FIN, null, groupe.tenant());
        assertThat(reponse.getStatusCode())
                .as("Comparaison : %s", reponse.getBody())
                .isEqualTo(HttpStatus.OK);

        JsonNode filiales = data(reponse).path("filiales");
        Map<Long, JsonNode> parEntreprise = new LinkedHashMap<>();
        filiales.forEach(ligne -> parEntreprise.put(ligne.path("entrepriseId").asLong(), ligne));

        assertThat(parEntreprise.keySet())
                .as("périmètre = toutes les entreprises non supprimées du groupe, maison mère incluse ; "
                        + "aucune entreprise d'un autre groupe")
                // CA HT décroissant, puis nom croissant pour les ex æquo.
                .containsExactly(alpha, beta, gamma, groupe.mereId());

        // Alpha : 8100 HT, 3 ventes, (8100 + 578 + 963) / 3 = 3214 (HALF_UP sur 3213,67)
        verifierLigne(parEntreprise.get(alpha), alpha, nomAlpha, 8100, 3, 3214);
        // Beta : 4000 HT, 1 vente de 2 lignes, (4000 + 0 + 200) / 1 = 4200
        verifierLigne(parEntreprise.get(beta), beta, nomBeta, 4000, 1, 4200);
        // Gamma : aucune vente → zéros explicites, pas d'omission
        verifierLigne(parEntreprise.get(gamma), gamma, nomGamma, 0, 0, 0);
        // Maison mère : inscrite par l'API, aucune vente sur la période
        verifierLigne(parEntreprise.get(groupe.mereId()), groupe.mereId(), groupe.nomMere(), 0, 0, 0);

        assertThat(parEntreprise).doesNotContainKey(autreGroupe.mereId());
    }

    private void verifierLigne(JsonNode ligne, long entrepriseId, String nom,
                               long caHt, long nbVentes, long panierMoyen) {
        assertThat(ligne).as("Ligne attendue pour l'entreprise %s", entrepriseId).isNotNull();
        assertThat(ligne.path("entrepriseId").asLong()).isEqualTo(entrepriseId);
        assertThat(ligne.path("nom").asText()).isEqualTo(nom);
        assertThat(ligne.path("caHt").asLong()).isEqualTo(caHt);
        assertThat(ligne.path("nbVentes").asLong()).isEqualTo(nbVentes);
        assertThat(ligne.path("panierMoyen").asLong()).isEqualTo(panierMoyen);
    }

    // ─── helpers d'insertion (aucune API de vente : US-064 / #1142 ouverte) ──

    /** Groupe réel (inscription HTTP) : maison mère + ADMIN_GROUPE connecté. */
    private Groupe creerGroupe(String etiquette) throws Exception {
        String suffixe = suffixe();
        String emailEntreprise = "it-cmp-" + etiquette.toLowerCase() + "-" + suffixe + "@stockmaster.test";
        String emailAdmin = "it-cmp-admin-" + etiquette.toLowerCase() + "-" + suffixe + "@stockmaster.test";

        Map<String, Object> inscription = new HashMap<>();
        inscription.put("nomGroupe", "IT Groupe " + etiquette + " " + suffixe);
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
                .as("Inscription groupe %s : %s", etiquette, reponse.getBody())
                .isEqualTo(HttpStatus.CREATED);

        Long mereId = jdbcTemplate.queryForObject(
                "SELECT id FROM entreprise WHERE email = ?", Long.class, emailEntreprise);
        Long groupId = jdbcTemplate.queryForObject(
                "SELECT group_id FROM entreprise WHERE id = ?", Long.class, mereId);
        Long utilisateurId = jdbcTemplate.queryForObject(
                "SELECT id FROM utilisateur WHERE email = ?", Long.class, emailAdmin);
        entreprisesCreees.add(mereId);
        groupesCrees.add(groupId);

        ResponseEntity<String> login = restTemplate.postForEntity("/api/v1/auth/login",
                json(Map.of("email", emailAdmin, "motDePasse", MOT_DE_PASSE), null), String.class);
        assertThat(login.getStatusCode()).as("Login %s : %s", etiquette, login.getBody()).isEqualTo(HttpStatus.OK);

        String nomMere = jdbcTemplate.queryForObject(
                "SELECT nom FROM entreprise WHERE id = ?", String.class, mereId);
        Tenant tenant = new Tenant(data(login).path("accessToken").asText(), mereId);
        return new Groupe(tenant, groupId, mereId, utilisateurId, nomMere);
    }

    private long insererFiliale(Groupe groupe, String nom, String code) {
        long id = jdbcTemplate.queryForObject(
                "INSERT INTO entreprise (group_id, parent_id, type_entreprise, nom, code_filiale, "
                        + "site_operationnel, actif) VALUES (?, ?, 'FILIALE', ?, ?, TRUE, TRUE) RETURNING id",
                Long.class, groupe.groupId(), groupe.mereId(), nom, code);
        entreprisesCreees.add(id);
        return id;
    }

    /** Article du catalogue groupe (DEC-002 : {@code group_id}, pas {@code entreprise_id}). */
    private long insererArticle(Groupe groupe, String codeArticle) {
        long categorieId = jdbcTemplate.queryForObject(
                "INSERT INTO categorie (group_id, code, designation) VALUES (?, ?, ?) RETURNING id",
                Long.class, groupe.groupId(), "CAT-" + codeArticle, "Catégorie " + codeArticle);
        return jdbcTemplate.queryForObject(
                "INSERT INTO article (group_id, categorie_id, code_article, designation, "
                        + "prix_achat_ht, prix_vente_ht, taux_tva, prix_vente_ttc) "
                        + "VALUES (?, ?, ?, ?, 0, 1000, 19.25, 1200) RETURNING id",
                Long.class, groupe.groupId(), categorieId, codeArticle, "Article " + codeArticle);
    }

    private long insererVente(long entrepriseId, long utilisateurId, String statut, Instant dateVente) {
        String code = "V-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return jdbcTemplate.queryForObject(
                "INSERT INTO vente (entreprise_id, utilisateur_id, code, date_vente, statut) "
                        + "VALUES (?, ?, ?, ?, ?) RETURNING id",
                Long.class, entrepriseId, utilisateurId, code, Timestamp.from(dateVente), statut);
    }

    private void insererLigne(long entrepriseId, long venteId, long articleId,
                              String quantite, int prixUnitaire, String tauxTva) {
        jdbcTemplate.update(
                "INSERT INTO ligne_vente (entreprise_id, vente_id, article_id, quantite, "
                        + "prix_unitaire, taux_tva_snapshot) VALUES (?, ?, ?, ?, ?, ?)",
                entrepriseId, venteId, articleId,
                new BigDecimal(quantite), prixUnitaire, new BigDecimal(tauxTva));
    }

    /** Groupe de test : JWT ADMIN_GROUPE, maison mère et utilisateur créateur. */
    private record Groupe(Tenant tenant, long groupId, long mereId, long utilisateurId, String nomMere) {
    }
}
