package com.stockmaster.stock.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Énumérations stock — carte des signes DEC-023 et machine d'état DEC-007")
class StockEnumsTest {

    @Test
    @DisplayName("TypeMouvement → 8 types, signe appliqué par CalculStockService (DEC-023)")
    void typeMouvementValues() {
        assertThat(TypeMouvement.values())
                .extracting(Enum::name)
                .containsExactly("ENTREE", "SORTIE", "CORRECTION_POS", "CORRECTION_NEG",
                        "TRANSFERT_ENTREE", "TRANSFERT_SORTIE", "ANNULATION_VENTE", "REMBOURSEMENT");
    }

    @Test
    @DisplayName("OrigineType → 6 origines tracées dans le journal immuable")
    void origineTypeValues() {
        assertThat(OrigineType.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("COMMANDE_FOURNISSEUR", "COMMANDE_CLIENT", "VENTE",
                        "CORRECTION", "TRANSFERT", "ANNULATION_VENTE");
    }

    @Test
    @DisplayName("StatutTransfert → machine d'état DEC-007 : DEMANDE → VALIDE → EN_TRANSIT → RECU (+ ECART, REFUSE, ANNULE)")
    void statutTransfertValues() {
        assertThat(StatutTransfert.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("DEMANDE", "VALIDE", "EN_TRANSIT", "RECU",
                        "ECART", "REFUSE", "ANNULE");
    }

    @Test
    @DisplayName("StatutSessionInventaire / StatutLigneInventaire → cycle inventaire DEC-036")
    void inventoryStatusValues() {
        assertThat(StatutSessionInventaire.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("OUVERTE", "VALIDE");
        assertThat(StatutLigneInventaire.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("A_COMPTER", "COMPTEE", "A_RECOMPTER", "VALIDE");
    }
}
