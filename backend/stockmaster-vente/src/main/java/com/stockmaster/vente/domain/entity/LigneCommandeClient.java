package com.stockmaster.vente.domain.entity;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Ligne de commande client — quantité {@code DECIMAL(12,3)} ({@code DEC-003}),
 * prix et TVA snapshottés.
 */
@Entity
@Table(name = "ligne_commande_client")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LigneCommandeClient extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_id", nullable = false)
    private CommandeClient commande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    /** Quantité en unités de gestion (DEC-013) — DECIMAL(12,3). */
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantite;

    @Column(name = "prix_unitaire", nullable = false)
    private Integer prixUnitaire;

    @Column(name = "taux_tva_snapshot", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTvaSnapshot;
}
