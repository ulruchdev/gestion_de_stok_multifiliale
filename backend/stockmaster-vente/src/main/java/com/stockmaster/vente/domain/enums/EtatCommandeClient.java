package com.stockmaster.vente.domain.enums;

/**
 * Machine à états de la commande client — {@code DEC-006} (V5 §5) :
 * {@code EN_PREPARATION → VALIDEE → LIVREE}, sortie {@code ANNULEE}.
 *
 * <p>Distinct de la vente directe (caisse) : la commande client est servie plus
 * tard et le règlement est suivi séparément ({@code DEC-011}, pas d'acompte en V1).</p>
 */
public enum EtatCommandeClient {
    EN_PREPARATION,
    VALIDEE,
    LIVREE,
    ANNULEE
}
