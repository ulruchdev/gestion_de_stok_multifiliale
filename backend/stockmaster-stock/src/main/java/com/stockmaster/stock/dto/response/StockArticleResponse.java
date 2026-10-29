package com.stockmaster.stock.dto.response;

import com.stockmaster.stock.domain.enums.StatutAlerte;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * US-051 — ligne de {@code GET /api/v1/stock} : le stock réel d'un article
 * dans l'entreprise du JWT.
 *
 * <p>Le stock est calculé à la volée depuis {@code mouvement_stock}
 * (REF §4.2), jamais dénormalisé.</p>
 */
@Getter
@Builder
@AllArgsConstructor
public class StockArticleResponse {

    private Long articleId;
    private String codeArticle;
    private String designation;
    private BigDecimal stockActuel;
    private BigDecimal seuilAlerte;
    private StatutAlerte statutAlerte;
}
