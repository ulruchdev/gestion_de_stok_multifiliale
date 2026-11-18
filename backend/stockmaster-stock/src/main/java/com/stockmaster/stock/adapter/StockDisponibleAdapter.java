package com.stockmaster.stock.adapter;

import com.stockmaster.catalogue.port.StockDisponiblePort;
import com.stockmaster.stock.repository.MouvementStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation stock de {@link StockDisponiblePort} — US-032/US-033.
 *
 * <p>Le catalogue affiche le stock réel et le badge d'alerte sans jamais
 * interroger le journal des mouvements : cet adaptateur est le seul point de
 * lecture, et il réutilise la requête agrégée du module stock (même CASE
 * {@code StatutAlerte} que {@code listerStockParArticle}, REF §4.2).</p>
 */
@Component
@RequiredArgsConstructor
public class StockDisponibleAdapter implements StockDisponiblePort {

    private final MouvementStockRepository mouvementStockRepository;

    @Override
    public Map<Long, LigneStock> stockParArticle(Long entrepriseId, Collection<Long> articleIds) {
        if (entrepriseId == null || articleIds == null || articleIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> lignes = mouvementStockRepository
                .stockParArticles(entrepriseId, articleIds);

        Map<Long, LigneStock> stocks = new HashMap<>(lignes.size());
        for (Object[] ligne : lignes) {
            Long articleId = ((Number) ligne[0]).longValue();
            BigDecimal stockActuel = ligne[1] != null
                    ? new BigDecimal(ligne[1].toString())
                    : BigDecimal.ZERO;
            stocks.put(articleId, new LigneStock(stockActuel, (String) ligne[2]));
        }
        return stocks;
    }
}
