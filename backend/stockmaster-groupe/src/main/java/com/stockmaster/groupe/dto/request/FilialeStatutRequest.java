package com.stockmaster.groupe.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Requête US-019 — {@code PATCH /api/v1/groupe/filiales/{id}/statut}.
 */
@Getter
@Setter
public class FilialeStatutRequest {

    @NotNull(message = "actif est obligatoire")
    private Boolean actif;
}
