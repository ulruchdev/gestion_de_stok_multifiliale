package com.stockmaster.vente.domain.entity;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.vente.domain.enums.StatutVente;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Vente directe à la caisse — {@code DEC-009 / DEC-010 / DEC-018}.
 *
 * <p>Créée avec le statut {@code PAYEE} (encaissement immédiat, jamais
 * {@code VALIDEE}) ; {@code client_id} nullable (client de passage) ;
 * rattachée à une {@link SessionCaisse} — l'obligation session est appliquée
 * au niveau applicatif (la colonne reste nullable en base pour ne pas casser
 * les ventes existantes, V5 §10).</p>
 */
@Entity
@Table(name = "vente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vente extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    /** Nullable : vente au comptoir pour un client de passage. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private com.stockmaster.tiers.domain.entity.Client client;

    /** DEC-018 : renseignée par le module caisse ; obligation au niveau applicatif. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_caisse_id")
    private SessionCaisse sessionCaisse;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(name = "date_vente", nullable = false)
    private Instant dateVente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutVente statut;

    @Column(columnDefinition = "text")
    private String commentaire;

    @PrePersist
    protected void onCreate() {
        if (dateVente == null) dateVente = Instant.now();
        if (statut == null) statut = StatutVente.PAYEE;
    }
}
