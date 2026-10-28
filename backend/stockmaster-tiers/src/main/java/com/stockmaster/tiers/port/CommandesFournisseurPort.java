package com.stockmaster.tiers.port;

/**
 * Port de lecture sur les commandes fournisseur — US-039.
 *
 * <p><b>Pourquoi un port et pas un repository (DEC-019 + ArchUnit règle 1)</b> :
 * {@code stockmaster-tiers} n'a le droit de dépendre que de {@code shared} et des
 * packages <em>contrat</em> ({@code domain.entity}, {@code domain.enums},
 * {@code event}, {@code port}). Importer
 * {@code com.stockmaster.achat.repository.CommandeFournisseurRepository} ferait
 * échouer {@code ModuleDependencyArchTest} et violerait l'encapsulation : la
 * dépendance est donc <b>inversée</b> — l'interface vit ici (contrat exposé par
 * {@code tiers}), l'implémentation vit dans {@code stockmaster-achat}
 * ({@code com.stockmaster.achat.adapter.CommandesFournisseurAdapter}).
 * Même mécanisme que {@code notification.port.CanalNotification} ({@code DEC-014}).</p>
 *
 * <p><b>Ce que « active » veut dire</b> (arbitrage du 28 oct. 2026, résolution de
 * la contradiction CA7 ⟂ CA10) : une commande est active tant qu'elle attend une
 * marchandise — {@code COMMANDEE} ou {@code PARTIELLEMENT_RECUE} ({@code DEC-006}).
 * Une commande {@code RECEPTIONNEE} ou {@code ANNULEE} est close et n'empêche
 * pas l'archivage du fournisseur (CA5).</p>
 *
 * <p><b>Isolation tenant</b> : le fournisseur reçu ici a déjà été chargé et
 * contrôlé sur l'{@code entreprise_id} du JWT par l'appelant — l'id est unique
 * au niveau plateforme, aucune requête de ce port n'est donc tenant-paramétrable.</p>
 */
public interface CommandesFournisseurPort {

    /**
     * @param fournisseurId identifiant d'un fournisseur déjà validé par l'appelant
     * @return {@code true} s'il existe au moins une commande non close
     *         ({@code COMMANDEE} ou {@code PARTIELLEMENT_RECUE}, non supprimée)
     */
    boolean aDesCommandesActives(Long fournisseurId);
}
