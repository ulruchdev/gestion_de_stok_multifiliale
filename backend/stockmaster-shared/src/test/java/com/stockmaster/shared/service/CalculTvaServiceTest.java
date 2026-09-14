package com.stockmaster.shared.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CalculTvaService — TTC XAF (DEC-003) et conversion unités (DEC-013)")
class CalculTvaServiceTest {

    private final CalculTvaService service = new CalculTvaService();

    // ===== DEC-003 : TTC = HT × (1 + TVA/100), HALF_UP, entier XAF ==========

    @ParameterizedTest(name = "HT {0} XAF, TVA {1}% → TTC {2} XAF")
    @CsvSource({
            "1000, 19.25, 1193",      // cas nominal, arrondi HALF_UP de 1192.5
            "1000, 0,    1000",       // exonéré
            "0,    19.25, 0",         // gratuit
            "100,  100,   200"        // TVA 100 %
    })
    @DisplayName("✅ calcule le TTC XAF (entier, arrondi HALF_UP)")
    void shouldComputeTtc(Integer ht, String tva, Integer attendu) {
        assertThat(service.calculerTtc(ht, new BigDecimal(tva))).isEqualTo(attendu);
    }

    @Test
    @DisplayName("❌ refuse un prix HT négatif")
    void shouldRejectNegativePrice() {
        assertThatThrownBy(() -> service.calculerTtc(-1, new BigDecimal("19.25")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("❌ refuse un taux de TVA négatif")
    void shouldRejectNegativeTva() {
        assertThatThrownBy(() -> service.calculerTtc(1000, new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("❌ refuse des arguments nuls")
    void shouldRejectNulls() {
        assertThatThrownBy(() -> service.calculerTtc(null, new BigDecimal("19.25")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.calculerTtc(1000, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ===== DEC-013 : conversion achat ↔ gestion ==============================

    @Test
    @DisplayName("✅ 1 carton (facteur 12) × 3 achetés = 36 unités de gestion")
    void shouldConvertAchatVersGestion() {
        assertThat(service.achatVersGestion(new BigDecimal("3"), new BigDecimal("12")))
                .isEqualByComparingTo("36.000");
    }

    @Test
    @DisplayName("✅ conversion avec décimales (sac de 12.5 kg)")
    void shouldConvertWithDecimals() {
        assertThat(service.achatVersGestion(new BigDecimal("2.500"), new BigDecimal("12.5")))
                .isEqualByComparingTo("31.250");
    }

    @Test
    @DisplayName("✅ gestion → achat : division arrondie à 3 décimales")
    void shouldConvertGestionVersAchat() {
        assertThat(service.gestionVersAchat(new BigDecimal("10"), new BigDecimal("3")))
                .isEqualByComparingTo("3.333");
    }

    @Test
    @DisplayName("❌ refuse un facteur de conversion <= 0 (CHECK facteur_conversion > 0)")
    void shouldRejectNonPositiveFactor() {
        assertThatThrownBy(() -> service.achatVersGestion(BigDecimal.ONE, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.gestionVersAchat(BigDecimal.ONE, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("❌ refuse une quantité négative")
    void shouldRejectNegativeQuantity() {
        assertThatThrownBy(() -> service.achatVersGestion(new BigDecimal("-1"), BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
