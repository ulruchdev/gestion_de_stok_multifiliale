package com.stockmaster.auth.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RestAuthenticationEntryPoint — 401 JSON pour toute requête non authentifiée")
class RestAuthenticationEntryPointTest {

    private final RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("✅ Requête sans authentification → 401 AUTH_011 (JSON), pas 403")
    void shouldWrite401WhenUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/categories");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response,
                new InsufficientAuthenticationException("Full authentication is required"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).contains("application/json");

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("errorCode").asText())
                .as("le frontend rafraîchit sa session uniquement sur 401 — AUTH_011 est le contrat")
                .isEqualTo("AUTH_011");
        assertThat(body.get("detail").asText()).isEqualTo("Authentification requise");
    }
}
