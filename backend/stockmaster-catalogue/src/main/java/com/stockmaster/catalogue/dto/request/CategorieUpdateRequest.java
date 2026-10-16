package com.stockmaster.catalogue.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * US-029 — corps de {@code PUT /api/v1/categories/{id}}.
 *
 * <p>Sémantique PATCH (endpoint PUT, convention US-018) : champ absent = inchangé.
 * Taux TVA, si fourni, doit être parmi les valeurs supportées ({@code CAT_001}).
 *
 * <p>{@code code} volontairement absent : le code catégorie n'est pas renégociable
 * par cette route (modification de désignation / TVA uniquement).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorieUpdateRequest {

    @NotBlank
    @Size(max = 150)
    private String designation;

    @NotNull
    private BigDecimal tauxTva;
}
