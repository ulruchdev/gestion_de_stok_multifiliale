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
 * US-040 — corps de {@code POST /api/v1/clients}.
 *
 * <p><b>Aucun champ tenant</b> : l'{@code entreprise_id} est extrait du JWT
 * ({@code StockMasterPrincipal}) — un {@code entrepriseId} forcé dans le
 * payload est ignoré par Jackson, jamais lu par le service.</p>
 *
 * <p><b>Validation</b> : {@code nom} obligatoire (max 100 = colonne V1) ;
 * {@code telephone} optionnel mais au format E.164 si fourni — règle portée par
 * {@link TelephoneE164}, partagée avec {@code ModifierClientRequest} (US-042) ;
 * tailles alignées sur {@code V1__init_schema.sql} (client).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreerClientRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    private String prenom;

    @Pattern(regexp = TelephoneE164.REGEX, message = TelephoneE164.MESSAGE)
    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String telephone;

    @Email(message = "Format d'email invalide")
    @Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères")
    private String email;

    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    private String adresseVille;

    @Size(max = 100, message = "Le quartier ne peut pas dépasser 100 caractères")
    private String adresseQuartier;
}
