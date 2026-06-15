package com.stockmaster.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration qui valide le démarrage complet de l'application.
 *
 * <p>Ce test vérifie que :
 * <ul>
 *   <li>Le contexte Spring se charge sans erreur (tous les beans créés)</li>
 *   <li>L'endpoint {@code GET /actuator/health} répond {@code 200 OK}</li>
 *   <li>L'application est bien {@code "UP"}</li>
 * </ul>
 *
 * <p>Prérequis :
 * <ul>
 *   <li>PostgreSQL 16 doit tourner sur {@code localhost:5432} (via {@code docker compose up} ou service CI)</li>
 *   <li>Redis doit être accessible sur {@code localhost:6379}</li>
 *   <li>Profil {@code test} : utilise {@code jdbc:postgresql://localhost:5432/stockmaster_dev}</li>
 * </ul>
 *
 * @see StockMasterApplication
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
    }
)
@ActiveProfiles("test")
@DisplayName("🚀 Test d'intégration — Démarrage application complète")
class StockMasterApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Le contexte Spring doit se charger sans erreur")
    void contextLoads() {
        assertThat(applicationContext)
                .as("Le contexte Spring doit être initialisé")
                .isNotNull();

        int beanCount = applicationContext.getBeanDefinitionCount();
        assertThat(beanCount)
                .as("Le contexte doit contenir au moins 50 beans (application complète)")
                .isGreaterThan(50);
    }

    @Test
    @DisplayName("L'endpoint GET /actuator/health doit retourner 200 UP")
    void healthEndpointShouldReturnUp() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/actuator/health",
                String.class
        );

        assertThat(response.getStatusCode())
                .as("Le healthcheck doit répondre 200 OK")
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .as("Le body doit contenir \"UP\" — application démarrée")
                .contains("\"status\":\"UP\"");
    }

    @Test
    @DisplayName("L'endpoint GET /actuator/info doit retourner le nom de l'application")
    void infoEndpointShouldReturnAppName() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/actuator/info",
                String.class
        );

        assertThat(response.getStatusCode())
                .as("L'endpoint /actuator/info doit répondre")
                .isEqualTo(HttpStatus.OK);
    }
}
