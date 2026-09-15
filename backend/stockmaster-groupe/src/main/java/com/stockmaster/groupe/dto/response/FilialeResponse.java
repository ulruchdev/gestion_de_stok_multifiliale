package com.stockmaster.groupe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Réponse US-016 (créer) / US-017 (lister) — filiale du groupe.
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

    /** US-017 : nombre d'employés de la filiale (0 à la création — US-016). */
    private Long nombreEmployes;
}
