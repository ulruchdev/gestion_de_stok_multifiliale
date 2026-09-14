package com.stockmaster.groupe.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ControleLimiteFilialesService — limite du plan, jamais en dur (DEC-015)")
class ControleLimiteFilialesServiceTest {

    private final ControleLimiteFilialesService service = new ControleLimiteFilialesService();

    @Test
    @DisplayName("✅ autorise tant que le nombre courant est strictement sous la limite")
    void shouldAllowUnderLimit() {
        assertThat(service.peutAjouterFiliale(0, 1)).isTrue();
        assertThat(service.peutAjouterFiliale(3, 15)).isTrue();
    }

    @Test
    @DisplayName("❌ refuse à la limite atteinte (comparaison stricte)")
    void shouldRefuseAtLimit() {
        assertThat(service.peutAjouterFiliale(1, 1)).isFalse();
        assertThat(service.peutAjouterFiliale(15, 15)).isFalse();
    }

    @Test
    @DisplayName("❌ refuse si la limite est absente ou invalide (0/négatif/null)")
    void shouldRefuseInvalidLimit() {
        assertThat(service.peutAjouterFiliale(0, 0)).isFalse();
        assertThat(service.peutAjouterFiliale(0, -5)).isFalse();
        assertThat(service.peutAjouterFiliale(0, null)).isFalse();
    }

    @Test
    @DisplayName("✅ DEC-015 : les sites site_operationnel = FALSE ne comptent pas dans la limite")
    void shouldExcludeNonOperationalSites() {
        // 4 entreprises actives dont 1 pur siège → 3 sites comptabilisés
        assertThat(service.comptabilise(4, 1)).isEqualTo(3);
        assertThat(service.comptabilise(1, 5)).isZero(); // jamais négatif
    }
}
