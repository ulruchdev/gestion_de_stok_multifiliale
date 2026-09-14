package com.stockmaster.shared.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AbstractEntity — socle commun (soft delete, horodatage @PrePersist/@PreUpdate)")
class AbstractEntityTest {

    /** Sous-classe concrète minimale pour tester la classe de base. */
    private static class Thing extends AbstractEntity { }

    @Test
    @DisplayName("Fraîche → supprime=false, isActif()=true ; marquerCommeSupprime() → isActif()=false")
    void softDeleteHelpers() {
        Thing t = new Thing();
        assertThat(t.getSupprime()).isFalse();
        assertThat(t.isActif()).isTrue();

        t.marquerCommeSupprime();
        assertThat(t.getSupprime()).isTrue();
        assertThat(t.isActif()).isFalse();
    }

    @Test
    @DisplayName("prePersist → dateCreation + dateModification horodatées, supprime=false")
    void prePersistStampsDefaults() {
        Thing t = new Thing();
        t.prePersist();
        assertThat(t.getDateCreation()).isNotNull();
        assertThat(t.getDateModification()).isNotNull();
        assertThat(t.getSupprime()).isFalse();
    }

    @Test
    @DisplayName("prePersist ne réécrase pas un dateCreation déjà positionné")
    void prePersistKeepsExistingCreationDate() {
        Instant d = Instant.parse("2026-06-01T12:00:00Z");
        Thing t = new Thing();
        t.setDateCreation(d);
        t.prePersist();
        assertThat(t.getDateCreation()).isEqualTo(d);
        assertThat(t.getDateModification()).isNotNull();
    }

    @Test
    @DisplayName("preUpdate → dateModification rafraîchie")
    void preUpdateTouchesModificationDate() {
        Instant d = Instant.parse("2026-06-01T12:00:00Z");
        Thing t = new Thing();
        t.setDateModification(d);
        t.preUpdate();
        assertThat(t.getDateModification()).isAfter(d);
    }
}
