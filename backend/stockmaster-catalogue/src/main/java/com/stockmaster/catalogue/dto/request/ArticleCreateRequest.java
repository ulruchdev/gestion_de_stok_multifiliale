package com.stockmaster.catalogue.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * US-031 — corps de {@code POST /api/v1/articles}.
 *
 * <p>Pas de champ tenant : le groupe vient du JWT (isolation — critère
 * US-031), jamais du corps. Le {@code tauxTva} est nullable : absent, il est
 * hérité de la catégorie. Le TTC n'est jamais dans le corps (calculé côté
 * serveur, DEC-003).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCreateRequest {

    @NotBlank
    @Size(max = 30)
    private String codeArticle;

    @NotBlank
    @Size(max = 150)
    private String designation;

    @NotNull
    private Long categorieId;

    @NotNull
    @PositiveOrZero
    private Integer prixAchatHt;

    @NotNull
    @PositiveOrZero
    private Integer prixVenteHt;

    /** Nullable = hérité de la catégorie (US-031). */
    private BigDecimal tauxTva;

    /** Nullable = 0 (alerte désactivée, DEC-023). */
    @PositiveOrZero
    private BigDecimal seuilAlerte;
}
