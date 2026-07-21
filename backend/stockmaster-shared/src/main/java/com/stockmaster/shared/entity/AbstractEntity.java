package com.stockmaster.shared.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

/**
 * Classe de base pour toutes les entités du projet StockMaster CM.
 * Fournit les champs communs : id (auto-généré), dateCreation, dateModification, supprime.
 *
 * <p>Toutes les entités héritent de cette classe et utilisent le soft delete
 * via le champ {@code supprime} plutôt que la suppression physique.</p>
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @LastModifiedDate
    @Column(name = "date_modification", nullable = false)
    private Instant dateModification;

    @Column(name = "supprime", nullable = false)
    private Boolean supprime = false;

    /**
     * Marque l'entité comme supprimée (soft delete).
     * La ligne reste en base, mais est filtrée dans toutes les requêtes.
     */
    public void marquerCommeSupprime() {
        this.supprime = true;
    }

    /**
     * Vérifie si l'entité est active (non supprimée).
     */
    public boolean isActif() {
        return !Boolean.TRUE.equals(this.supprime);
    }

    @PrePersist
    protected void prePersist() {
        if (dateCreation == null) {
            dateCreation = Instant.now();
        }
        if (dateModification == null) {
            dateModification = Instant.now();
        }
        if (supprime == null) {
            supprime = false;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        dateModification = Instant.now();
    }
}
