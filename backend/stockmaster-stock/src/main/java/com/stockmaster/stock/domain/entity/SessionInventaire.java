package com.stockmaster.stock.domain.entity;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.stock.domain.enums.StatutSessionInventaire;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Campagne d'inventaire — {@code DEC-036} (V5 §15) : datée, SANS gel du stock,
 * validée par l'ADMIN_FILIALE le jour même. Chaque ligne fige le stock système
 * à l'instant du comptage.
 */
@Entity
@Table(name = "session_inventaire")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionInventaire extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSessionInventaire statut;

    @Column(name = "date_ouverture", nullable = false)
    private Instant dateOuverture;

    @Column(name = "date_cloture")
    private Instant dateCloture;

    @PrePersist
    protected void onCreate() {
        if (dateOuverture == null) dateOuverture = Instant.now();
        if (statut == null) statut = StatutSessionInventaire.OUVERTE;
    }
}
