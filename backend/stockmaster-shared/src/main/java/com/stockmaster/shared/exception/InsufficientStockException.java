package com.stockmaster.shared.exception;

import lombok.Getter;

import java.util.List;

/**
 * Exception levée lorsque le stock est insuffisant pour une opération.
 *
 * <p>Utilise le code d'erreur {@code ErrorCode.STK_INSUFFICIENT_STOCK}
 * et retourne un HTTP 409.</p>
 */
@Getter
public class InsufficientStockException extends BusinessException {

    /** Détail des articles en rupture : code, désignation, disponible, demandé. */
    private final transient List<StockShortage> shortages;

    public InsufficientStockException(List<StockShortage> shortages) {
        super(ErrorCode.STK_INSUFFICIENT_STOCK,
              "Stock insuffisant pour %d article(s). Voir les détails dans 'shortages'."
                      .formatted(shortages.size()));
        this.shortages = shortages;
    }

    /**
     * Détail d'une rupture de stock pour un article donné.
     */
    @Getter
    public static class StockShortage {
        private final Long articleId;
        private final String codeArticle;
        private final String designation;
        private final int stockDisponible;
        private final int quantiteDemandee;

        public StockShortage(Long articleId, String codeArticle, String designation,
                             int stockDisponible, int quantiteDemandee) {
            this.articleId = articleId;
            this.codeArticle = codeArticle;
            this.designation = designation;
            this.stockDisponible = stockDisponible;
            this.quantiteDemandee = quantiteDemandee;
        }
    }
}
