package com.stockmaster.utilisateur.dto.request;

import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * US-022 — corps de {@code POST /api/v1/utilisateurs/employes}.
 *
 * <p>Le champ {@code role} est fourni par l'Admin Filiale mais est validé côté
 * service : seuls {@code GESTIONNAIRE_STOCK}, {@code RESP_ACHATS},
 * {@code COMMERCIAL} et {@code CAISSIER} sont autorisés — jamais un rôle
 * {@code ADMIN_*} (critère d'acceptation US-022).</p>
 *
 * <p>Le mot de passe provisoire est soumis aux mêmes critères que
 * l'inscription (REF §4, DEC-003) : ≥ 8 car, 1 maj, 1 chiffre, 1 spécial.</p>
 */
@Getter
@Setter
public class CreerEmployeRequest {

    @NotBlank
    @Size(max = 100)
    private String prenom;

    @NotBlank
    @Size(max = 100)
    private String nom;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotNull
    private RoleUtilisateur role;

    /**
     * Mot de passe provisoire remis en main propre à l'employé.
     * Mêmes critères que l'inscription : ≥ 8 car, 1 majuscule, 1 chiffre, 1 spécial.
     */
    @NotBlank
    @Size(min = 8, max = 255)
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]).{8,}$",
        message = "Le mot de passe doit contenir au moins 1 majuscule, 1 chiffre et 1 caractère spécial"
    )
    private String motDePasseProvisoire;
}
