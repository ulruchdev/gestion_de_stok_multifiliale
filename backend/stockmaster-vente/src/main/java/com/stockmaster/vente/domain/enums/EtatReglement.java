package com.stockmaster.vente.domain.enums;

/**
 * Règlement d'une commande client — {@code DEC-011 / DEC-020} (V5 §5) :
 * pas d'acompte en V1 (décision assumée) — paiement partiel reste NON_REGLEE.
 * {@code date_reglement} alimente le CA encaissé (distinct du CA facturé).
 */
public enum EtatReglement {
    NON_REGLEE,
    REGLEE
}
