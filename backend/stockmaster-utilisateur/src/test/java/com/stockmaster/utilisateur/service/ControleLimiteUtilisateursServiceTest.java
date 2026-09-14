package com.stockmaster.utilisateur.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ControleLimiteUtilisateursService — limite_utilisateurs du plan (DEC-015)")
class ControleLimiteUtilisateursServiceTest {

    private final ControleLimiteUtilisateursService service = new ControleLimiteUtilisateursService();

    @Test
    @DisplayName("✅ autorise sous la limite, refuse à la limite")
    void shouldRespectLimit() {
        assertThat(service.peutAjouterUtilisateur(9, 10)).isTrue();
        assertThat(service.peutAjouterUtilisateur(10, 10)).isFalse();
        assertThat(service.peutAjouterUtilisateur(50, 50)).isFalse();
    }

    @Test
    @DisplayName("❌ limite absente ou invalide → refus (fail-safe)")
    void shouldRefuseInvalidLimit() {
        assertThat(service.peutAjouterUtilisateur(0, null)).isFalse();
        assertThat(service.peutAjouterUtilisateur(0, 0)).isFalse();
    }
}
