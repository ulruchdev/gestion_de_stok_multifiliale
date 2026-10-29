package com.stockmaster.tiers.dto.request;

import com.stockmaster.tiers.validation.TelephoneE164;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * US-038 — corps de {@code PUT /api/v1/fournisseurs/{id}}.
 *
 * <p><b>Sémantique PATCH (convention US-014/US-018/US-024, endpoint PUT)</b> :
 * tous les champs sont optionnels, {@code null} ou blanc = <b>inchangé</b>. Un
 * corps vide est donc valide : il ne modifie rien et renvoie l'état courant.</p>
 *
 * <p><b>Aucun champ tenant</b> : {@code entreprise_id} vient du JWT
 * ({@code StockMasterPrincipal}) et n'est <b>jamais</b> modifiable — un
 * {@code entrepriseId} forcé dans le payload est ignoré par Jackson, un
 * {@code id} ne peut pas être changé (il est dans le chemin).</p>
 *
 * <p><b>6 champs modifiables</b> (les seuls portés par l'entité
 * {@code Fournisseur}) : {@code raisonSociale}, {@code nif}, {@code contact},
 * {@code telephone}, {@code email}, {@code adresseVille}. Tailles alignées sur
 * les colonnes de {@code V1__init_schema.sql}.</p>
 *
 * <p>{@code raisonSociale} reste <b>obligatoire en base</b> : fourni, il ne peut
 * pas être blanc ({@code 400 SYS_005}) ; absent, il est conservé.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModifierFournisseurRequest {

    @Size(max = 200, message = "La raison sociale ne peut pas dépasser 200 caractères")
    @Pattern(regexp = ".*\\S.*", message = "La raison sociale ne peut pas être vide")
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
