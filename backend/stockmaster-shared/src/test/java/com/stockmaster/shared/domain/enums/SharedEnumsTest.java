package com.stockmaster.shared.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Énumérations partagées — grille GS-CDA-2026-01 (7 rôles, 3 plans — DEC-015)")
class SharedEnumsTest {

    @Test
    @DisplayName("RoleUtilisateur → les 7 acteurs du référentiel")
    void roleUtilisateurValues() {
        assertThat(RoleUtilisateur.values())
                .extracting(Enum::name)
                .containsExactly("SUPER_ADMIN", "ADMIN_GROUPE", "ADMIN_FILIALE",
                        "GESTIONNAIRE_STOCK", "RESP_ACHATS", "COMMERCIAL", "CAISSIER");
    }

    @Test
    @DisplayName("PlanAbonnement → grille à 3 valeurs (DEC-015 : STARTER/ENTERPRISE supprimés)")
    void planAbonnementValues() {
        assertThat(PlanAbonnement.values())
                .extracting(Enum::name)
                .containsExactly("GRATUIT", "PRO", "PERSONNALISE");
    }

    @Test
    @DisplayName("TypeEntreprise → MERE / FILIALE")
    void typeEntrepriseValues() {
        assertThat(TypeEntreprise.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("MERE", "FILIALE");
    }
}
