package com.stockmaster.tiers.dto.request;

import com.stockmaster.tiers.validation.TelephoneE164;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * US-036 — corps de {@code POST /api/v1/fournisseurs}.
 *
 * <p><b>CA2 : aucun champ tenant.</b> L'{@code entreprise_id} est extrait du
 * JWT ({@code StockMasterPrincipal}) — un {@code entrepriseId} forcé dans le
 * payload est ignoré par Jackson, jamais lu par le service.</p>
 *
 * <p><b>CA3 :</b> {@code raisonSociale} obligatoire, taille max 200
 * (colonne {@code fournisseur.raison_sociale}, V1) ; {@code telephone}
 * optionnel mais au format international E.164 si fourni — règle portée par
 * {@link TelephoneE164}, partagée avec {@code ModifierFournisseurRequest}
 * (US-038) pour ne pas la dupliquer.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreerFournisseurRequest {

    @NotBlank(message = "La raison sociale est obligatoire")
    @Size(max = 200, message = "La raison sociale ne peut pas dépasser 200 caractères")
    private String raisonSociale;

    @Pattern(regexp = TelephoneE164.REGEX, message = TelephoneE164.MESSAGE)
    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String telephone;

    @Size(max = 20, message = "Le NIF ne peut pas dépasser 20 caractères")
    private String nif;

    @Size(max = 100, message = "Le contact ne peut pas dépasser 100 caractères")
    private String contact;

    @Email(message = "Format d'email invalide")
    @Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères")
    private String email;

    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    private String adresseVille;
}
