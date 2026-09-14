package com.stockmaster.catalogue.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Article — défauts @PrePersist (DEC-013 conditionnement, seuil d'alerte)")
class ArticleDefaultsTest {

    @Test
    @DisplayName("Article.onCreate → unités par défaut UNITE, facteur 1, seuil 0, actif")
    void articleDefaults() {
        Article a = Article.builder().codeArticle("ART-001").designation("Huile").build();
        a.onCreate();
        assertThat(a.getActif()).isTrue();
        assertThat(a.getUniteGestion()).isEqualTo("UNITE");
        assertThat(a.getUniteAchat()).isEqualTo("UNITE");
        assertThat(a.getFacteurConversion()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(a.getSeuilAlerte()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Article.onCreate préserve le conditionnement explicite (sacs de 25 — DEC-013)")
    void articlePreservesExplicitUnits() {
        Article a = Article.builder()
                .codeArticle("ART-002")
                .designation("Ciment")
                .uniteGestion("SAC")
                .uniteAchat("SAC")
                .facteurConversion(new BigDecimal("25"))
                .seuilAlerte(new BigDecimal("10"))
                .build();
        a.onCreate();
        assertThat(a.getUniteGestion()).isEqualTo("SAC");
        assertThat(a.getFacteurConversion()).isEqualByComparingTo(new BigDecimal("25"));
        assertThat(a.getSeuilAlerte()).isEqualByComparingTo(new BigDecimal("10"));
    }
}
