package com.stockmaster.stock.domain.entity;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.stock.domain.enums.OrigineType;
import com.stockmaster.stock.domain.enums.TypeMouvement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mouvement de stock — journal IMMUABLE (V5 §12) : jamais d'UPDATE ni de
 * DELETE applicatif ; la colonne {@code supprime} existe mais est verrouillée
 * à FALSE par CHECK en base (REF §6.3), et le trigger
 * {@code update_date_modification} est volontairement EXCLU de cette table (C-12).
 *
 * <p>Table partitionnée par mois sur {@code date_mouvement} ({@code DEC-033}) :
 * la PK réelle est composite {@code (id, date_mouvement)} ; le mappage JPA ne
 * déclare que {@code id} (bigserial, unique en pratique) car le journal est en
 * insertion/lecture seule — aucune mise à jour par clé n'est jamais effectuée.
 * {@code date_mouvement} doit être renseigné à l'insertion (elle détermine la
 * partition cible).</p>
 */
@Entity
@Table(name = "mouvement_stock")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false, length = 30)
    private TypeMouvement typeMouvement;

    /** Toujours > 0 (CHECK) — le sens est porté par {@code typeMouvement}. */
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantite;

    /** Détermine la partition mensuelle cible (DEC-033) — jamais NULL. */
    @Column(name = "date_mouvement", nullable = false)
    private Instant dateMouvement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "origine_id")
    private Long origineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "origine_type", length = 30)
    private OrigineType origineType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfert_id")
    private TransfertStock transfert;

    @Column(columnDefinition = "text")
    private String motif;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(name = "date_modification", nullable = false)
    private Instant dateModification;

    /** Présent pour conformité REF §6.3 mais verrouillé à FALSE en base (journal immuable). */
    @Column(nullable = false)
    private Boolean supprime;

    @PrePersist
    protected void onCreate() {
        if (dateMouvement == null) dateMouvement = Instant.now();
        dateCreation = Instant.now();
        dateModification = Instant.now();
        supprime = false; // CHECK chk_mouvement_stock_supprime_interdit
    }
}
