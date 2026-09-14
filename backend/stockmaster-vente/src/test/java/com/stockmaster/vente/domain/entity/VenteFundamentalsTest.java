package com.stockmaster.vente.domain.entity;

import com.stockmaster.vente.domain.entity.CommandeClient;
import com.stockmaster.vente.domain.entity.SessionCaisse;
import com.stockmaster.vente.domain.entity.Vente;
import com.stockmaster.vente.domain.enums.EtatCommandeClient;
import com.stockmaster.vente.domain.enums.EtatReglement;
import com.stockmaster.vente.domain.enums.ModePaiement;
import com.stockmaster.vente.domain.enums.StatutSessionCaisse;
import com.stockmaster.vente.domain.enums.StatutVente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Vente — fondamentaux : énumérations (DEC-009/010/011) et défauts @PrePersist")
class VenteFundamentalsTest {

    @Test
    @DisplayName("StatutVente → PAYEE / ANNULEE / REMBOURSEE (annulation via mouvements V5 §12)")
    void statutVenteValues() {
        assertThat(StatutVente.values()).extracting(Enum::name)
                .containsExactly("PAYEE", "ANNULEE", "REMBOURSEE");
    }

    @Test
    @DisplayName("ModePaiement → ESPECES / MOBILE_MONEY / CARTE (paiement mixte DEC-009)")
    void modePaiementValues() {
        assertThat(ModePaiement.values()).extracting(Enum::name)
                .containsExactly("ESPECES", "MOBILE_MONEY", "CARTE");
    }

    @Test
    @DisplayName("Machine commande client DEC-011 et règlement DEC-020")
    void commandeClientEnumValues() {
        assertThat(EtatCommandeClient.values()).extracting(Enum::name)
                .containsExactly("EN_PREPARATION", "VALIDEE", "LIVREE", "ANNULEE");
        assertThat(EtatReglement.values()).extracting(Enum::name)
                .containsExactly("NON_REGLEE", "REGLEE");
    }

    @Test
    @DisplayName("SessionCaisse → OUVERTE / CLOTUREE (une session ouverte par caissier)")
    void statutSessionCaisseValues() {
        assertThat(StatutSessionCaisse.values()).extracting(Enum::name)
                .containsExactly("OUVERTE", "CLOTUREE");
    }

    @Test
    @DisplayName("Vente.onCreate → horodatée, statut PAYEE par défaut")
    void venteDefaults() {
        Vente v = new Vente();
        v.onCreate();
        assertThat(v.getDateVente()).isNotNull();
        assertThat(v.getStatut()).isEqualTo(StatutVente.PAYEE);
    }

    @Test
    @DisplayName("SessionCaisse.onCreate → OUVERTE, fond de caisse 0 par défaut")
    void sessionCaisseDefaults() {
        SessionCaisse s = new SessionCaisse();
        s.onCreate();
        assertThat(s.getDateOuverture()).isNotNull();
        assertThat(s.getStatut()).isEqualTo(StatutSessionCaisse.OUVERTE);
        assertThat(s.getFondCaisse()).isEqualTo(0);
    }

    @Test
    @DisplayName("SessionCaisse.onCreate préserve un fond de caisse explicite")
    void sessionCaissePreservesFondCaisse() {
        SessionCaisse s = new SessionCaisse();
        s.setFondCaisse(25_000);
        s.onCreate();
        assertThat(s.getFondCaisse()).isEqualTo(25_000);
    }

    @Test
    @DisplayName("CommandeClient.onCreate → EN_PREPARATION + NON_REGLEE, date du jour")
    void commandeClientDefaults() {
        CommandeClient c = new CommandeClient();
        c.onCreate();
        assertThat(c.getDateCommande()).isNotNull();
        assertThat(c.getEtatCommande()).isEqualTo(EtatCommandeClient.EN_PREPARATION);
        assertThat(c.getEtatReglement()).isEqualTo(EtatReglement.NON_REGLEE);
    }
}
