package com.stockmaster.achat.adapter;

import com.stockmaster.achat.domain.enums.EtatCommandeFournisseur;
import com.stockmaster.achat.repository.CommandeFournisseurRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US-039 — adaptateur du port {@code tiers.port.CommandesFournisseurPort}.
 *
 * <p>Ce test verrouille la <b>seule</b> définition d'une commande « active » :
 * {@code COMMANDEE} et {@code PARTIELLEMENT_RECUE}. C'est ce jeu d'états qui
 * autorise ou refuse l'archivage d'un fournisseur — s'il dérive, la règle
 * métier dérive silencieusement.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommandesFournisseurAdapter — US-039 : quelles commandes bloquent l'archivage")
class CommandesFournisseurAdapterTest {

    private static final Long FOURNISSEUR_ID = 400L;

    @Mock
    private CommandeFournisseurRepository commandeFournisseurRepository;

    @InjectMocks
    private CommandesFournisseurAdapter adapter;

    @Test
    @DisplayName("✅ true — une commande est encore à livrer")
    void shouldRetournerVraiQuandCommandeActive() {
        when(commandeFournisseurRepository
                .existsByFournisseurIdAndEtatCommandeInAndSupprimeFalse(
                        eq(FOURNISSEUR_ID), any()))
                .thenReturn(true);

        assertThat(adapter.aDesCommandesActives(FOURNISSEUR_ID)).isTrue();
    }

    @Test
    @DisplayName("✅ false — aucune commande active (reçues et annulées ne comptent pas)")
    void shouldRetournerFauxQuandAucuneCommandeActive() {
        when(commandeFournisseurRepository
                .existsByFournisseurIdAndEtatCommandeInAndSupprimeFalse(
                        eq(FOURNISSEUR_ID), any()))
                .thenReturn(false);

        assertThat(adapter.aDesCommandesActives(FOURNISSEUR_ID)).isFalse();
    }

    @Test
    @DisplayName("✅ les états interrogés sont exactement COMMANDEE et PARTIELLEMENT_RECUE")
    @SuppressWarnings("unchecked")
    void shouldInterrogerUniquementLesEtatsActifs() {
        when(commandeFournisseurRepository
                .existsByFournisseurIdAndEtatCommandeInAndSupprimeFalse(
                        eq(FOURNISSEUR_ID), any()))
                .thenReturn(false);

        adapter.aDesCommandesActives(FOURNISSEUR_ID);

        verify(commandeFournisseurRepository)
                .existsByFournisseurIdAndEtatCommandeInAndSupprimeFalse(
                        eq(FOURNISSEUR_ID),
                        argThat(etats -> etats.size() == 2
                                && etats.containsAll(java.util.List.of(
                                        EtatCommandeFournisseur.COMMANDEE,
                                        EtatCommandeFournisseur.PARTIELLEMENT_RECUE))));
    }

    private static Collection<EtatCommandeFournisseur> argThat(
            java.util.function.Predicate<Collection<EtatCommandeFournisseur>> predicat) {
        return org.mockito.ArgumentMatchers.<Collection<EtatCommandeFournisseur>>argThat(
                predicat::test);
    }
}
