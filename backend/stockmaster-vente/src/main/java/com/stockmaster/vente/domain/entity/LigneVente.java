package com.stockmaster.vente.domain.entity;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Ligne de vente — quantité en {@code DECIMAL(12,3)} (V5 §7, {@code DEC-003} :
 * vente au poids/litre) ; prix unitaire et TVA snapshottés à la vente.
 */
@Entity
@Table(name = "ligne_vente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LigneVente extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vente_id", nullable = false)
    private Vente vente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    /** Quantité en unités de gestion (DEC-013) — DECIMAL(12,3). */
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantite;

    @Column(name = "prix_unitaire", nullable = false)
    private Integer prixUnitaire;

    /** Taux figé à la vente — base du CA HT facturé (DEC-020). */
    @Column(name = "taux_tva_snapshot", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTvaSnapshot;
}
