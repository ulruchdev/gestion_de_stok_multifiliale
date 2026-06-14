package com.stockmaster.auth;

import com.stockmaster.auth.controller.AuthController;
import com.stockmaster.shared.handler.GlobalExceptionHandler;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de test minimale pour le module auth.
 * Scanne le package du controller + fournit une config sécurité permissive.
 */
@SpringBootConfiguration
@ComponentScan(basePackageClasses = AuthController.class)
@Import(GlobalExceptionHandler.class)
public class AuthTestApplication {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
