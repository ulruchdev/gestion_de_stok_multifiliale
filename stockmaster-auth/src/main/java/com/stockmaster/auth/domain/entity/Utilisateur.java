package com.stockmaster.auth.domain.entity;

import com.stockmaster.auth.domain.enums.RoleUtilisateur;
import com.stockmaster.auth.domain.enums.ScopeUtilisateur;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "utilisateur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ScopeUtilisateur scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoleUtilisateur role;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Column(length = 500)
    private String photo;

    @Column(nullable = false)
    private Boolean actif;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "adresse_ville", length = 100)
    private String adresseVille;

    @Column(name = "token_reset", length = 255)
    private String tokenReset;

    @Column(name = "token_reset_expiry")
    private Instant tokenResetExpiry;

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
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = Instant.now();
    }
}
