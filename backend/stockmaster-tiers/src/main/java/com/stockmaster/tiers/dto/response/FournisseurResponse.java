package com.stockmaster.tiers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * US-036 — réponse de {@code POST /api/v1/fournisseurs}.
 *
 * <p>{@code entrepriseId} est dérivé de l'association {@code Fournisseur.entreprise}
 * (tenant du JWT) — jamais saisi en entrée.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FournisseurResponse {

    private Long id;
    private String raisonSociale;
    private String nif;
    private String contact;
    private String telephone;
    private String email;
    private String adresseVille;
    private Long entrepriseId;
}
