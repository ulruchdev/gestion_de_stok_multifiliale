package com.stockmaster.auth.security;

import com.stockmaster.shared.config.JwtProperties;
import com.stockmaster.shared.config.RedisHealthTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * US-025 — {@link RevocationTokenAdapter} : implémentation auth du port
 * {@code TokenRevocationPort} (shared). Contrats verrouillés :
 *
 * <ul>
 *   <li><b>Révocation double</b> : {@code blacklist:user:{userId}} (TTL = durée de vie
 *       max d'un access token — le filtre JWT refuse ensuite tout token de cet utilisateur)
 *       + suppression de {@code refresh:{userId}} (la rotation de refresh s'arrête net) ;</li>
 *   <li><b>Fail-open US-085</b> : Redis injoignable → aucune exception propagée
 *       (la désactivation du compte en base reste la barrière principale : login et
 *       refresh vérifient déjà {@code actif=false}) + {@code recordFailure}.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RevocationTokenAdapter — révocation des sessions d'un utilisateur (US-025)")
class RevocationTokenAdapterTest {

    private static final Long USER_ID = 200L;

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private RedisHealthTracker redisHealthTracker;

    private RevocationTokenAdapter adapter;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        adapter = new RevocationTokenAdapter(redisTemplate, redisHealthTracker, jwtProperties);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("✅ revoquerSessionsDe — blacklist:user:{id} avec TTL + refresh:{id} supprimé + succès enregistré")
    void shouldBlacklistUserKeyAndDeleteRefreshKey() {
        adapter.revoquerSessionsDe(USER_ID);

        verify(valueOperations).set(eq("blacklist:user:" + USER_ID), eq("true"), any(Duration.class));
        verify(redisTemplate).delete("refresh:" + USER_ID);
        verify(redisHealthTracker).recordSuccess();
    }

    @Test
    @DisplayName("⚠️ US-085 — Redis injoignable → fail-open : aucune exception, échec enregistré")
    void shouldFailOpenWhenRedisUnavailable() {
        doThrow(new RedisConnectionFailureException("down"))
                .when(valueOperations).set(any(String.class), any(String.class), any(Duration.class));

        assertThatCode(() -> adapter.revoquerSessionsDe(USER_ID))
                .doesNotThrowAnyException();

        verify(redisHealthTracker).recordFailure();
    }

    @Test
    @DisplayName("✅ userId null → no-op, aucune I/O Redis")
    void shouldDoNothingWhenUserIdNull() {
        adapter.revoquerSessionsDe(null);

        verifyNoInteractions(redisTemplate);
        verifyNoInteractions(redisHealthTracker);
        verify(redisTemplate, never()).delete(any(String.class));
    }
}
