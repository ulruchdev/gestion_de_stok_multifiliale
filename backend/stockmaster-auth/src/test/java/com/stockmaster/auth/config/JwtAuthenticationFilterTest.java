package com.stockmaster.auth.config;

import com.stockmaster.shared.config.RedisHealthTracker;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter — vérification JWT & blacklist (US-085 fail-open)")
class JwtAuthenticationFilterTest {

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
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("✅ Aucun token → poursuit sans authentification")
    void shouldProceedWithoutTokenWhenAbsent() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("✅ Token valide, jti non blacklisté → authentification posée, chaîne poursuivie")
    void shouldAuthenticateWhenTokenValidAndNotBlacklisted() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(claims);
        when(claims.get("jti", String.class)).thenReturn("jti-1");
        when(claims.get("userId", Long.class)).thenReturn(1L);
        when(claims.get("role", String.class)).thenReturn("ADMIN_GROUPE");
        when(redisTemplate.hasKey("blacklist:jti:jti-1")).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("❌ Token valide, jti blacklisté → 401, chaîne interrompue, pas d'authentification")
    void shouldReject401WhenBlacklisted() throws Exception {
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(claims);
        when(claims.get("jti", String.class)).thenReturn("jti-1");
        when(redisTemplate.hasKey("blacklist:jti:jti-1")).thenReturn(true);

        filter.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        verify(response).setStatus(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("⚠️ US-085 — Redis indisponible sur la vérification blacklist → fail-open : authentification posée, chaîne poursuivie, échec enregistré")
    void shouldFailOpenWhenRedisUnavailableForBlacklistCheck() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(claims);
        when(claims.get("jti", String.class)).thenReturn("jti-1");
        when(claims.get("userId", Long.class)).thenReturn(1L);
        when(claims.get("role", String.class)).thenReturn("ADMIN_GROUPE");
        when(redisTemplate.hasKey("blacklist:jti:jti-1"))
                .thenThrow(new RedisConnectionFailureException("down"));

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(redisHealthTracker).recordFailure();
    }

    @Test
    @DisplayName("✅ Token expiré → poursuit sans authentification, aucune exception propagée")
    void shouldProceedWithoutAuthenticationWhenTokenExpired() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer expired-token");
        when(jwtTokenProvider.validateToken("expired-token"))
                .thenThrow(new ExpiredJwtException(null, null, "expiré"));

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
