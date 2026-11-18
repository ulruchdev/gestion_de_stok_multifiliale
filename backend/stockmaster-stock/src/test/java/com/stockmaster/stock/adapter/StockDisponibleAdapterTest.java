package com.stockmaster.stock.adapter;

import com.stockmaster.catalogue.port.StockDisponiblePort.LigneStock;
import com.stockmaster.stock.repository.MouvementStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockDisponibleAdapter — lecture du stock pour le catalogue")
class StockDisponibleAdapterTest {

    @Mock
    private MouvementStockRepository mouvementStockRepository;

    @InjectMocks
    private StockDisponibleAdapter adapter;

    @Test
    @DisplayName("✅ Mappe articleId, stock et statut renvoyés par la requête agrégée")
    void shouldMapRows() {
        when(mouvementStockRepository.stockParArticles(7L, List.of(1L, 2L)))
                .thenReturn(List.<Object[]>of(
                        new Object[]{1L, new BigDecimal("12.5"), "BAS"},
                        new Object[]{2L, BigDecimal.ZERO, "RUPTURE"}));

        Map<Long, LigneStock> stocks = adapter.stockParArticle(7L, List.of(1L, 2L));

        assertThat(stocks).hasSize(2);
        assertThat(stocks.get(1L).stockActuel()).isEqualByComparingTo("12.5");
        assertThat(stocks.get(1L).statutAlerte()).isEqualTo("BAS");
        assertThat(stocks.get(2L).stockActuel()).isEqualByComparingTo("0");
        assertThat(stocks.get(2L).statutAlerte()).isEqualTo("RUPTURE");
    }

    @Test
    @DisplayName("✅ Sans filiale ou sans article, aucune requête n'est émise")
    void shouldShortCircuit() {
        assertThat(adapter.stockParArticle(null, List.of(1L))).isEmpty();
        assertThat(adapter.stockParArticle(7L, List.of())).isEmpty();
        verifyNoInteractions(mouvementStockRepository);
    }
}
