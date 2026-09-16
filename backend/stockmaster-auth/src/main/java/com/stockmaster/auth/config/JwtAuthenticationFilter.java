package com.stockmaster.auth.config;

import com.stockmaster.shared.config.RedisHealthTracker;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final RedisHealthTracker redisHealthTracker;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtTokenProvider.validateToken(token);

                if (sessionRevoquee(claims, response)) {
                    return;
                }

                Long userId = claims.get("userId", Long.class);
                String role = claims.get("role", String.class);

                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                new StockMasterPrincipal(userId, claims), null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                log.warn("Token expiré — poursuite sans authentification (endpoint public si permitAll)");
            } catch (JwtException e) {
                log.warn("Token invalide: {} — poursuite sans authentification", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Vérifie les deux blacklists Redis — fail-open US-085 : Redis injoignable →
     * on laisse passer (la signature JWT reste valide localement, bloquer ici ferait
     * tomber tous les endpoints authentifiés de l'application sur un incident Redis) ;
     * l'échec est enregistré au {@link RedisHealthTracker}.
     *
     * @return true si la requête doit être interrompue (401 déjà écrit)
     */
    private boolean sessionRevoquee(Claims claims, HttpServletResponse response) throws IOException {
        // 1. Blacklist par jti (déconnexion US-085)
        String jti = claims.get("jti", String.class);
        boolean redisJoignable = true;
        if (jti != null) {
            Boolean blacklistJti;
            try {
                blacklistJti = redisTemplate.hasKey("blacklist:jti:" + jti);
                redisHealthTracker.recordSuccess();
            } catch (RedisConnectionFailureException e) {
                redisHealthTracker.recordFailure();
                log.warn("Redis injoignable — vérification blacklist ignorée pour jti={} (fail-open)", jti);
                blacklistJti = false;
                redisJoignable = false;
            }
            if (Boolean.TRUE.equals(blacklistJti)) {
                log.warn("Token blacklisté (jti={})", jti);
                ecrire401(response, "Token révoqué");
                return true;
            }
        }

        // 2. Blacklist par utilisateur (US-025) — quand un admin désactive un compte,
        // ses sessions sont révoquées immédiatement (port TokenRevocationPort), sans
        // attendre l'expiration naturelle des tokens. Deuxième round-trip Redis accepté :
        // la révocation immédiate est un critère d'acceptation. Si Redis était déjà
        // injoignable au check jti, on ne reteste pas (même panne probable).
        Long userId = claims.get("userId", Long.class);
        if (userId != null && redisJoignable) {
            Boolean utilisateurRevoque;
            try {
                utilisateurRevoque = redisTemplate.hasKey("blacklist:user:" + userId);
            } catch (RedisConnectionFailureException e) {
                redisHealthTracker.recordFailure();
                log.warn("Redis injoignable — vérification blacklist utilisateur ignorée pour userId={} (fail-open)", userId);
                utilisateurRevoque = false;
            }
            if (Boolean.TRUE.equals(utilisateurRevoque)) {
                log.warn("Sessions utilisateur révoquées (US-025) — userId={}", userId);
                ecrire401(response, "Sessions révoquées");
                return true;
            }
        }
        return false;
    }

    private void ecrire401(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(401);
        response.getWriter().write("{\"errorCode\":\"AUTH_005\",\"detail\":\"" + detail + "\"}");
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
