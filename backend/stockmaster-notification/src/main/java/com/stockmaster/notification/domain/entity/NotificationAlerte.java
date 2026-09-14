package com.stockmaster.notification.domain.entity;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.notification.domain.enums.EtatAlerte;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Centre de notifications — refondu par {@code DEC-004} (V5 §13).
 *
 * <p>{@code destinataire_utilisateur_id} NULL = toute l'entreprise ;
 * {@code type_alerte} est VOLONTAIREMENT sans CHECK en base : extensible
 * (STOCK_BAS, RUPTURE, SECURITY_ALERT…) — c'est l'erreur de conception que
 * DEC-004 reprochait à l'ancien modèle. {@code ECART_STOCK_DETECTE} n'existe
 * plus ({@code DEC-037}) : l'inventaire remplace la détection d'écart.</p>
 */
@Entity
@Table(name = "notification_alerte")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationAlerte extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    /** NULL = notification destinée à toute l'entreprise ; sinon destinataire précis (SECURITY_ALERT). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_utilisateur_id")
    private com.stockmaster.shared.domain.entity.Utilisateur destinataire;

    /** NULL pour les notifications non liées au catalogue (ex. SECURITY_ALERT). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    /** SANS CHECK : extensible par convention de code (DEC-004). */
    @Column(name = "type_alerte", nullable = false, length = 30)
    private String typeAlerte;

    @Column(name = "stock_actuel", precision = 12, scale = 3)
    private BigDecimal stockActuel;

    @Column(name = "seuil_alerte", precision = 12, scale = 3)
    private BigDecimal seuilAlerte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtatAlerte etat;

    @PrePersist
    protected void onCreate() {
        if (etat == null) etat = EtatAlerte.NON_LUE;
        if (typeAlerte == null) typeAlerte = "STOCK_BAS";
    }
}
