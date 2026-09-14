package com.stockmaster.stock.domain.entity;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.shared.entity.AbstractEntity;
import com.stockmaster.stock.domain.enums.StatutLigneInventaire;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Ligne d'inventaire — {@code DEC-036} : {@code ecart = quantite_constatee −
 * stock_systeme}, figé sur la ligne à l'instant du comptage. Ligne refusée
 * (constatée sous zéro) marquée {@code A_RECOMPTER}, jamais appliquée en partie.
 */
@Entity
@Table(name = "ligne_inventaire")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LigneInventaire extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_inventaire_id", nullable = false)
    private SessionInventaire sessionInventaire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "quantite_constatee", precision = 12, scale = 3)
    private BigDecimal quantiteConstatee;

    /** Stock système au moment du comptage — figé (DEC-036). */
    @Column(name = "stock_systeme", precision = 12, scale = 3)
    private BigDecimal stockSysteme;

    /** Constatée − stock système, figé à la saisie. */
    @Column(precision = 12, scale = 3)
    private BigDecimal ecart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutLigneInventaire statut;

    @PrePersist
    protected void onCreate() {
        if (statut == null) statut = StatutLigneInventaire.A_COMPTER;
    }
}
