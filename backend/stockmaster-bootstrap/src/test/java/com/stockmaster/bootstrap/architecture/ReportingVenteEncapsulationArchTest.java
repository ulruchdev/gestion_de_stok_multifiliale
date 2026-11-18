package com.stockmaster.bootstrap.architecture;

import com.stockmaster.reporting.architecture.VenteRepositoryLeakProbe;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test négatif de la règle ArchUnit de GS-1251.
 *
 * <p>La règle {@code reportingReadsVenteThroughPortOnly} est verte sur le code
 * réel : ce test prouve qu'elle <b>échoue</b> bien si un jour une classe de
 * {@code reporting} touche directement {@code vente.repository}. Sans ce test,
 * une règle mal écrite (paquet mal orthographié, prédicat vide) resterait verte
 * pour toujours.</p>
 */
@DisplayName("ArchUnit — GS-1251 : reporting ne lit les ventes que via LectureVentePort")
class ReportingVenteEncapsulationArchTest {

    @Test
    @DisplayName("✅ la règle détecte une dépendance reporting → vente.repository")
    void shouldDetecterUneFuiteDuRepositoryVente() {
        JavaClasses classes = new ClassFileImporter()
                .importClasses(VenteRepositoryLeakProbe.class);

        EvaluationResult resultat = ModuleDependencyArchTest
                .reportingReadsVenteThroughPortOnly
                .evaluate(classes);

        assertThat(resultat.hasViolation())
                .as("la règle doit refuser un accès direct au repository vente")
                .isTrue();
        assertThat(resultat.getFailureReport().getDetails())
                .anySatisfy(detail -> assertThat(detail).contains("LigneVenteRepository"));
    }
}
