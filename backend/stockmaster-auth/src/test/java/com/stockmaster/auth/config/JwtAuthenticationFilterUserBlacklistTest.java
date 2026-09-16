package com.stockmaster.auth.config;

import com.stockmaster.shared.config.RedisHealthTracker;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US-025 — vérification {@code blacklist:user:{userId}} dans le filtre JWT.
 * Complète la blacklist par-jti (US-085/logout) : quand un admin désactive un
 * compte, la révocation doit être <b>immédiate</b> pour tous les access tokens
 * de la cible — sans attendre leur expiration.
 *
 * <p>Même contrat fail-open que la blacklist jti (US-085) : Redis injoignable
 * → authentification posée, échec enregistré — la barrière principale reste
 * la vérification {@code actif=false} au login et au refresh.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter — blacklist utilisateur (US-025 : révocation immédiate)")
class JwtAuthenticationFilterUserBlacklistTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private RedisHealthTracker redisHealthTracker;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain chain;
    @Mock private Claims claims;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider, redisTemplate, redisHealthTracker);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();
    }

    private void tokenValide() {
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        lenient().when(jwtTokenProvider.validateToken("valid-token")).thenReturn(claims);
        lenient().when(claims.get("jti", String.class)).thenReturn("jti-1");
        lenient().when(redisTemplate.hasKey("blacklist:jti:jti-1")).thenReturn(false);
    }

    @Test
    @DisplayName("❌ blacklist:user:{userId} présent → 401, chaîne interrompue, pas d'authentification")
    void shouldReject401WhenUserBlacklisted() throws Exception {
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        tokenValide();
        when(claims.get("userId", Long.class)).thenReturn(200L);
        when(redisTemplate.hasKey("blacklist:user:200")).thenReturn(true);

        filter.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        verify(response).setStatus(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("✅ blacklist:user:{userId} absent → authentification posée, chaîne poursuivie")
    void shouldAuthenticateWhenUserNotBlacklisted() throws Exception {
        tokenValide();
        when(claims.get("userId", Long.class)).thenReturn(200L);
        when(claims.get("role", String.class)).thenReturn("GESTIONNAIRE_STOCK");
        when(redisTemplate.hasKey("blacklist:user:200")).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("⚠️ US-085 — Redis injoignable sur la vérification utilisateur → fail-open : auth posée, échec enregistré")
    void shouldFailOpenWhenRedisUnavailableForUserCheck() throws Exception {
        tokenValide();
        when(claims.get("userId", Long.class)).thenReturn(200L);
        when(claims.get("role", String.class)).thenReturn("GESTIONNAIRE_STOCK");
        when(redisTemplate.hasKey("blacklist:user:200"))
                .thenThrow(new RedisConnectionFailureException("down"));

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(redisHealthTracker).recordFailure();
    }

    @Test
    @DisplayName("✅ Pas de claim userId (token atypique) → aucune vérification utilisateur, auth posée")
    void shouldSkipUserCheckWhenNoUserIdClaim() throws Exception {
        tokenValide();
        when(claims.get("userId", Long.class)).thenReturn(null);
        when(claims.get("role", String.class)).thenReturn("ADMIN_GROUPE");

        filter.doFilterInternal(request, response, chain);

        verify(redisTemplate, never()).hasKey(startsWith("blacklist:user:"));
        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }
}
