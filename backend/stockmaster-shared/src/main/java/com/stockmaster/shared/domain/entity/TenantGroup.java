package com.stockmaster.shared.domain.entity;

import com.stockmaster.shared.domain.enums.PlanAbonnement;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Racine du multi-tenant (REF §6.2) — groupe d'entreprises et plan d'abonnement.
 *
 * <p>{@code DEC-015} : grille à 3 plans (GRATUIT / PRO / PERSONNALISE) ;
 * les limites portent sur {@code limite_filiales} ET {@code limite_utilisateurs}
 * (10 GRATUIT / 50 PRO / négocié PERSONNALISE), jamais codées en dur ailleurs.</p>
 */
@Entity
@Table(name = "tenant_group")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TenantGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_groupe", nullable = false, unique = true, length = 100)
    private String nomGroupe;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_abonnement", nullable = false, length = 20)
    private PlanAbonnement planAbonnement;

    @Column(nullable = false)
    private Boolean actif;

    @Column(name = "date_expiration_plan")
    private LocalDate dateExpirationPlan;

    @Column(name = "limite_filiales", nullable = false)
    private Integer limiteFiliales;

    /** DEC-015 : 10 (GRATUIT) / 50 (PRO) / négocié (PERSONNALISE). */
    @Column(name = "limite_utilisateurs", nullable = false)
    private Integer limiteUtilisateurs;

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
        if (planAbonnement == null) planAbonnement = PlanAbonnement.GRATUIT;
        if (limiteFiliales == null) limiteFiliales = 1;
        if (limiteUtilisateurs == null) limiteUtilisateurs = 10;
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = Instant.now();
    }
}
