package com.stockmaster.auth.domain.entity;

import com.stockmaster.auth.domain.enums.TypeEntreprise;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "entreprise")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private TenantGroup groupe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Entreprise parent;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_entreprise", nullable = false, length = 10)
    private TypeEntreprise typeEntreprise;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(name = "code_filiale", length = 10)
    private String codeFiliale;

    @Column(length = 20)
    private String nif;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telephone;

    @Column(name = "adresse_rue", length = 200)
    private String adresseRue;

    @Column(name = "adresse_quartier", length = 100)
    private String adresseQuartier;

    @Column(name = "adresse_ville", length = 100)
    private String adresseVille;

    @Column(name = "adresse_region", length = 100)
    private String adresseRegion;

    @Column(name = "adresse_pays", length = 50)
    private String adressePays;

    @Column(length = 500)
    private String logo;

    @Column(nullable = false)
    private Boolean actif;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(name = "date_modification", nullable = false)
    private Instant dateModification;

    @Column(nullable = false)
    private Boolean supprime;

    @PrePersist
    protected void onCreate() {
        dateCreation = Instant.now();
        dateModification = Instant.now();
        if (actif == null) actif = true;
        if (supprime == null) supprime = false;
        if (adressePays == null) adressePays = "Cameroun";
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = Instant.now();
    }
}
