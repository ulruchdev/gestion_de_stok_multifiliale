package com.stockmaster.notification.domain.entity;

import com.stockmaster.notification.domain.entity.NotificationAlerte;
import com.stockmaster.notification.domain.enums.EtatAlerte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Notification — fondamentaux : états d'alerte (DEC-004/037) et défauts @PrePersist")
class NotificationFundamentalsTest {

    @Test
    @DisplayName("EtatAlerte → NON_LUE / LUE / RESOLUE")
    void etatAlerteValues() {
        assertThat(EtatAlerte.values()).extracting(Enum::name)
                .containsExactly("NON_LUE", "LUE", "RESOLUE");
    }

    @Test
    @DisplayName("NotificationAlerte.onCreate → NON_LUE, type STOCK_BAS par défaut")
    void alerteDefaults() {
        NotificationAlerte a = new NotificationAlerte();
        a.onCreate();
        assertThat(a.getEtat()).isEqualTo(EtatAlerte.NON_LUE);
        assertThat(a.getTypeAlerte()).isEqualTo("STOCK_BAS");
    }

    @Test
    @DisplayName("NotificationAlerte.onCreate préserve un type explicite (type libre, sans CHECK — DEC-037)")
    void alertePreservesType() {
        NotificationAlerte a = new NotificationAlerte();
        a.setTypeAlerte("SEUIL_ATTEINT");
        a.setEtat(EtatAlerte.LUE);
        a.onCreate();
        assertThat(a.getTypeAlerte()).isEqualTo("SEUIL_ATTEINT");
        assertThat(a.getEtat()).isEqualTo(EtatAlerte.LUE);
    }
}
