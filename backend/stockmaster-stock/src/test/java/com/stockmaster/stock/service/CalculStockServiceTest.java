package com.stockmaster.stock.service;

import com.stockmaster.stock.domain.enums.TypeMouvement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CalculStockService — stock réel signé (DEC-023) et définition unique de la rupture")
class CalculStockServiceTest {

    private final CalculStockService service = new CalculStockService();

    // ===== Signes (DEC-023) ==================================================

    @Test
    @DisplayName("✅ ENTREE / TRANSFERT_ENTREE / ANNULATION_VENTE / REMBOURSEMENT / CORRECTION_POS sont positifs")
    void shouldSignPositiveTypes() {
        assertThat(CalculStockService.signeDe(TypeMouvement.ENTREE)).isEqualTo(1);
        assertThat(CalculStockService.signeDe(TypeMouvement.TRANSFERT_ENTREE)).isEqualTo(1);
        assertThat(CalculStockService.signeDe(TypeMouvement.ANNULATION_VENTE)).isEqualTo(1);
        assertThat(CalculStockService.signeDe(TypeMouvement.REMBOURSEMENT)).isEqualTo(1);
        assertThat(CalculStockService.signeDe(TypeMouvement.CORRECTION_POS)).isEqualTo(1);
    }

    @Test
    @DisplayName("✅ SORTIE / TRANSFERT_SORTIE / CORRECTION_NEG sont négatifs")
    void shouldSignNegativeTypes() {
        assertThat(CalculStockService.signeDe(TypeMouvement.SORTIE)).isEqualTo(-1);
        assertThat(CalculStockService.signeDe(TypeMouvement.TRANSFERT_SORTIE)).isEqualTo(-1);
        assertThat(CalculStockService.signeDe(TypeMouvement.CORRECTION_NEG)).isEqualTo(-1);
    }

    // ===== Application d'un mouvement ========================================

    @Test
    @DisplayName("✅ ENTREE : 10 + 5 = 15 (DECIMAL(12,3))")
    void shouldAddEntree() {
        assertThat(service.appliquer(new BigDecimal("10"), TypeMouvement.ENTREE, new BigDecimal("5")))
                .isEqualByComparingTo("15");
    }

    @Test
    @DisplayName("✅ SORTIE : 10 − 5 = 5 ; quantités décimales (poids/litre DEC-003)")
    void shouldSubtractSortie() {
        assertThat(service.appliquer(new BigDecimal("10"), TypeMouvement.SORTIE, new BigDecimal("2.500")))
                .isEqualByComparingTo("7.500");
    }

    @Test
    @DisplayName("✅ un TRANSFERT_SORTIE + TRANSFERT_ENTREE de même quantité est neutre pour le groupe")
    void shouldBalanceTransfert() {
        BigDecimal apresSortie = service.appliquer(new BigDecimal("20"), TypeMouvement.TRANSFERT_SORTIE, new BigDecimal("6"));
        BigDecimal apresEntree = service.appliquer(apresSortie, TypeMouvement.TRANSFERT_ENTREE, new BigDecimal("6"));
        assertThat(apresEntree).isEqualByComparingTo("20");
    }

    @Test
    @DisplayName("❌ refuse une quantité <= 0 (CHECK en base)")
    void shouldRejectNonPositiveQuantity() {
        assertThatThrownBy(() -> service.appliquer(BigDecimal.TEN, TypeMouvement.ENTREE, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.appliquer(BigDecimal.TEN, TypeMouvement.SORTIE, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ===== Rupture : stock ≤ seuil, seuil 0 = désactivé ======================

    @Test
    @DisplayName("✅ rupture = stock ≤ seuil (jamais <) — définition unique DEC-023")
    void shouldDetectRupture() {
        assertThat(service.estEnRupture(new BigDecimal("5"), new BigDecimal("5"))).isTrue();
        assertThat(service.estEnRupture(new BigDecimal("4.999"), new BigDecimal("5"))).isTrue();
        assertThat(service.estEnRupture(new BigDecimal("5.001"), new BigDecimal("5"))).isFalse();
    }

    @Test
    @DisplayName("✅ seuil 0 = alerte désactivée (jamais de faux positif)")
    void shouldDisableAlertWhenSeuilZero() {
        assertThat(service.estEnRupture(BigDecimal.ZERO, BigDecimal.ZERO)).isFalse();
        assertThat(service.estEnRupture(new BigDecimal("0"), new BigDecimal("-1"))).isFalse();
    }

    // ===== Invariant non-négativité (DEC-023) ================================

    @Test
    @DisplayName("✅ détecte la violation : une SORTIE qui rend le stock négatif doit être bloquée à l'écriture")
    void shouldDetectNegativeStockViolation() {
        BigDecimal stockApres = service.appliquer(new BigDecimal("3"), TypeMouvement.SORTIE, new BigDecimal("5"));
        assertThat(service.violInvariantNonNegativite(stockApres)).isTrue();
    }

    @Test
    @DisplayName("✅ un stock exactement nul respecte l'invariant")
    void shouldAcceptZeroStock() {
        assertThat(service.violInvariantNonNegativite(BigDecimal.ZERO)).isFalse();
    }
}
