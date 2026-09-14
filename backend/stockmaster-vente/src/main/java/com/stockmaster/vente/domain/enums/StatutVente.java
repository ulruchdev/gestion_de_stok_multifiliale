package com.stockmaster.vente.domain.enums;

/**
 * Statut d'une vente — {@code DEC-006 / DEC-010 / DEC-018} (V5 §10) :
 * <pre>PAYEE → ANNULEE | REMBOURSEE</pre>
 * Jamais de {@code VALIDEE} pour une vente directe : la vente à la caisse est
 * encaissée au moment de la création. L'annulation/restauration du stock passe
 * par les types de mouvement {@code ANNULATION_VENTE} / {@code REMBOURSEMENT} (V5 §12).
 */
public enum StatutVente {
    PAYEE,
    ANNULEE,
    REMBOURSEE
}
