package com.stockmaster.utilisateur.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-101 — la limite effective dépend du plan ET de son expiration : un plan
 * expiré est traité comme GRATUIT (10, DEC-015) pour le calcul, même si
 * {@code limite_utilisateurs} porte encore une valeur plus élevée. Ce test
 * verrouille la résolution de la limite effective avant son usage par
 * {@code AdminFilialeService} (US-021) puis les créations US-022/réactivation US-025.
 */
@DisplayName("ControleLimiteUtilisateursService — limite effective (US-101, plan expiré → GRATUIT)")
class ControleLimiteUtilisateursServiceEffectiveLimitTest {

    private final ControleLimiteUtilisateursService service = new ControleLimiteUtilisateursService();

    @Test
    @DisplayName("✅ plan actif → limite telle quelle (50 PRO)")
    void shouldUseStoredLimitWhenPlanActive() {
        assertThat(service.limiteUtilisateursEffective(50, false)).isEqualTo(50);
    }

    @Test
    @DisplayName("✅ plan expiré → traité comme GRATUIT (10), lecture littérale DEC-015/US-101")
    void shouldDegradeToGratuitWhenPlanExpired() {
        assertThat(service.limiteUtilisateursEffective(50, true)).isEqualTo(10);
    }

    @Test
    @DisplayName("✅ plan expiré → GRATUIT (10) même avec une valeur négociée différente")
    void shouldApplyGratuitGridRegardlessOfNegotiatedValue() {
        assertThat(service.limiteUtilisateursEffective(3, true)).isEqualTo(10);
    }

    @Test
    @DisplayName("✅ limite absente et plan actif → null (le refus fail-safe reste en aval)")
    void shouldKeepNullWhenNoLimit() {
        assertThat(service.limiteUtilisateursEffective(null, false)).isNull();
    }
}
