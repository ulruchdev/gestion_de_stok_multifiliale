package com.stockmaster.stock.domain.enums;

/**
 * Types de mouvement du journal de stock — V5 §12.
 *
 * <p>Signe de l'effet sur le stock réel ({@code DEC-023}) :
 * positif = ENTREE, TRANSFERT_ENTREE, ANNULATION_VENTE, REMBOURSEMENT,
 * CORRECTION_POS ; négatif = SORTIE, TRANSFERT_SORTIE, CORRECTION_NEG.</p>
 */
public enum TypeMouvement {
    ENTREE,
    SORTIE,
    CORRECTION_POS,
    CORRECTION_NEG,
    TRANSFERT_ENTREE,
    TRANSFERT_SORTIE,
    ANNULATION_VENTE,
    REMBOURSEMENT
}
