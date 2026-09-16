package com.stockmaster.auth.security;

import com.stockmaster.shared.config.JwtProperties;
import com.stockmaster.shared.config.RedisHealthTracker;
import com.stockmaster.shared.security.TokenRevocationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * US-025 — implémentation auth du port {@link TokenRevocationPort}.
 *
 * <p>Révocation double, dans l'espace de clés que le module auth possède déjà :
 * <ul>
 *   <li>{@code blacklist:user:{userId}} (TTL = durée de vie max d'un access token) :
 *       le filtre JWT refuse tout token de cet utilisateur dès la prochaine requête ;</li>
 *   <li>{@code refresh:{userId}} supprimé : la rotation de refresh (US-083) s'arrête net
 *       — même s'il reste des tokens d'appoint côté client, ils ne produiront rien.</li>
 * </ul></p>
 *
 * <p><b>Fail-open US-085</b> : un incident Redis ne propage aucune exception —
 * l'admin voit la désactivation réussir, la barrière principale reste la
 * vérification {@code actif=false} au login et au refresh. L'échec est signalé
 * au {@link RedisHealthTracker} (alerte technique unique après 60 s de panne).</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RevocationTokenAdapter implements TokenRevocationPort {

    private static final String BLACKLIST_USER_PREFIX = "blacklist:user:";
    private static final String REFRESH_KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;
    private final RedisHealthTracker redisHealthTracker;
    private final JwtProperties jwtProperties;

    @Override
    public void revoquerSessionsDe(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            // TTL = durée de vie max d'un access token : la clé meurt exactement quand
            // le dernier token émis avant la révocation aurait de toute façon expiré.
            Duration ttl = Duration.ofSeconds(jwtProperties.getAccessTokenExpiration());
            redisTemplate.opsForValue().set(BLACKLIST_USER_PREFIX + userId, "true", ttl);
            redisTemplate.delete(REFRESH_KEY_PREFIX + userId);
            log.info("US-025 : sessions révoquées pour userId={} (blacklist TTL={}s, refresh supprimé)",
                    userId, ttl.toSeconds());
            redisHealthTracker.recordSuccess();
        } catch (RedisConnectionFailureException e) {
            redisHealthTracker.recordFailure();
            log.error("Redis injoignable — révocation des sessions impossible pour userId={} "
                    + "(le compte reste bloqué au login/refresh via actif=false)", userId);
        }
    }
}
