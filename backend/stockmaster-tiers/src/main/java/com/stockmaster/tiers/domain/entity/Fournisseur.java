package com.stockmaster.tiers.domain.entity;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Fournisseur d'une filiale — périmètre entreprise (REF §6.2).
 *
 * <p><b>Correction du 28 oct. 2026 (US-038)</b> — la javadoc affirmait « le NIF
 * est normalisé à NULL si vide (index partiel UNIQUE côté base) ». Vérifié faux
 * sur les deux points : aucune normalisation (US-036 n'en fait pas) et
 * <b>aucun index UNIQUE</b> sur {@code fournisseur} ({@code V1__init_schema.sql}
 * L197-209, {@code V2__create_indexes.sql} L14 = simple index de scoping).
 * {@code FournisseurRepository.existsByEntrepriseIdAndNifAndSupprimeFalse} est
 * donc sans appelant et sans contrainte sous-jacente : <b>un NIF en doublon est
 * aujourd'hui accepté</b>, et US-038 n'ajoute aucune règle d'unicité (aucun
 * critère d'acceptation, aucun index).</p>
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
