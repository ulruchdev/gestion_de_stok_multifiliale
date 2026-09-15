package com.stockmaster.utilisateur.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * US-021 — corps de {@code POST /api/v1/utilisateurs/admin-filiale}.
 * Volontairement SANS champ rôle/scope : le rôle est forcé
 * {@code ADMIN_FILIALE} côté service (critère d'acceptation explicite).
 */
@Getter
@Setter
public class AdminFilialeCreateRequest {

    @NotNull
    private Long filialeId;

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
}
