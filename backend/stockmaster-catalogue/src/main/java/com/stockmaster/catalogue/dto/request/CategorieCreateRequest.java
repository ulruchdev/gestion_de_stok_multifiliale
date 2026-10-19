package com.stockmaster.catalogue.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * US-027 — corps de {@code POST /api/v1/categories}.
 *
 * <p>Pas de champ tenant : le groupe vient du JWT (isolation — critère
 * US-027), jamais du corps de la requête.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategorieCreateRequest {

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String designation;

    @NotNull
    private BigDecimal tauxTva;
}
