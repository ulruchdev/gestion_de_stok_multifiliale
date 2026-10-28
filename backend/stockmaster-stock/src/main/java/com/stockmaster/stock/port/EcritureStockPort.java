package com.stockmaster.stock.port;

import com.stockmaster.stock.domain.enums.OrigineType;
import com.stockmaster.stock.domain.enums.TypeMouvement;

import java.math.BigDecimal;

/**
 * Port d'écriture du stock — ticket GS-1240.
 *
 * <p>Contrat unique par lequel les modules {@code achat} (réception, US-049),
 * {@code vente} (vente directe, US-064) et les transferts (US-068) créent des
 * mouvements de stock. Les modules ne touchent <b>jamais</b> à
 * {@code stock.repository} ni {@code stock.service} : la règle ArchUnit
 * n°1 ({@code ModuleDependencyArchTest}) n'autorise, pour ce module, que
 * {@code stock.port..} et {@code stock.domain.enums..} (couche contrat).</p>
 *
 * <p>Invariants garantis par l'implémentation ({@code DEC-023} + addendum) :
 * <ul>
 *   <li>stock <b>jamais négatif</b> — toute écriture décrémentante au-delà du
 *       stock réel est refusée en {@code STK_001} (409) ;</li>
 *   <li><b>sérialisation explicite</b> des écritures concurrentes sur le couple
 *       {@code (entreprise, article)} — verrou pessimiste d'advisory-lock
 *       transactionnel, le journal n'ayant aucune ligne de stock à verrouiller
 *       (stock calculé à la volée, ADR-003) ;</li>
 *   <li>journal <b>immuable</b> — insertion seule, aucun UPDATE/DELETE (V5 §12).</li>
 * </ul></p>
 *
 * <p>⚠️ Idempotence {@code DEC-027} : hors périmètre de ce ticket (EPIC 17,
 * US-100 — l'en-tête {@code Idempotency-Key} sera exigé au niveau des
 * endpoints appelants, pas dans ce port).</p>
 */
public interface EcritureStockPort {

    /**
     * Enregistre un mouvement dans le journal et dérive l'effet sur le stock réel.
     *
     * <p>Le sens de l'effet est porté par le {@code typeMouvement} (jamais par le
     * signe de la quantité, toujours &gt; 0 — CHECK en base). Pour les types
     * décrémentants ({@code SORTIE}, {@code TRANSFERT_SORTIE}, {@code CORRECTION_NEG}) :
     * si le stock après écriture passe sous 0 → {@code InsufficientStockException
     * STK_001} (409) et <b>rien n'est écrit</b> (transaction rollback).</p>
     *
     * @return le mouvement créé (id du journal + stock réel après écriture)
     */
    MouvementEnregistre enregistrer(RequeteMouvement requete);

    /**
     * Requête d'écriture — immuable, autoportante : tout ce qu'un module
     * consommateur doit connaître est ici (couche contrat).
     *
     * @param entrepriseId  filiale du mouvement (tenant du JWT) ;
     * @param articleId     article du groupe de cette entreprise ;
     * @param utilisateurId auteur du mouvement (actif, même entreprise) ;
     * @param typeMouvement {ENTREE, SORTIE, CORRECTION_POS, CORRECTION_NEG,
     *                      TRANSFERT_ENTREE, TRANSFERT_SORTIE,
     *                      ANNULATION_VENTE, REMBOURSEMENT} ;
     * @param quantite      toujours &gt; 0 (le sens est porté par le type) ;
     * @param origineType   traçabilité du journal — {@code CORRECTION} obligatoire
     *                      pour les types {@code CORRECTION_POS}/{@code CORRECTION_NEG} ;
     * @param origineId     id du document d'origine (commande, vente, transfert…)
     */
    record RequeteMouvement(
            Long entrepriseId,
            Long articleId,
            Long utilisateurId,
            TypeMouvement typeMouvement,
            BigDecimal quantite,
            OrigineType origineType,
            Long origineId,
            String motif
    ) {
    }

    /**
     * Résultat de l'écriture — {@code stockApres} = somme signée recalculée
     * <b>dans la même transaction</b> que l'insertion (même verrou), pour que
     * l'appelant n'ait jamais à relire le journal.
     *
     * @param mouvementId id du mouvement créé (journal immuable)
     * @param stockApres  stock réel du couple (entreprise, article) après écriture
     */
    record MouvementEnregistre(
            Long mouvementId,
            BigDecimal stockApres
    ) {
    }
}
