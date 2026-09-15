package com.stockmaster.utilisateur;

import com.stockmaster.shared.handler.GlobalExceptionHandler;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Contexte de test MVC minimal pour {@link UtilisateurAdminController} — même
 * pattern que {@code GroupeTestApplication} : chaîne HTTP permitAll, le contrôle
 * d'accès est porté par les {@code @PreAuthorize} (méthode), comme en production.
 *
 * <p>{@code @Import} ciblé (pas de {@code @ComponentScan}) — même raison que
 * groupe : plusieurs contrôleurs coexistent dans {@code utilisateur.controller}
 * (US-021/022/023/024/025). <b>Chaque test MVC importe son contrôleur</b> via
 * {@code @Import} — cette configuration n'en déclare aucun, sinon tout contexte
 * du module exigerait les dépendances de tous les contrôleurs.</p>
 */
@SpringBootConfiguration
@Import(GlobalExceptionHandler.class)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@EnableMethodSecurity
public class UtilisateurTestApplication {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
