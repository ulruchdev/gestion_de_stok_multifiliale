package com.stockmaster.achat.domain.entity;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.achat.domain.enums.EtatCommandeFournisseur;
import com.stockmaster.tiers.domain.entity.Fournisseur;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Commande fournisseur d'une filiale — machine à états {@code DEC-006}.
 *
 * <p>Transitions légales (au niveau service, cf. {@code EtatCommandeFournisseur}) :
 * COMMANDEE → PARTIELLEMENT_RECUE → RECEPTIONNEE ; annulation depuis
 * COMMANDEE / PARTIELLEMENT_RECUE. Aucune modification de ligne après réception.</p>
 */
@Entity
@Table(name = "commande_fournisseur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommandeFournisseur extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    /** Créateur de la commande (journal d'audit). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(name = "date_commande", nullable = false)
    private LocalDate dateCommande;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_commande", nullable = false, length = 30)
    private EtatCommandeFournisseur etatCommande;

    @Column(columnDefinition = "text")
    private String commentaire;

    @PrePersist
    protected void onCreate() {
        if (dateCommande == null) dateCommande = LocalDate.now();
        if (etatCommande == null) etatCommande = EtatCommandeFournisseur.COMMANDEE;
    }
}
