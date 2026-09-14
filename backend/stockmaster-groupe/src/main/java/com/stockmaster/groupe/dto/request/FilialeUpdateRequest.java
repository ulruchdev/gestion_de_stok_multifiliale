package com.stockmaster.groupe.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Requête US-018 — {@code PUT /api/v1/groupe/filiales/{id}}.
 *
 * <p>Modification partielle (sémantique PATCH, même contrat que {@code GroupeUpdateRequest}
 * US-014) : seuls les champs non-{@code null} sont appliqués.</p>
 */
@Getter
@Setter
public class FilialeUpdateRequest {

    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @Size(max = 10, message = "Le code filiale ne doit pas dépasser 10 caractères")
    private String codeFiliale;

    @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
    private String ville;

    @Size(max = 100, message = "Le quartier ne doit pas dépasser 100 caractères")
    private String quartier;
}
