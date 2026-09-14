package com.stockmaster.achat.domain.entity;

import com.stockmaster.achat.domain.entity.CommandeFournisseur;
import com.stockmaster.achat.domain.enums.EtatCommandeFournisseur;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Achat — fondamentaux : machine d'état DEC-006 et défauts @PrePersist")
class AchatFundamentalsTest {

    @Test
    @DisplayName("EtatCommandeFournisseur → COMMANDEE → PARTIELLEMENT_RECUE → RECEPTIONNEE | ANNULEE (DEC-006)")
    void etatCommandeFournisseurValues() {
        assertThat(EtatCommandeFournisseur.values()).extracting(Enum::name)
                .containsExactly("COMMANDEE", "PARTIELLEMENT_RECUE", "RECEPTIONNEE", "ANNULEE");
    }

    @Test
    @DisplayName("CommandeFournisseur.onCreate → datée du jour, état COMMANDEE par défaut")
    void commandeFournisseurDefaults() {
        CommandeFournisseur c = new CommandeFournisseur();
        c.onCreate();
        assertThat(c.getDateCommande()).isNotNull();
        assertThat(c.getEtatCommande()).isEqualTo(EtatCommandeFournisseur.COMMANDEE);
    }

    @Test
    @DisplayName("CommandeFournisseur.onCreate préserve un état explicite (réception partielle)")
    void commandeFournisseurPreservesEtat() {
        CommandeFournisseur c = new CommandeFournisseur();
        c.setEtatCommande(EtatCommandeFournisseur.PARTIELLEMENT_RECUE);
        c.onCreate();
        assertThat(c.getEtatCommande()).isEqualTo(EtatCommandeFournisseur.PARTIELLEMENT_RECUE);
    }
}
