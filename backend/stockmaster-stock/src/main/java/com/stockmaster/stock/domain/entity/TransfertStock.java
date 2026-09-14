package com.stockmaster.stock.domain.entity;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.stock.domain.enums.StatutTransfert;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Bon de transfert inter-filiales — multi-lignes et rattaché au GROUPE
 * ({@code DEC-002 / DEC-007}, V5 §8). Exception déclarée à la règle
 * "entreprise_id" (REF §6.3) : la table porte {@code group_id} et deux FK
 * composites vers {@code entreprise(group_id, id)} garantissent que source
 * et cible sont dans le même groupe — le contrôle est en base, pas seulement
 * applicatif.
 */
@Entity
@Table(name = "transfert_stock")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransfertStock extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private com.stockmaster.shared.domain.entity.TenantGroup groupe;

    /** FK composite (group_id, filiale_source_id) → entreprise — même groupe garanti. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "filiale_source_id", nullable = false)
    private Entreprise filialeSource;

    /** FK composite (group_id, filiale_cible_id) → entreprise — même groupe garanti. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "filiale_cible_id", nullable = false)
    private Entreprise filialeCible;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demandeur_id", nullable = false)
    private Utilisateur demandeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approbateur_id")
    private Utilisateur approbateur;

    @Column(nullable = false, length = 30)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutTransfert statut;

    @Column(name = "motif_refus", columnDefinition = "text")
    private String motifRefus;

    @Column(name = "date_demande", nullable = false)
    private Instant dateDemande;

    @Column(name = "date_validation")
    private Instant dateValidation;

    @Column(name = "date_expedition")
    private Instant dateExpedition;

    @Column(name = "date_reception")
    private Instant dateReception;

    @PrePersist
    protected void onCreate() {
        if (dateDemande == null) dateDemande = Instant.now();
        if (statut == null) statut = StatutTransfert.DEMANDE;
    }
}
