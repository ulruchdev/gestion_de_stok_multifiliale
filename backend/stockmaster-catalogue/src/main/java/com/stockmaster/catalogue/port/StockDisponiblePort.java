package com.stockmaster.catalogue.port;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * Port de lecture du stock disponible par article — US-032/US-033.
 *
 * <p><b>Pourquoi un port (DEC-019 + ArchUnit règle 1)</b> : le catalogue est
 * <b>group-scoped</b> et ne peut pas dépendre du module {@code stock} (la
 * dépendance de compilation va dans l'autre sens : {@code stock} →
 * {@code catalogue}). L'interface vit donc ici, dans la couche contrat exposée
 * par le catalogue, et l'implémentation dans
 * {@code com.stockmaster.stock.adapter.StockDisponibleAdapter} — même mécanisme
 * que {@code tiers.port.CommandesClientPort} (US-043).</p>
 *
 * <p><b>Tenant</b> : {@code entrepriseId} provient exclusivement du JWT de
 * l'appelant, jamais du corps ni d'un paramètre de requête.</p>
 *
 * <p><b>Source de vérité</b> : le stock est calculé à la volée depuis le
 * journal des mouvements (DEC-023) et le statut d'alerte est dérivé par
 * l'implémentation selon le REF §4.2 — le catalogue ne réimplémente aucune des
 * deux règles, il se contente de recopier le résultat.</p>
 */
public interface StockDisponiblePort {

    /**
     * Stock réel et statut d'alerte de plusieurs articles d'une même filiale,
     * en une seule requête. Les articles sans mouvement ne sont pas
     * nécessairement présents dans la map : l'appelant traite l'absence comme
     * « non disponible » (champs {@code null} dans la réponse).
     *
     * @param entrepriseId filiale du JWT
     * @param articleIds   identifiants d'articles déjà contrôlés par l'appelant
     * @return map {@code articleId → ligne de stock}, vide si la liste est vide
     */
    Map<Long, LigneStock> stockParArticle(Long entrepriseId, Collection<Long> articleIds);

    /**
     * Ligne de stock — {@code statutAlerte} reprend le nom de l'énumération
     * {@code StatutAlerte} du module stock ({@code NORMAL}, {@code BAS},
     * {@code RUPTURE}, {@code ANOMALIE}) afin que le catalogue n'ait pas à
     * dépendre de ce module.
     */
    record LigneStock(BigDecimal stockActuel, String statutAlerte) {
    }
}
