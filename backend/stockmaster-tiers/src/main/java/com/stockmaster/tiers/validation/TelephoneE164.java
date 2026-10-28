package com.stockmaster.tiers.validation;

/**
 * Format de téléphone des tiers — E.164 international (ex. {@code +237691234567}).
 *
 * <p><b>Source unique de la règle</b> : la regex était écrite en dur dans
 * {@code CreerFournisseurRequest} (US-036). US-038 doit valider le même format
 * sur un second DTO : la règle est extraite ici pour ne pas la dupliquer
 * (deux copies divergent toujours).</p>
 *
 * <p>La regex reste <b>facultative</b> ({@code ?} final) : le téléphone est
 * optionnel côté {@code fournisseur}, mais s'il est fourni il doit être
 * international — un numéro local à 9 chiffres ({@code 691234567}) est refusé.</p>
 *
 * <p>Portée volontairement limitée au module {@code stockmaster-tiers} : aucun
 * autre module n'est modifié par US-038.</p>
 */
public final class TelephoneE164 {

    /** Regex E.164 — {@code +}, indicatif 1-9 puis 1 à 14 chiffres ; chaîne vide acceptée. */
    public static final String REGEX = "^(\\+[1-9]\\d{1,14})?$";

    public static final String MESSAGE =
            "Le numéro de téléphone doit être au format international (ex: +237691234567)";

    private TelephoneE164() {
        // classe de constantes — non instanciable
    }
}
