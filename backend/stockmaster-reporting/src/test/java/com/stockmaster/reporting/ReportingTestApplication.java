package com.stockmaster.reporting;

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
 * Contexte de test MVC minimal pour le module reporting — même pattern que
 * {@code StockTestApplication} : chaîne HTTP {@code permitAll}, l'accès est
 * porté par les {@code @PreAuthorize} des contrôleurs, comme en production.
 *
 * <p>CSRF laissé actif (défaut Spring Security) ; les tests n'émettent que des
 * {@code GET}, donc aucun jeton n'est requis.</p>
 */
@SpringBootConfiguration
@Import(GlobalExceptionHandler.class)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@EnableMethodSecurity
public class ReportingTestApplication {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
