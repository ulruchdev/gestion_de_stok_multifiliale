package com.stockmaster.vente.domain.enums;

/**
 * Mode de paiement — {@code DEC-009} : paiement mixte à la caisse.
 * Le Mobile Money (MTN / Orange) est le canal dominant au Cameroun.
 */
public enum ModePaiement {
    ESPECES,
    MOBILE_MONEY,
    CARTE
}
