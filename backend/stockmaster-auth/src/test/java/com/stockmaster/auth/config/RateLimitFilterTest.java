package com.stockmaster.auth.config;

import com.stockmaster.shared.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitFilter — comportement réel de main")
class RateLimitFilterTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain chain;

    private RateLimitProperties properties;
    private RateLimitFilter filter;
    private StringWriter responseBody;

    @BeforeEach
    void setUp() throws Exception {
        properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.getGlobal().setEnabled(true);
        properties.getGlobal().setMaxRequests(100);
        properties.getGlobal().setWindowSeconds(60);

        RateLimitProperties.EndpointConfig loginConfig = new RateLimitProperties.EndpointConfig();
        loginConfig.setUri("/api/v1/auth/login");
        loginConfig.setMaxAttempts(5);
        loginConfig.setWindowSeconds(900);
        properties.setEndpoints(Map.of("login", loginConfig));

        filter = new RateLimitFilter(redisTemplate, properties);

        responseBody = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    }
    @Test
    @DisplayName("Limiteur désactivé — chaîne poursuivie sans toucher Redis")
    void shouldPassThroughWhenDisabled() throws Exception {
        properties.setEnabled(false);
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    @DisplayName("Sous le seuil global — incrémente (1er appel : TTL) et poursuit la chaîne")
    void shouldProceedWhenUnderLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/some-endpoint");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // Premier appel pour cette IP : aucune valeur en cache → TTL posé
        when(valueOperations.get("rate_limit_global:127.0.0.1")).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(valueOperations).increment("rate_limit_global:127.0.0.1");
        verify(redisTemplate).expire(eq("rate_limit_global:127.0.0.1"), any());
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("Seuil global atteint — 429 AUTH_429, chaîne interrompue")
    void shouldReturn429WhenGlobalLimitExceeded() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/some-endpoint");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("rate_limit_global:127.0.0.1")).thenReturn("100");

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        verify(response).setStatus(429);
        assertThat(responseBody.toString()).contains("AUTH_429");
    }
    @Test
    @DisplayName("Seuil d'un endpoint sensible atteint — 429, chaîne interrompue")
    void shouldReturn429WhenEndpointLimitExceeded() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("rate_limit_global:127.0.0.1")).thenReturn("1");
        when(valueOperations.get("rate_limit:_api_v1_auth_login:127.0.0.1")).thenReturn("5");

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        verify(response).setStatus(429);
        assertThat(responseBody.toString()).contains("AUTH_429");
    }

    @Test
    @DisplayName("Endpoint non sensible : seul le compteur global sollicite Redis, chaîne poursuivie")
    void shouldNotAffectNonSensitiveEndpoints() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/catalogue/articles");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("rate_limit_global:127.0.0.1")).thenReturn("1");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("Redis indisponible sur le compteur global — l'exception se propage (500 sur main, avant branchement GS-085)")
    void shouldPropagateWhenRedisUnavailable() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/some-endpoint");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("rate_limit_global:127.0.0.1"))
                .thenThrow(new RedisConnectionFailureException("down"));

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(RedisConnectionFailureException.class);

        verify(chain, never()).doFilter(any(), any());
        verify(response, never()).setStatus(anyInt());
    }
}