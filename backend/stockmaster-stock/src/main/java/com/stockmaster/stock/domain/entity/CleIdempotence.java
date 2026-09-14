package com.stockmaster.stock.domain.entity;

import com.stockmaster.shared.domain.entity.Entreprise;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Clé d'idempotence — {@code DEC-027} (V5 §14) : l'en-tête {@code Idempotency-Key}
 * est exigé sur toute écriture de stock (vente, réception, transfert, correction).
 * La réponse JSON de la première requête est rejouée à l'identique tant que la
 * clé n'a pas expiré.
 *
 * <p>PK = la clé elle-même (VARCHAR(64)) ; pas de {@code date_modification} ni
 * {@code supprime} sur cette table — une entrée est créée une fois puis expire.</p>
 */
@Entity
@Table(name = "cle_idempotence")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CleIdempotence {

    @Id
    @Column(length = 64, nullable = false)
    private String cle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @Column(name = "methode_http", nullable = false, length = 10)
    private String methodeHttp;

    @Column(name = "chemin_api", nullable = false, length = 255)
    private String cheminApi;

    @Column(name = "code_http", nullable = false)
    private Integer codeHttp;

    /** Réponse sérialisée de la première requête (JSONB). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String reponse;

    @Column(name = "date_expiration", nullable = false)
    private Instant dateExpiration;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) dateCreation = Instant.now();
    }
}
