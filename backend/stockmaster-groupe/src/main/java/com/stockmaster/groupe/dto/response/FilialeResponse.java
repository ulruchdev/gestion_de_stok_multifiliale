package com.stockmaster.groupe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Réponse US-016 — filiale créée dans le groupe.
 */
@Getter
@Builder
@AllArgsConstructor
public class FilialeResponse {

    private Long id;

    private String nom;

    private String codeFiliale;

    private String ville;

    private String quartier;

    private Boolean actif;

    private Boolean siteOperationnel;

    /** Id de la maison mère du groupe. */
    private Long parentId;
}
