package com.stockmaster.shared.domain.entity;

import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.domain.enums.ScopeUtilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Utilisateur du système — un seul login, rattaché à une entreprise (REF §6.2).
 *
 * <p>{@code DEC-016} : {@code email_verifie} distingue l'activation (email vérifié)
 * de l'état {@code actif} (désactivation admin). La connexion exige les deux à
 * {@code TRUE} — le contrôle effectif dans le flux de login est activé avec
 * l'EPIC 12 (module notification, AUTH-07), sinon aucun compte ne serait
 * activable tant que le flux de vérification n'existe pas.</p>
 *
 * <p>{@code DEC-024} : les colonnes {@code token_reset} sont supprimées du schéma ;
 * le reset passe exclusivement par Redis (US-011/012 déjà implémenté ainsi).</p>
 */
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

    /** DEC-016 : compte inactif tant que FALSE (distinct de actif). */
    @Column(name = "email_verifie", nullable = false)
    private Boolean emailVerifie;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "adresse_ville", length = 100)
    private String adresseVille;

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
        if (emailVerifie == null) emailVerifie = false;
        if (supprime == null) supprime = false;
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = Instant.now();
    }
}
