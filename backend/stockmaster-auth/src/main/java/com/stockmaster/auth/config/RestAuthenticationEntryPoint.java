package com.stockmaster.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Réponse 401 JSON pour toute requête non authentifiée sur un endpoint protégé
 * (token absent, malformé ou expiré — le filtre JWT poursuit alors sans
 * authentification). Sans cet entry point, Spring Security répond 403 par
 * défaut, et l'intercepteur du frontend (qui ne rafraîchit que sur 401) ne
 * déclenche jamais la rotation du refresh token (GS-1244).
 *
 * <p>Format aligné sur les autres écritures des filtres ({@code {"errorCode":..,"detail":..}})
 * — l'harmonisation RFC 7807 est suivie en C-15.</p>
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"errorCode\":\"AUTH_011\",\"detail\":\"Authentification requise\"}");
    }
}
