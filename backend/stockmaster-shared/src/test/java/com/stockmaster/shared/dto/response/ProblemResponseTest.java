package com.stockmaster.shared.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProblemResponse — fabriques RFC 7807 (statut, titre, champs passés tels quels)")
class ProblemResponseTest {

    @Test
    @DisplayName("badRequest → 400 / 'Requête invalide'")
    void shouldBuildBadRequest() {
        ProblemResponse p = ProblemResponse.badRequest("/errors/x", "detail-1", "SYS_002");
        assertThat(p.getStatus()).isEqualTo(400);
        assertThat(p.getTitle()).isEqualTo("Requête invalide");
        assertThat(p.getType()).isEqualTo("/errors/x");
        assertThat(p.getDetail()).isEqualTo("detail-1");
        assertThat(p.getErrorCode()).isEqualTo("SYS_002");
        assertThat(p.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("notFound → 404 / 'Ressource non trouvée'")
    void shouldBuildNotFound() {
        ProblemResponse p = ProblemResponse.notFound("/errors/y", "d", "RES_001");
        assertThat(p.getStatus()).isEqualTo(404);
        assertThat(p.getTitle()).isEqualTo("Ressource non trouvée");
        assertThat(p.getErrorCode()).isEqualTo("RES_001");
    }

    @Test
    @DisplayName("conflict → 409 / 'Conflit'")
    void shouldBuildConflict() {
        ProblemResponse p = ProblemResponse.conflict("/errors/z", "d", "RES_002");
        assertThat(p.getStatus()).isEqualTo(409);
        assertThat(p.getTitle()).isEqualTo("Conflit");
    }

    @Test
    @DisplayName("forbidden → 403 / 'Accès interdit'")
    void shouldBuildForbidden() {
        ProblemResponse p = ProblemResponse.forbidden("/errors/f", "d", "SEC_001");
        assertThat(p.getStatus()).isEqualTo(403);
        assertThat(p.getTitle()).isEqualTo("Accès interdit");
    }

    @Test
    @DisplayName("unauthorized → 401 / 'Non authentifié'")
    void shouldBuildUnauthorized() {
        ProblemResponse p = ProblemResponse.unauthorized("/errors/u", "d", "AUTH_001");
        assertThat(p.getStatus()).isEqualTo(401);
        assertThat(p.getTitle()).isEqualTo("Non authentifié");
    }

    @Test
    @DisplayName("internalError → 500 / 'Erreur interne du serveur'")
    void shouldBuildInternalError() {
        ProblemResponse p = ProblemResponse.internalError("/errors/i", "d", "SYS_001");
        assertThat(p.getStatus()).isEqualTo(500);
        assertThat(p.getTitle()).isEqualTo("Erreur interne du serveur");
    }

    @Test
    @DisplayName("builder() pré-positionne le timestamp")
    void builderShouldPresetTimestamp() {
        assertThat(ProblemResponse.builder().build().getTimestamp()).isNotNull();
    }
}
