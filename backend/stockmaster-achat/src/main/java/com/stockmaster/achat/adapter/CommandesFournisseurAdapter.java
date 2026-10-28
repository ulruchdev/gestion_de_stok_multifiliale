package com.stockmaster.achat.adapter;

import com.stockmaster.achat.domain.enums.EtatCommandeFournisseur;
import com.stockmaster.achat.repository.CommandeFournisseurRepository;
import com.stockmaster.tiers.port.CommandesFournisseurPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

/**
 * Implémentation du port exposé par {@code stockmaster-tiers} — US-039.
 *
 * <p>La dépendance va bien de {@code achat} vers {@code tiers} (déjà déclarée
 * dans le pom et autorisée par ArchUnit) : {@code tiers} n'importe rien d'ici.
 * Le service {@code FournisseurService} ne connaît que l'interface.</p>
 *
 * <p><b>États « actifs »</b> — le seul endroit du code où la règle est écrite :
 * {@code COMMANDEE} et {@code PARTIELLEMENT_RECUE}. Ajouter un état à
 * {@code EtatCommandeFournisseur} sans l'ajouter ici ne bloquera pas
 * l'archivage — c'est pourquoi le test de l'adaptateur verrouille cette liste.</p>
 */
@Component
@RequiredArgsConstructor
public class CommandesFournisseurAdapter implements CommandesFournisseurPort {

    /** États d'une commande qui attend encore une marchandise ({@code DEC-006}). */
    static final Set<EtatCommandeFournisseur> ETATS_ACTIFS =
            EnumSet.of(EtatCommandeFournisseur.COMMANDEE,
                    EtatCommandeFournisseur.PARTIELLEMENT_RECUE);

    private final CommandeFournisseurRepository commandeFournisseurRepository;

    @Override
    public boolean aDesCommandesActives(Long fournisseurId) {
        return commandeFournisseurRepository
                .existsByFournisseurIdAndEtatCommandeInAndSupprimeFalse(fournisseurId, ETATS_ACTIFS);
    }
}
