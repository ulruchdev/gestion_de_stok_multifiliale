package com.stockmaster.vente.adapter;

import com.stockmaster.vente.repository.CommandeClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US-043 — adaptateur du port {@code tiers.port.CommandesClientPort}.
 *
 * <p>Le CA de la US est « supprimer un client <b>sans historique de
 * commandes</b> » : <b>toute</b> commande existante bloque l'archivage, quel
 * que soit son état (y compris {@code ANNULEE}) — arbitrage du 9 nov. 2026,
 * distinct du choix retenu pour les fournisseurs (US-039).</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommandesClientAdapter — US-043 : quelles commandes bloquent l'archivage")
class CommandesClientAdapterTest {

    private static final Long CLIENT_ID = 400L;

    @Mock
    private CommandeClientRepository commandeClientRepository;

    @InjectMocks
    private CommandesClientAdapter adapter;

    @Test
    @DisplayName("✅ true — au moins une commande existe pour le client")
    void shouldRetournerVraiQuandCommandeExiste() {
        when(commandeClientRepository.existsByClientId(CLIENT_ID)).thenReturn(true);

        assertThat(adapter.aDesCommandes(CLIENT_ID)).isTrue();
        verify(commandeClientRepository).existsByClientId(CLIENT_ID);
    }

    @Test
    @DisplayName("✅ false — aucune commande, tous états confondus")
    void shouldRetournerFauxQuandAucuneCommande() {
        when(commandeClientRepository.existsByClientId(CLIENT_ID)).thenReturn(false);

        assertThat(adapter.aDesCommandes(CLIENT_ID)).isFalse();
        verify(commandeClientRepository).existsByClientId(CLIENT_ID);
    }
}
