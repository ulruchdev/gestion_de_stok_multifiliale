package com.stockmaster.vente.adapter;

import com.stockmaster.tiers.port.CommandesClientPort;
import com.stockmaster.vente.repository.CommandeClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * US-043 — implémentation vente de {@link CommandesClientPort} (DEC-019).
 *
 * <p>La dépendance est inversée : {@code tiers} déclare le contrat, {@code vente}
 * le réalise (ArchUnit règle 1 — {@code tiers} ne connaît pas {@code vente}).
 * Miroir de {@code CommandesFournisseurAdapter} (US-039).</p>
 *
 * <p><b>Règle métier</b> : toute commande rattachée au client bloque l'archivage,
 * quel que soit son état — {@code existsByClientId} ne filtre pas sur
 * {@code etatCommande}.</p>
 */
@Component
@RequiredArgsConstructor
public class CommandesClientAdapter implements CommandesClientPort {

    private final CommandeClientRepository commandeClientRepository;

    @Override
    public boolean aDesCommandes(Long clientId) {
        return commandeClientRepository.existsByClientId(clientId);
    }
}
