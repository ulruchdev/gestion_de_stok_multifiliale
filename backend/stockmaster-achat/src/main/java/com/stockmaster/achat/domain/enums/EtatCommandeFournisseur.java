package com.stockmaster.achat.domain.enums;

/**
 * Machine à états de la commande fournisseur — {@code DEC-006} (V5 §4) :
 * <pre>COMMANDEE → PARTIELLEMENT_RECUE → RECEPTIONNEE
 *               ↘ ANNULEE (depuis COMMANDEE ou PARTIELLEMENT_RECUE)</pre>
 *
 * <p>Les anciens états V1 (EN_PREPARATION, VALIDEE, LIVREE) sont migrés par
 * V5 : LIVREE → RECEPTIONNEE, EN_PREPARATION/VALIDEE → COMMANDEE.
 * La réception physique crée l'entrée de stock (module stock, DEC-020).</p>
 */
public enum EtatCommandeFournisseur {
    COMMANDEE,
    PARTIELLEMENT_RECUE,
    RECEPTIONNEE,
    ANNULEE
}
