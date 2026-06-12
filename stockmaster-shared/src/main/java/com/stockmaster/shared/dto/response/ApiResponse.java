package com.stockmaster.shared.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper standard pour toutes les réponses API réussies.
 *
 * <p>Utilisé comme enveloppe unique pour tous les endpoints.
 * Le champ {@code data} contient le payload métier, typé via le générique {@code T}.</p>
 *
 * @param <T> le type des données retournées
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** Indique si la requête a été traitée avec succès. */
    private boolean success;

    /** Message optionnel (succès ou information). */
    private String message;

    /** Données métier de la réponse. */
    private T data;

    /**
     * Crée une réponse de succès avec des données.
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Crée une réponse de succès avec des données et un message.
     */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Crée une réponse de succès sans données (ex: création, suppression).
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Crée une réponse d'échec.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
