package com.stockmaster.shared.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Configuration Web globale de l'application.
 *
 * <ul>
 *   <li>CORS — permet les requêtes depuis les origines configurées</li>
 *   <li>JPA Auditing — fournit le timestamp {@link Instant} pour {@code @CreatedDate} et {@code @LastModifiedDate}</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final CorsProperties corsProperties;

    /**
     * Filtre CORS basé sur les propriétés {@code stockmaster.cors.*}.
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(corsProperties.getAllowedMethods());
        config.setAllowedHeaders(corsProperties.getAllowedHeaders());
        config.setExposedHeaders(corsProperties.getExposedHeaders());
        config.setMaxAge(corsProperties.getMaxAge());
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * Fournisseur de date/heure pour {@code @EnableJpaAuditing}.
     * Utilise {@link Instant} pour tous les champs d'audit.
     */
    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }
}
