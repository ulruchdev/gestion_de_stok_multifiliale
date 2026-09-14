package com.stockmaster.stock.service;

import com.stockmaster.stock.domain.enums.TypeMouvement;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Règle unique de calcul du stock réel — {@code DEC-023} (V5 §12).
 *
 * <p>Somme <strong>signée</strong> des mouvements : le sens de l'effet est porté
 * par le {@code type_mouvement}, jamais par le signe de la quantité (la colonne
 * est toujours &gt; 0, CHECK en base). C'est la définition unique de "rupture" :
 * {@code stock_reel <= seuil_alerte} (seuil 0 = alerte désactivée), et le stock
 * ne peut jamais devenir négatif — le service d'écriture (à implémenter par les
 * développeurs, US-0XX) DOIT bloquer l'opération avec {@code STK_INSUFFICIENT_STOCK}
 * (409) et poser un verrou par (entreprise, article) en concurrence.</p>
 */
@Service
public class CalculStockService {

    /** Effet du mouvement sur le stock : +1, -1 ou 0 (type inconnu → rejeté par le CHECK). */
    public static int signeDe(TypeMouvement type) {
        return switch (type) {
            case ENTREE, TRANSFERT_ENTREE, ANNULATION_VENTE, REMBOURSEMENT,
                 CORRECTION_POS -> 1;
            case SORTIE, TRANSFERT_SORTIE, CORRECTION_NEG -> -1;
        };
    }

    /**
     * Applique un mouvement à un stock courant — utilise {@link #signeDe(TypeMouvement)}.
     * Le résultat peut être négatif : c'est justement ce que l'écriture doit
     * détecter pour refuser l'opération (invariant DEC-023).
     */
    public BigDecimal appliquer(BigDecimal stockCourant, TypeMouvement type, BigDecimal quantite) {
        if (stockCourant == null || quantite == null) {
            throw new IllegalArgumentException("stockCourant et quantite sont obligatoires");
        }
        if (quantite.signum() <= 0) {
            throw new IllegalArgumentException("quantite doit être > 0 (CHECK en base)");
        }
        return stockCourant.add(quantite.multiply(BigDecimal.valueOf(signeDe(type))));
    }

    /** Définition unique de la rupture : stock réel ≤ seuil (seuil 0 ⇒ jamais d'alerte). */
    public boolean estEnRupture(BigDecimal stockReel, BigDecimal seuilAlerte) {
        if (stockReel == null || seuilAlerte == null) {
            return false;
        }
        if (seuilAlerte.signum() <= 0) {
            return false; // article.seuil_alerte = 0 → alerte désactivée (V1)
        }
        return stockReel.compareTo(seuilAlerte) <= 0;
    }

    /** Un stock réel négatif est un état impossible : à bloquer à l'écriture, jamais à corriger ici. */
    public boolean violInvariantNonNegativite(BigDecimal stockApres) {
        return stockApres.signum() < 0;
    }
}
