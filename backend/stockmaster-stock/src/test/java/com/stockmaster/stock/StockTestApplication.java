package com.stockmaster.stock;

import com.stockmaster.shared.handler.GlobalExceptionHandler;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Contexte de test MVC minimal pour le module stock — même pattern que
 * {@code CatalogueTestApplication} : chaîne HTTP permitAll, le contrôle
 * d'accès est porté par les {@code @PreAuthorize} (méthode), comme en production.
 *
 * <p><b>CSRF laissé ACTIF</b> (défaut Spring Security — jamais désactivé ici,
 * CodeQL {@code java/spring-disabled-csrf-protection}) : les tests MockMvc
 * passent le jeton via {@code SecurityMockMvcRequestPostProcessors.csrf()},
 * comme le ferait un client navigateur (convention
 * {@code TiersTestApplication}).</p>
 *
 * <p>{@code @Import} ciblé (pas de {@code @ComponentScan}) — chaque test MVC
 * importe son contrôleur via {@code @Import}.</p>
 */
@SpringBootConfiguration
@Import(GlobalExceptionHandler.class)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@EnableMethodSecurity
public class StockTestApplication {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
