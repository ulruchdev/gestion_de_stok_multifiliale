package com.stockmaster.catalogue.domain.entity;

import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Catégorie d'articles — rattachée au GROUPE, pas à la filiale ({@code DEC-002}) :
 * le catalogue est partagé par toutes les filiales pour rendre les transferts possibles.
 *
 * <p>Unicité en base : {@code (group_id, code)} (V5 §6). {@code taux_tva} est
 * porté par la catégorie et snapshotté sur les lignes de documents.</p>
 */
@Entity
@Table(name = "categorie")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Categorie extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private TenantGroup groupe;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String designation;

    /** Ex. 19.25 % (taux standard Cameroun). Snapshotté sur les lignes de documents. */
    @Column(name = "taux_tva", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTva;
}
