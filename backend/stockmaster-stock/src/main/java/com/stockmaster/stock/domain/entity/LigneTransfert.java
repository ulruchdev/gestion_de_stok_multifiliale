package com.stockmaster.stock.domain.entity;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Ligne de transfert — écart = {@code quantite_expediee − quantite_recue}
 * ({@code DEC-007}) ; le lot est porté par la ligne ({@code DEC-012}).
 */
@Entity
@Table(name = "ligne_transfert")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LigneTransfert extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfert_id", nullable = false)
    private TransfertStock transfert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(length = 50)
    private String lot;

    @Column(name = "quantite_demandee", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantiteDemandee;

    @Column(name = "quantite_expediee", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantiteExpediee;

    @Column(name = "quantite_recue", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantiteRecue;

    @PrePersist
    protected void onCreate() {
        if (quantiteExpediee == null) quantiteExpediee = BigDecimal.ZERO;
        if (quantiteRecue == null) quantiteRecue = BigDecimal.ZERO;
    }
}
