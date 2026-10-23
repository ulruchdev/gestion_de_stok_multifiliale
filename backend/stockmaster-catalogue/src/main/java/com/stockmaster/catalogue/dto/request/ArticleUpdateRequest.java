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
 * US-034 — corps de {@code PUT /api/v1/articles/{id}}.
 *
 * <p>Pas de champ tenant : le groupe vient du JWT (isolation — DEC-002),
 * jamais du corps de la requête. {@code prixVenteTtc} n'est jamais présent :
 * recalculé côté serveur (DEC-003) si {@code prixVenteHt} ou {@code tauxTva}
 * change. Le code article peut être modifié — l'unicité (group_id,
 * code_article) est re-vérifiée côté service (409 RES_002).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUpdateRequest {

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

    /** Optional : sinon hérite de la (nouvelle) catégorie. */
    private BigDecimal tauxTva;

    /** 0 = alerte désactivée (DEC-023). */
    private BigDecimal seuilAlerte;
}
