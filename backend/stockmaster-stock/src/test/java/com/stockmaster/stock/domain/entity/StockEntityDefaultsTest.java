package com.stockmaster.stock.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Stock — défauts @PrePersist : journal immuable (C-12), transferts DEC-007, inventaire DEC-036")
class StockEntityDefaultsTest {

    @Test
    @DisplayName("MouvementStock.onCreate → horodaté, supprime=false (CHECK chk_mouvement_stock_supprime_interdit)")
    void mouvementStockDefaults() {
        MouvementStock m = new MouvementStock();
        m.onCreate();
        assertThat(m.getDateMouvement()).isNotNull();
        assertThat(m.getDateCreation()).isNotNull();
        assertThat(m.getDateModification()).isNotNull();
        assertThat(m.getSupprime()).isFalse();
    }

    @Test
    @DisplayName("TransfertStock.onCreate → DEMANDE, daté")
    void transfertStockDefaults() {
        TransfertStock t = new TransfertStock();
        t.onCreate();
        assertThat(t.getDateDemande()).isNotNull();
        assertThat(t.getStatut()).isEqualTo(com.stockmaster.stock.domain.enums.StatutTransfert.DEMANDE);
    }

    @Test
    @DisplayName("LigneTransfert.onCreate → quantités expédiée/reçue à 0 (écarts calculés ensuite)")
    void ligneTransfertDefaults() {
        LigneTransfert l = new LigneTransfert();
        l.onCreate();
        assertThat(l.getQuantiteExpediee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(l.getQuantiteRecue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("SessionInventaire.onCreate → OUVERTE, datée")
    void sessionInventaireDefaults() {
        SessionInventaire s = new SessionInventaire();
        s.onCreate();
        assertThat(s.getDateOuverture()).isNotNull();
        assertThat(s.getStatut()).isEqualTo(com.stockmaster.stock.domain.enums.StatutSessionInventaire.OUVERTE);
    }

    @Test
    @DisplayName("LigneInventaire.onCreate → A_COMPTER")
    void ligneInventaireDefaults() {
        LigneInventaire l = new LigneInventaire();
        l.onCreate();
        assertThat(l.getStatut()).isEqualTo(com.stockmaster.stock.domain.enums.StatutLigneInventaire.A_COMPTER);
    }

    @Test
    @DisplayName("CleIdempotence.onCreate → datée (clé DEC-027)")
    void cleIdempotenceDefaults() {
        CleIdempotence c = new CleIdempotence();
        c.onCreate();
        assertThat(c.getDateCreation()).isNotNull();
    }
}
