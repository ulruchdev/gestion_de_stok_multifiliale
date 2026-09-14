package com.stockmaster.reporting.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CalculCaService — CA HT facturé vs encaissé (DEC-020), montants XAF (DEC-003)")
class CalculCaServiceTest {

    private final CalculCaService service = new CalculCaService();

    @Test
    @DisplayName("✅ CA HT d'une ligne = quantité × prix HT (décimales autorisées)")
    void shouldComputeLineHt() {
        var ligne = new CalculCaService.LigneReport(1L, new BigDecimal("2.500"), 1000, new BigDecimal("19.25"));
        assertThat(ligne.montantHt()).isEqualTo(2500);
    }

    @Test
    @DisplayName("✅ TVA d'une ligne = HT × taux/100, arrondi HALF_UP")
    void shouldComputeLineTva() {
        var ligne = new CalculCaService.LigneReport(1L, new BigDecimal("1"), 1000, new BigDecimal("19.25"));
        assertThat(ligne.montantTva()).isEqualTo(193); // 192.5 → 193
    }

    @Test
    @DisplayName("✅ CA facturé multi-lignes = somme HT ; TTC = HT + TVA")
    void shouldComputeMultiLineCa() {
        List<CalculCaService.LigneReport> lignes = List.of(
                new CalculCaService.LigneReport(1L, new BigDecimal("2"), 500, new BigDecimal("19.25")),
                new CalculCaService.LigneReport(2L, new BigDecimal("1"), 1000, BigDecimal.ZERO)
        );
        assertThat(service.caHtFacture(lignes)).isEqualTo(2000);
        assertThat(service.tvaFacturee(lignes)).isEqualTo(193);
        assertThat(service.caTtcFacture(lignes)).isEqualTo(2193);
    }

    @Test
    @DisplayName("✅ liste vide ou nulle → CA nul (pas d'exception)")
    void shouldHandleEmptyList() {
        assertThat(service.caHtFacture(List.of())).isZero();
        assertThat(service.caHtFacture(null)).isZero();
    }

    @Test
    @DisplayName("❌ refuse une ligne incohérente (quantité ≤ 0, prix ou TVA négatif)")
    void shouldRejectInvalidLine() {
        assertThatThrownBy(() -> new CalculCaService.LigneReport(1L, BigDecimal.ZERO, 100, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CalculCaService.LigneReport(1L, BigDecimal.ONE, -1, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CalculCaService.LigneReport(1L, BigDecimal.ONE, 100, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
