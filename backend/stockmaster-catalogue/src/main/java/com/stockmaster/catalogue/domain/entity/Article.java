package com.stockmaster.catalogue.domain.entity;

import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Article du catalogue — global au groupe ({@code DEC-002}), montants en
 * INTEGER XAF entiers ({@code DEC-003}), conditionnement à deux unités
 * ({@code DEC-013}) et schéma lot-ready ({@code DEC-012} : colonnes présentes
 * en V1, usage opérationnel FEFO en V1.5).
 *
 * <p>{@code prix_vente_ttc} est calculé, jamais saisi : {@code CalculTvaService}.</p>
 */
@Entity
@Table(name = "article")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Article extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private TenantGroup groupe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @Column(name = "code_article", nullable = false, length = 30)
    private String codeArticle;

    @Column(nullable = false, length = 150)
    private String designation;

    @Column(name = "prix_achat_ht", nullable = false)
    private Integer prixAchatHt;

    @Column(name = "prix_vente_ht", nullable = false)
    private Integer prixVenteHt;

    @Column(name = "taux_tva", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTva;

    /** Calculé : prix_vente_ht × (1 + taux_tva/100) — DEC-003, jamais saisi. */
    @Column(name = "prix_vente_ttc", nullable = false)
    private Integer prixVenteTtc;

    /** 0 = alerte désactivée. Comparaison "rupture" : stock ≤ seuil (DEC-023). */
    @Column(name = "seuil_alerte", nullable = false, precision = 12, scale = 3)
    private BigDecimal seuilAlerte;

    @Column(length = 500)
    private String photo;

    @Column(nullable = false)
    private Boolean actif;

    // ===== DEC-013 : conditionnement =====================================

    @Column(name = "unite_gestion", nullable = false, length = 20)
    private String uniteGestion;

    @Column(name = "unite_achat", nullable = false, length = 20)
    private String uniteAchat;

    /** 1 unité d'achat = facteur_conversion unités de gestion (doit être > 0). */
    @Column(name = "facteur_conversion", nullable = false, precision = 12, scale = 3)
    private BigDecimal facteurConversion;

    // ===== DEC-012 : schéma lot-ready =====================================

    @Column(length = 50)
    private String lot;

    @Column(name = "date_peremption")
    private LocalDate datePeremption;

    @PrePersist
    protected void onCreate() {
        if (actif == null) actif = true;
        if (uniteGestion == null) uniteGestion = "UNITE";
        if (uniteAchat == null) uniteAchat = "UNITE";
        if (facteurConversion == null) facteurConversion = BigDecimal.ONE;
        if (seuilAlerte == null) seuilAlerte = BigDecimal.ZERO;
    }
}
