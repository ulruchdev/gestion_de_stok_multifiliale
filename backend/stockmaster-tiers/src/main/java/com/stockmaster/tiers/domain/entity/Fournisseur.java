package com.stockmaster.tiers.domain.entity;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Fournisseur d'une filiale — périmètre entreprise (REF §6.2).
 * Le NIF est normalisé à NULL si vide (index partiel UNIQUE côté base).
 */
@Entity
@Table(name = "fournisseur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Fournisseur extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @Column(name = "raison_sociale", nullable = false, length = 200)
    private String raisonSociale;

    @Column(length = 20)
    private String nif;

    @Column(length = 100)
    private String contact;

    @Column(length = 20)
    private String telephone;

    @Column(length = 150)
    private String email;

    @Column(name = "adresse_ville", length = 100)
    private String adresseVille;
}
