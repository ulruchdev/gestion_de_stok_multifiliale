package com.stockmaster.catalogue.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * US-031 — réponse de {@code POST /api/v1/articles}.
 *
 * <p>{@code prixVenteTtc} est toujours calculé côté serveur (DEC-003), jamais
 * reçu du client. {@code margeBrutePct} est calculée à la volée, non persistée
 * : {@code (prixVenteHt − prixAchatHt) / prixAchatHt × 100}, arrondie HALF_UP
 * à 2 décimales ; {@code null} si le prix d'achat est 0 (pas de division par
 * zéro). Une valeur négative est possible et assumée (prix d'achat ≥ prix de
 * vente = avertissement non bloquant, DEC-029).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleResponse {

    private Long id;
    private String codeArticle;
    private String designation;
    private Long categorieId;
    private Integer prixAchatHt;
    private Integer prixVenteHt;
    private BigDecimal tauxTva;
    private Integer prixVenteTtc;
    private BigDecimal margeBrutePct;
    private BigDecimal seuilAlerte;
    private Boolean actif;
    private Long groupId;

    /**
     * US-032/033 — stock réel calculé à la volée (DEC-023 : Σ ENTREE − Σ SORTIE
     * depuis `mouvement_stock`). Rempli par {@code stockmaster-stock} quand
     * {@code CalculStockService} (EPIC 8) sera livré — {@code null} en V1 : le
     * module catalogue ne référence jamais le repository d'un autre module
     * (communication inter-modules = Spring Events uniquement).
     */
    private BigDecimal stockActuel;

    /**
     * US-033 — statut d'alerte (REF §4.2 : NORMAL / BAS / RUPTURE / ANOMALIE).
     * Également {@code null} en V1, faute de collection de mouvements (EPIC 8).
     */
    private String statutAlerte;
}
