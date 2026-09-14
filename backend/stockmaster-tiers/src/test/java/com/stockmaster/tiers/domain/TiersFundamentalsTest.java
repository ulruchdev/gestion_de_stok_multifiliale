package com.stockmaster.tiers.domain;

import com.stockmaster.tiers.domain.entity.Client;
import com.stockmaster.tiers.domain.entity.Fournisseur;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tiers — fondamentaux : Client / Fournisseur et soft delete hérité")
class TiersFundamentalsTest {

    @Test
    @DisplayName("Client → champs portés, actif à la création, soft delete hérité d'AbstractEntity")
    void clientLifecycle() {
        Client c = new Client();
        c.setNom("Ngoma");
        c.setPrenom("Aline");
        c.setTelephone("+237600000001");
        c.setEmail("aline.ngoma@example.cm");

        assertThat(c.isActif()).isTrue();

        c.marquerCommeSupprime();
        assertThat(c.getSupprime()).isTrue();
        assertThat(c.isActif()).isFalse();
        assertThat(c.getNom()).isEqualTo("Ngoma");
        assertThat(c.getPrenom()).isEqualTo("Aline");
    }

    @Test
    @DisplayName("Fournisseur → raison sociale + NIF portés (unicité NIF contrôlée en repository)")
    void fournisseurFields() {
        Fournisseur f = new Fournisseur();
        f.setRaisonSociale("SARL Import Golfe");
        f.setNif("M0126000001");

        assertThat(f.getRaisonSociale()).isEqualTo("SARL Import Golfe");
        assertThat(f.getNif()).isEqualTo("M0126000001");
        assertThat(f.isActif()).isTrue();
    }
}
