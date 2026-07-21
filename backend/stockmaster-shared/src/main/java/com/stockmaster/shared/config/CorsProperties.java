package com.stockmaster.shared.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Configuration CORS externalisée via {@code application.yml}.
 *
 * <p>Préfixe : {@code stockmaster.cors}</p>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "stockmaster.cors")
public class CorsProperties {

    /** Origines autorisées (ex: http://localhost:5173, https://app.stockmaster.cm). */
    @NotEmpty
    private List<String> allowedOrigins;

    /** Méthodes HTTP autorisées. */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    /** Entêtes autorisées. */
    private List<String> allowedHeaders = List.of("*");

    /** Exposition des entêtes au client. */
    private List<String> exposedHeaders = List.of("X-Request-Id");

    /** Durée de cache des preflight requests en secondes. */
    private long maxAge = 3600;
}
