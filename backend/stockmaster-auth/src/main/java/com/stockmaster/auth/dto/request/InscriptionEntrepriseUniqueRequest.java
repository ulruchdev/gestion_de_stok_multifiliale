package com.stockmaster.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InscriptionEntrepriseUniqueRequest {

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Size(max = 100, message = "Le nom de l'entreprise ne peut pas dépasser 100 caractères")
    private String nomEntreprise;

    @Size(max = 20, message = "Le NIF ne peut pas dépasser 20 caractères")
    private String nif;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(max = 16, message = "Le téléphone ne peut pas dépasser 16 caractères")
    @Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "Le numéro de téléphone doit être au format international (ex: +237691234567)"
    )
    private String telephone;

    @NotBlank(message = "Le nom de l'administrateur est obligatoire")
    @Size(max = 100)
    private String adminNom;

    @NotBlank(message = "Le prénom de l'administrateur est obligatoire")
    @Size(max = 100)
    private String adminPrenom;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 50, message = "Le mot de passe doit contenir entre 8 et 50 caractères")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String motDePasse;
}
