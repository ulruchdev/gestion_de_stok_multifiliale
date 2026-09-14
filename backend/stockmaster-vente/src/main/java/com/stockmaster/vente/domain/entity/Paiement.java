package com.stockmaster.vente.domain.entity;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.vente.domain.enums.ModePaiement;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Paiement d'une vente — {@code DEC-009} : paiement mixte (une vente peut
 * combiner espèces + Mobile Money + carte ⇒ plusieurs lignes de paiement).
 * Montant en entiers XAF ({@code DEC-003}) ; référence pour Mobile Money/carte.
 */
@Entity
@Table(name = "paiement")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Paiement extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vente_id", nullable = false)
    private Vente vente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModePaiement mode;

    @Column(nullable = false)
    private Integer montant;

    @Column(name = "reference_transaction", length = 100)
    private String referenceTransaction;
}
