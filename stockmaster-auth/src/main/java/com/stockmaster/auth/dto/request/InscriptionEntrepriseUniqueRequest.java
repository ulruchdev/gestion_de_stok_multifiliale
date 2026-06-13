package com.stockmaster.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InscriptionEntrepriseUniqueRequest {

    @NotBlank(message = "Le nom de la boutique est obligatoire")
    @Size(max = 100, message = "Le nom de la boutique ne peut pas dépasser 100 caractères")
    private String nomBoutique;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100)
    private String ville;

    @NotBlank(message = "Le quartier est obligatoire")
    @Size(max = 100)
    private String quartier;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 50, message = "Le mot de passe doit contenir entre 8 et 50 caractères")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String motDePasse;
}
