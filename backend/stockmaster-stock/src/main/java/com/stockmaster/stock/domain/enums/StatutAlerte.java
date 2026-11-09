package com.stockmaster.stock.domain.enums;

import java.math.BigDecimal;

/**
 * Statut d'alerte d'un article — REF §4.2 (US-051).
 *
 * <ul>
 *   <li>{@code ANOMALIE} — stock &lt; 0 (erreur de donnée, jamais atteint en V1)</li>
 *   <li>{@code RUPTURE} — stock ≤ 0</li>
 *   <li>{@code BAS} — 0 &lt; stock ≤ seuil (seuil 0 = alerte désactivée)</li>
 *   <li>{@code NORMAL} — sinon</li>
 * </ul>
 *
 * <p>La même dérivation est appliquée en SQL dans la requête paginée
 * {@code listerStockParArticle} — les deux doivent rester alignées (REF §4.2
 * est l'autorité).</p>
 */
public enum StatutAlerte {
    NORMAL, BAS, RUPTURE, ANOMALIE;

    /**
     * Dérive le statut à partir du stock réel et du seuil d'alerte de l'article.
     *
     * @param stock stock réel (peut être négatif : état ANOMALIE)
     * @param seuil {@code article.seuil_alerte} — 0 = alerte désactivée
     * @return le statut conforme au REF §4.2
     * @throws IllegalArgumentException si {@code stock} ou {@code seuil} est null
     */
    public static StatutAlerte de(BigDecimal stock, BigDecimal seuil) {
        if (stock == null || seuil == null) {
            throw new IllegalArgumentException("stock et seuil sont obligatoires");
        }
        if (stock.signum() < 0) {
            return ANOMALIE;
        }
        if (stock.signum() == 0) {
            return RUPTURE;
        }
        if (seuil.signum() > 0 && stock.compareTo(seuil) <= 0) {
            return BAS;
        }
        return NORMAL;
    }
}
