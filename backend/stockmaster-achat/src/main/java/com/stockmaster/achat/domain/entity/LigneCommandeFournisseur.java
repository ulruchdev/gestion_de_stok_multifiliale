package com.stockmaster.achat.domain.entity;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Ligne de commande fournisseur — quantité en {@code DECIMAL(12,3)} (V5 §4,
 * {@code DEC-003} : vente au poids/litre) et taux TVA snapshotté à la création.
 *
 * <p>Le prix unitaire reste un entier XAF HT ({@code DEC-003}).</p>
 */
@Entity
@Table(name = "ligne_commande_fournisseur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LigneCommandeFournisseur extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_id", nullable = false)
    private CommandeFournisseur commande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    /** Quantité en unités de gestion (DEC-013) — DECIMAL(12,3). */
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantite;

    @Column(name = "prix_unitaire", nullable = false)
    private Integer prixUnitaire;

    /** Taux figé à la création — les modifications TVA postérieures n'affectent pas la commande. */
    @Column(name = "taux_tva_snapshot", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTvaSnapshot;
}
