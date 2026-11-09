package com.stockmaster.stock.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-051 — dérivation de {@code statutAlerte} conforme au REF §4.2 :
 *
 * <ul>
 *   <li>{@code ANOMALIE} si stock &lt; 0 (erreur de donnée)</li>
 *   <li>{@code RUPTURE} si stock ≤ 0 — <b>décision GO utilisateur</b> : lecture
 *       littérale du REF §4.2, un article jamais approvisionné (stock 0) remonte
 *       {@code RUPTURE}</li>
 *   <li>{@code BAS} si 0 &lt; stock ≤ seuil (seuil 0 = alerte désactivée)</li>
 *   <li>{@code NORMAL} sinon</li>
 * </ul>
 */
@DisplayName("StatutAlerte.de — dérivation REF §4.2 (US-051)")
class StatutAlerteTest {

    @Nested
    @DisplayName("✅ Cas nominaux")
    class Nominaux {

        @Test
        @DisplayName("✅ stock = 0 → RUPTURE (décision GO : REF §4.2 littéral, même sans mouvement)")
        void shouldRuptureQuandStockZero() {
            assertThat(StatutAlerte.de(BigDecimal.ZERO, new BigDecimal("10.000")))
                    .isEqualTo(StatutAlerte.RUPTURE);
        }

        @Test
        @DisplayName("✅ stock < 0 → ANOMALIE (état impossible, erreur de donnée)")
        void shouldAnomalieQuandStockNegatif() {
            assertThat(StatutAlerte.de(new BigDecimal("-2.500"), new BigDecimal("10.000")))
                    .isEqualTo(StatutAlerte.ANOMALIE);
        }

        @Test
        @DisplayName("✅ 0 < stock ≤ seuil → BAS")
        void shouldBasQuandSousLeSeuil() {
            assertThat(StatutAlerte.de(new BigDecimal("3.000"), new BigDecimal("5.000")))
                    .isEqualTo(StatutAlerte.BAS);
        }

        @Test
        @DisplayName("✅ stock = seuil exact → BAS (bornes inclusives)")
        void shouldBasQuandEgalAuSeuil() {
            assertThat(StatutAlerte.de(new BigDecimal("5.000"), new BigDecimal("5.000")))
                    .isEqualTo(StatutAlerte.BAS);
        }

        @Test
        @DisplayName("✅ stock > seuil → NORMAL")
        void shouldNormalAuDessusDuSeuil() {
            assertThat(StatutAlerte.de(new BigDecimal("10.000"), new BigDecimal("5.000")))
                    .isEqualTo(StatutAlerte.NORMAL);
        }

        @Test
        @DisplayName("✅ seuil = 0 → alerte désactivée : stock positif = NORMAL")
        void shouldNormalQuandAlerteDesactivee() {
            assertThat(StatutAlerte.de(new BigDecimal("5.000"), BigDecimal.ZERO))
                    .isEqualTo(StatutAlerte.NORMAL);
        }
    }

    @Nested
    @DisplayName("❌ Entrées invalides")
    class Invalides {

        @Test
        @DisplayName("❌ stock null → IllegalArgumentException")
        void shouldRefuserStockNull() {
            assertThatThrownBy(() -> StatutAlerte.de(null, BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("❌ seuil null → IllegalArgumentException")
        void shouldRefuserSeuilNull() {
            assertThatThrownBy(() -> StatutAlerte.de(BigDecimal.ONE, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
