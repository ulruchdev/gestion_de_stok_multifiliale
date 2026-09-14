package com.stockmaster.stock.domain.enums;

/**
 * Type d'origine d'un mouvement de stock — V5 §12 (traçabilité du journal).
 * {@code ANNULATION_VENTE} désigne le mouvement généré par l'annulation
 * d'une vente ({@code DEC-010}) ; l'{@code origine_id} pointe alors vers la vente.
 */
public enum OrigineType {
    COMMANDE_FOURNISSEUR,
    COMMANDE_CLIENT,
    VENTE,
    CORRECTION,
    TRANSFERT,
    ANNULATION_VENTE
}
