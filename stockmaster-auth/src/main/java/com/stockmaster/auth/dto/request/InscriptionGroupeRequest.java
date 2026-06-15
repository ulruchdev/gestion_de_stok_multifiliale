package com.stockmaster.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InscriptionGroupeRequest {

    @NotBlank(message = "Le nom du groupe est obligatoire")
    @Size(max = 100, message = "Le nom du groupe ne peut pas dépasser 100 caractères")
    private String nomGroupe;

    @NotBlank(message = "La ville du siège est obligatoire")
    @Size(max = 100)
    private String villesiege;

    @Size(max = 20, message = "Le NIF ne peut pas dépasser 20 caractères")
    @Pattern(
        regexp = "^[A-Za-z0-9/-]*$",
        message = "Le format du NIF est invalide"
    )
    private String nif;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String telephone;

    @NotBlank(message = "L'email de l'entreprise est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 150)
    private String emailEntreprise;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "L'email de l'administrateur est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 150)
    private String emailAdmin;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 50, message = "Le mot de passe doit contenir entre 8 et 50 caractères")
    @Pattern(
        regexp = "^(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String motDePasse;
}
