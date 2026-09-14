package com.stockmaster.groupe;

import com.stockmaster.groupe.controller.FilialeController;
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
 * Contexte de test MVC minimal pour {@link FilialeController} — même pattern que
 * {@link GroupeTestApplication} (voir sa javadoc : {@code @Import} ciblé, pas de
 * {@code @ComponentScan} du package {@code groupe.controller}).
 */
@SpringBootConfiguration
@Import({FilialeController.class, GlobalExceptionHandler.class})
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@EnableMethodSecurity
public class FilialeTestApplication {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
