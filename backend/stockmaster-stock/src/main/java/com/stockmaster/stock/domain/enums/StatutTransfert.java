package com.stockmaster.stock.domain.enums;

/**
 * Machine à états du transfert inter-filiales — {@code DEC-002 / DEC-007} (V5 §8) :
 * <pre>DEMANDE → VALIDE → EN_TRANSIT → RECU | ECART
 *           ↘ REFUSE     ↘ ANNULE</pre>
 *
 * <p>L'écart (expédié − reçu) est porté ligne par ligne ({@code ligne_transfert}).
 * Le stock sort à l'expédition et entre à la réception (TRANSFERT_SORTIE /
 * TRANSFERT_ENTREE).</p>
 */
public enum StatutTransfert {
    DEMANDE,
    VALIDE,
    EN_TRANSIT,
    RECU,
    ECART,
    REFUSE,
    ANNULE
}
