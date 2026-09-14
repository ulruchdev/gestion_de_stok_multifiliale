package com.stockmaster.vente.domain.entity;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.tiers.domain.entity.Client;
import com.stockmaster.vente.domain.enums.EtatCommandeClient;
import com.stockmaster.vente.domain.enums.EtatReglement;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Commande client — {@code DEC-006} (états) et {@code DEC-011 / DEC-020}
 * (règlement) : pas d'acompte en V1, {@code etat_reglement} passe à REGLEE
 * au paiement complet ; {@code date_reglement} alimente le CA encaissé.
 */
@Entity
@Table(name = "commande_client")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommandeClient extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(name = "date_commande", nullable = false)
    private LocalDate dateCommande;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_commande", nullable = false, length = 20)
    private EtatCommandeClient etatCommande;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_reglement", nullable = false, length = 20)
    private EtatReglement etatReglement;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Column(name = "date_reglement")
    private Instant dateReglement;

    @Column(columnDefinition = "text")
    private String commentaire;

    @PrePersist
    protected void onCreate() {
        if (dateCommande == null) dateCommande = LocalDate.now();
        if (etatCommande == null) etatCommande = EtatCommandeClient.EN_PREPARATION;
        if (etatReglement == null) etatReglement = EtatReglement.NON_REGLEE;
    }
}
