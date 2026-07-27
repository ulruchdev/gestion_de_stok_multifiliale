package com.stockmaster.shared.config;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration du rate limiting externalisée via {@code application.yml}.
 *
 * <p>Préfixe : {@code stockmaster.rate-limiting}</p>
 *
 * <p>Permet de configurer :</p>
 * <ul>
 *   <li>Un {@link GlobalConfig compteur global} pour détecter les bots (toutes requêtes confondues)</li>
 *   <li>Des {@link EndpointConfig compteurs par endpoint} pour les points sensibles (login, inscription, etc.)</li>
 * </ul>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "stockmaster.rate-limiting")
public class RateLimitProperties {

    /** Active/désactive le rate limiting globalement. */
    private boolean enabled = true;

    /** Configuration du compteur global (toutes les requêtes). */
    private GlobalConfig global = new GlobalConfig();

    /**
     * Configuration des compteurs par endpoint.
     * <p>Clé = nom logique de l'endpoint (ex: "login", "refresh"),
     *    valeur = {@link EndpointConfig} avec URI, maxAttempts, windowSeconds.</p>
     */
    private Map<String, EndpointConfig> endpoints = new HashMap<>();

    @Getter
    @Setter
    public static class GlobalConfig {
        /** Active/désactive le compteur global. */
        private boolean enabled = true;

        /** Nombre maximum de requêtes autorisées dans la fenêtre. */
        @PositiveOrZero
        private int maxRequests = 100;

        /** Durée de la fenêtre en secondes (défaut : 1 minute). */
        @Positive
        private long windowSeconds = 60;
    }

    @Getter
    @Setter
    public static class EndpointConfig {
        /** URI exacte de l'endpoint à limiter (ex: /api/v1/auth/login). */
        private String uri;

        /** Nombre maximum de tentatives autorisées dans la fenêtre. */
        @PositiveOrZero
        private int maxAttempts = 5;

        /** Durée de la fenêtre en secondes (défaut : 15 minutes). */
        @Positive
        private long windowSeconds = 900;
    }
}
