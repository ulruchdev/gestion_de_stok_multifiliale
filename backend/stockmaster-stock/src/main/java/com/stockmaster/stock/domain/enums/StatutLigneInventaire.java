package com.stockmaster.stock.domain.enums;

/**
 * État d'une ligne d'inventaire — {@code DEC-036} (V5 §15). Une ligne refusée
 * (écart négatif hors tolérance) est marquée {@code A_RECOMPTER} et jamais
 * appliquée en partie.
 */
public enum StatutLigneInventaire {
    A_COMPTER,
    COMPTEE,
    A_RECOMPTER,
    VALIDE
}
