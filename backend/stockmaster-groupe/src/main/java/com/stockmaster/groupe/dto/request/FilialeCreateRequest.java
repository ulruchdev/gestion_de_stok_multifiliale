package com.stockmaster.groupe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Requête US-016 — {@code POST /api/v1/groupe/filiales}.
 */
@Getter
@Setter
public class FilialeCreateRequest {

    @NotBlank(message = "Le nom de la filiale est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "Le code filiale est obligatoire")
    @Size(max = 10, message = "Le code filiale ne doit pas dépasser 10 caractères")
    private String codeFiliale;

    @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
    private String ville;

    @Size(max = 100, message = "Le quartier ne doit pas dépasser 100 caractères")
    private String quartier;
}
