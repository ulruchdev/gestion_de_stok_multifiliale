package com.stockmaster.tiers.domain.entity;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Client d'une filiale — périmètre entreprise (REF §6.2) : un client
 * n'existe que dans la filiale qui le sert, contrairement au catalogue.
 */
@Entity
@Table(name = "client")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Client extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 100)
    private String prenom;

    @Column(length = 20)
    private String telephone;

    @Column(length = 150)
    private String email;

    @Column(name = "adresse_ville", length = 100)
    private String adresseVille;

    @Column(name = "adresse_quartier", length = 100)
    private String adresseQuartier;
}
