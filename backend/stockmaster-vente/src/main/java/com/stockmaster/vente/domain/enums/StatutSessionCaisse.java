package com.stockmaster.vente.domain.enums;

/**
 * Session de caisse — {@code DEC-009 / DEC-018} : toute vente est rattachée
 * à une session ouverte ; la clôture (fond constaté vs attendu) est le
 * contrôle anti-perte ({@code ecart = montant_constate − attendu}).
 * Une session ouverte par caissier à la fois (contrôle applicatif).
 */
public enum StatutSessionCaisse {
    OUVERTE,
    CLOTUREE
}
