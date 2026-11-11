package com.stockmaster.tiers.port;

/**
 * Port de lecture sur les commandes client — US-043.
 *
 * <p><b>Pourquoi un port et pas un repository (DEC-019 + ArchUnit règle 1)</b> :
 * {@code stockmaster-tiers} ne peut dépendre que de {@code shared} et des
 * packages <em>contrat</em> ({@code domain.entity}, {@code domain.enums},
 * {@code event}, {@code port}). La dépendance est donc <b>inversée</b> :
 * l'interface vit ici (contrat exposé par {@code tiers}), l'implémentation dans
 * {@code stockmaster-vente}
 * ({@code com.stockmaster.vente.adapter.CommandesClientAdapter}) — même
 * mécanisme que {@code CommandesFournisseurPort} (US-039).</p>
 *
 * <p><b>Ce que « historique » veut dire</b> (arbitrage du 9 nov. 2026) : le CA
 * US-043 exige la suppression d'un client « <b>sans historique de commandes</b> ».
 * Contrairement aux fournisseurs (US-039, seules les commandes encore à livrer
 * bloquent), <b>toute</b> commande client bloque l'archivage, quel que soit son
 * état ({@code EN_PREPARATION}, {@code VALIDEE}, {@code LIVREE} ou
 * {@code ANNULEE}) : la trace commerciale est conservée tant que la fiche
 * existe, et une commande annulée reste une commande passée.</p>
 *
 * <p><b>Isolation tenant</b> : le client reçu ici a déjà été chargé et contrôlé
 * sur l'{@code entreprise_id} du JWT par l'appelant — l'id est unique au niveau
 * plateforme, aucune requête de ce port n'est tenant-paramétrable.</p>
 */
public interface CommandesClientPort {

    /**
     * @param clientId identifiant d'un client déjà validé par l'appelant
     * @return {@code true} s'il existe au moins une commande client rattachée,
     *         tous états confondus
     */
    boolean aDesCommandes(Long clientId);
}
