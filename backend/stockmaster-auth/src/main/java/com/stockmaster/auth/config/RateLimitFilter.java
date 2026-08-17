package com.stockmaster.auth.config;

import com.stockmaster.shared.config.RateLimitProperties;
import com.stockmaster.shared.config.RedisHealthTracker;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * Filtre de rate limiting avec deux niveaux :
 * <ol>
 *   <li><b>Global</b> — toutes les requêtes confondues (détection de bots)</li>
 *   <li><b>Par endpoint</b> — endpoints sensibles configurés dans {@code application.yml}</li>
 * </ol>
 *
 * <p>Les seuils et fenêtres sont entièrement configurables via
 * {@link RateLimitProperties} (préfixe {@code stockmaster.rate-limiting}).</p>
 *
 * <p><b>US-085 — Comportement si Redis est injoignable :</b> le compteur global (anti-bot,
 * toutes requêtes confondues) est <b>fail-open</b> — le bloquer ferait tomber toute
 * l'application sur un simple incident Redis. Le compteur par endpoint (sur les URIs
 * sensibles configurées) est <b>fail-closed</b> (503) — c'est la protection anti-bruteforce
 * elle-même, la contourner silencieusement est le risque que cette US couvre.</p>
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties rateLimitProperties;
    private final RedisHealthTracker redisHealthTracker;

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        if (!rateLimitProperties.isEnabled()) {
            chain.doFilter(req, res);
            return;
        }

        String ip = getClientIp(req);
        String uri = req.getRequestURI();

        // ================================================================
        // 1. Vérification du compteur GLOBAL (toutes les requêtes) — fail-open
        // ================================================================
        if (rateLimitProperties.getGlobal().isEnabled()) {
            String globalKey = "rate_limit_global:" + ip;
            try {
                if (isExceeded(globalKey,
                        rateLimitProperties.getGlobal().getMaxRequests(),
                        rateLimitProperties.getGlobal().getWindowSeconds())) {
                    redisHealthTracker.recordSuccess();
                    log.warn("Rate limit GLOBAL atteint — IP {} bloquée pour {}s",
                            ip, rateLimitProperties.getGlobal().getWindowSeconds());
                    send429(res, "Trop de requêtes. Réessayez dans quelques instants.");
                    return;
                }
                redisHealthTracker.recordSuccess();
            } catch (RedisConnectionFailureException e) {
                redisHealthTracker.recordFailure();
                log.warn("Redis injoignable — rate limit GLOBAL ignoré pour cette requête (fail-open)");
            }
        }

        // ================================================================
        // 2. Vérification par endpoint sensible — fail-closed
        // ================================================================
        for (RateLimitProperties.EndpointConfig config : rateLimitProperties.getEndpoints().values()) {
            if (config.getUri() != null && config.getUri().equals(uri)) {
                String endpointKey = "rate_limit:" + sanitizeUri(config.getUri()) + ":" + ip;
                try {
                    if (isExceeded(endpointKey, config.getMaxAttempts(), config.getWindowSeconds())) {
                        redisHealthTracker.recordSuccess();
                        log.warn("Rate limit ENDPOINT {} atteint — IP {} bloquée pour {}s",
                                config.getUri(), ip, config.getWindowSeconds());
                        send429(res, "Trop de tentatives sur cet endpoint. Réessayez plus tard.");
                        return;
                    }
                    redisHealthTracker.recordSuccess();
                } catch (RedisConnectionFailureException e) {
                    redisHealthTracker.recordFailure();
                    log.error("Redis injoignable — endpoint sensible {} rejeté (fail-closed, SEC_STORE_UNAVAILABLE)",
                            config.getUri());
                    send503(res);
                    return;
                }
                break;
            }
        }

        chain.doFilter(req, res);
    }

    /**
     * Vérifie si le compteur a dépassé le seuil autorisé.
     * Si non, incrémente le compteur et définir le TTL si c'est la première requête.
     *
     * @param key          clé Redis
     * @param maxAttempts  nombre maximum de tentatives autorisées
     * @param windowSeconds fenêtre de temps en secondes
     * @return true si le seuil est dépassé
     */
    private boolean isExceeded(String key, int maxAttempts, long windowSeconds) {
        String attemptsStr = redisTemplate.opsForValue().get(key);
        int attempts = (attemptsStr != null) ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= maxAttempts) {
            return true;
        }

        redisTemplate.opsForValue().increment(key);
        // Définir le TTL uniquement sur la première requête (évite de prolonger la fenêtre)
        if (attemptsStr == null) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }
        return false;
    }

    private void send429(HttpServletResponse res, String message) throws IOException {
        res.setStatus(429);
        res.setContentType("application/json");
        res.getWriter().write("{\"errorCode\":\"AUTH_429\",\"detail\":\"" + message + "\"}");
    }

    private void send503(HttpServletResponse res) throws IOException {
        res.setStatus(503);
        res.setContentType("application/json");
        res.getWriter().write("{\"errorCode\":\"SEC_004\",\"detail\":\"Service de sécurité temporairement indisponible\"}");
    }

    private String sanitizeUri(String uri) {
        return uri.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (xff != null) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }
}
