package com.stockmaster.stock.dto.response;

import com.stockmaster.stock.domain.enums.OrigineType;
import com.stockmaster.stock.domain.enums.TypeMouvement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * US-052 — ligne de {@code GET /api/v1/stock/articles/{articleId}/mouvements} :
 * un mouvement du journal immuable {@code mouvement_stock}.
 *
 * <p>{@code utilisateurNom} = « prénom nom » de l'auteur du mouvement
 * (critère US-052 : retracer qui a fait quoi, et d'où ça vient).</p>
 */
@Getter
@Builder
@AllArgsConstructor
public class MouvementStockResponse {

    private Long id;
    private TypeMouvement typeMouvement;
    private BigDecimal quantite;
    private Instant dateMouvement;
    private Long utilisateurId;
    private String utilisateurNom;
    private OrigineType origineType;
    private Long origineId;
    private String motif;
}
