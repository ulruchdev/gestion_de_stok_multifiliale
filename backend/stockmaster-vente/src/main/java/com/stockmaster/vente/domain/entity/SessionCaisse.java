package com.stockmaster.vente.domain.entity;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.vente.domain.enums.StatutSessionCaisse;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Session de caisse — {@code DEC-009 / DEC-010 / DEC-018} : ouverte avec un
 * fond de caisse, clôturée avec le montant constaté ; {@code ecart} est le
 * contrôle anti-perte (constaté − attendu, peut être négatif).
 */
@Entity
@Table(name = "session_caisse")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionCaisse extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSessionCaisse statut;

    @Column(name = "date_ouverture", nullable = false)
    private Instant dateOuverture;

    @Column(name = "date_cloture")
    private Instant dateCloture;

    @Column(name = "fond_caisse", nullable = false)
    private Integer fondCaisse;

    /** Saisi à la clôture (comptage physique). */
    @Column(name = "montant_constate")
    private Integer montantConstate;

    /** Constaté − attendu, calculé à la clôture (peut être négatif). */
    @Column
    private Integer ecart;

    @PrePersist
    protected void onCreate() {
        if (dateOuverture == null) dateOuverture = Instant.now();
        if (statut == null) statut = StatutSessionCaisse.OUVERTE;
        if (fondCaisse == null) fondCaisse = 0;
    }
}
