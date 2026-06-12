package com.stockmaster.shared.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Réponse d'erreur conforme à la RFC 7807 (Problem Details for HTTP APIs).
 *
 * <p>Utilisé par {@code GlobalExceptionHandler} pour toutes les réponses d'erreur.
 * Contient les champs standardisés : type, title, status, detail, instance,
 * plus les extensions propres à StockMaster : errorCode, timestamp, errors[].</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemResponse {

    /** URI identifiant le type d'erreur (ex: /errors/entity-not-found). */
    private String type;

    /** Titre court et lisible de l'erreur. */
    private String title;

    /** Code HTTP de l'erreur. */
    private int status;

    /** Message détaillé de l'erreur. */
    private String detail;

    /** URI de l'instance (endpoint) qui a généré l'erreur. */
    private String instance;

    // ----- Extensions StockMaster -----

    /** Code d'erreur métier (enum ErrorCode). */
    private String errorCode;

    /** Timestamp de l'erreur. */
    private Instant timestamp;

    /** Liste d'erreurs de validation (champ → message). */
    private List<Map<String, String>> errors;

    /**
     * Crée une ProblemResponse pour une erreur métier.
     */
    public static ProblemResponseBuilder builder() {
        return new ProblemResponseBuilder()
                .timestamp(Instant.now());
    }

    /**
     * Crée une erreur 400 BAD_REQUEST.
     */
    public static ProblemResponse badRequest(String type, String detail, String errorCode) {
        return ProblemResponse.builder()
                .type(type)
                .title("Requête invalide")
                .status(400)
                .detail(detail)
                .errorCode(errorCode)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Crée une erreur 404 NOT_FOUND.
     */
    public static ProblemResponse notFound(String type, String detail, String errorCode) {
        return ProblemResponse.builder()
                .type(type)
                .title("Ressource non trouvée")
                .status(404)
                .detail(detail)
                .errorCode(errorCode)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Crée une erreur 409 CONFLICT.
     */
    public static ProblemResponse conflict(String type, String detail, String errorCode) {
        return ProblemResponse.builder()
                .type(type)
                .title("Conflit")
                .status(409)
                .detail(detail)
                .errorCode(errorCode)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Crée une erreur 403 FORBIDDEN.
     */
    public static ProblemResponse forbidden(String type, String detail, String errorCode) {
        return ProblemResponse.builder()
                .type(type)
                .title("Accès interdit")
                .status(403)
                .detail(detail)
                .errorCode(errorCode)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Crée une erreur 401 UNAUTHORIZED.
     */
    public static ProblemResponse unauthorized(String type, String detail, String errorCode) {
        return ProblemResponse.builder()
                .type(type)
                .title("Non authentifié")
                .status(401)
                .detail(detail)
                .errorCode(errorCode)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Crée une erreur 500 INTERNAL_SERVER_ERROR.
     */
    public static ProblemResponse internalError(String type, String detail, String errorCode) {
        return ProblemResponse.builder()
                .type(type)
                .title("Erreur interne du serveur")
                .status(500)
                .detail(detail)
                .errorCode(errorCode)
                .timestamp(Instant.now())
                .build();
    }
}
