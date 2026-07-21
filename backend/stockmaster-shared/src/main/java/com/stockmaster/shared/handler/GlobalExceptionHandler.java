package com.stockmaster.shared.handler;

import com.stockmaster.shared.dto.response.ProblemResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.EntityNotFoundException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.exception.InsufficientStockException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire centralisé des exceptions.
 *
 * <p>Intercepte toutes les exceptions levées par les contrôleurs et retourne
 * une réponse structurée conforme à la RFC 7807 ({@link ProblemResponse}).
 * Les stack traces sont loguées uniquement côté serveur — jamais exposées au client.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========================================================================
    // Exceptions métier (BusinessException et sous-classes)
    // ========================================================================

    /**
     * BusinessException : classe de base de toutes les exceptions métier.
     * Associe un ErrorCode (code, message, statut HTTP) et un detail optionnel.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();
        log.warn("BusinessException — [{}] {} — {}", errorCode.getCode(),
                 errorCode.getMessage(), ex.getMessage());

        ProblemResponse body = ProblemResponse.builder()
                .type("/errors/" + errorCode.getCode().toLowerCase().replace('_', '-'))
                .title(errorCode.getMessage())
                .status(errorCode.getHttpStatus())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode(errorCode.getCode())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    /**
     * EntityNotFoundException : ressource non trouvée → 404.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemResponse> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        return handleBusinessException(ex, request);
    }

    /**
     * InsufficientStockException : stock insuffisant → 409 avec détails.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ProblemResponse> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();
        log.warn("Stock insuffisant — {} articles en rupture", ex.getShortages().size());

        ProblemResponse body = ProblemResponse.builder()
                .type("/errors/insufficient-stock")
                .title(errorCode.getMessage())
                .status(errorCode.getHttpStatus())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode(errorCode.getCode())
                .timestamp(Instant.now())
                .errors(List.of(Map.of("shortages",
                        ex.getShortages().stream()
                                .map(s -> "%s (%s) : disponible=%d, demandé=%d"
                                        .formatted(s.getCodeArticle(), s.getDesignation(),
                                                s.getStockDisponible(), s.getQuantiteDemandee()))
                                .collect(Collectors.joining("; ")))))
                .build();

        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    // ========================================================================
    // Exceptions de validation
    // ========================================================================

    /**
     * MethodArgumentNotValidException : erreurs de validation Jakarta → 400.
     * Retourne la liste des champs en erreur avec leurs messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> Map.of(fe.getField(),
                        fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Valeur invalide"))
                .toList();

        log.warn("Validation échouée — {} champ(s) en erreur", errors.size());

        ProblemResponse body = ProblemResponse.builder()
                .type("/errors/validation")
                .title("Erreur de validation")
                .status(400)
                .detail("Un ou plusieurs champs sont invalides. Voir 'errors' pour les détails.")
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.SYS_VALIDATION_ERROR.getCode())
                .timestamp(Instant.now())
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ========================================================================
    // Exceptions de sécurité
    // ========================================================================

    /**
     * AccessDeniedException : utilisateur non autorisé → 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Accès refusé — {}", request.getRequestURI());

        ProblemResponse body = ProblemResponse.builder()
                .type("/errors/access-denied")
                .title("Accès refusé")
                .status(403)
                .detail("Vous n'avez pas les droits nécessaires pour accéder à cette ressource.")
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.SEC_ACCESS_DENIED.getCode())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // ========================================================================
    // Exceptions de routage
    // ========================================================================

    /**
     * NoHandlerFoundException : endpoint inexistant → 404.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ProblemResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {

        log.warn("Endpoint non trouvé — {} {}", ex.getHttpMethod(), ex.getRequestURL());

        ProblemResponse body = ProblemResponse.builder()
                .type("/errors/not-found")
                .title("Ressource non trouvée")
                .status(404)
                .detail("L'URL '%s' n'existe pas.".formatted(ex.getRequestURL()))
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.RES_ENTITY_NOT_FOUND.getCode())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * HttpRequestMethodNotSupportedException : méthode HTTP non autorisée → 405.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("Méthode non supportée — {} pour {}", ex.getMethod(), request.getRequestURI());

        ProblemResponse body = ProblemResponse.builder()
                .type("/errors/method-not-allowed")
                .title("Méthode non autorisée")
                .status(405)
                .detail("La méthode '%s' n'est pas supportée pour cette URL. Méthodes autorisées : %s"
                        .formatted(ex.getMethod(), ex.getSupportedHttpMethods()))
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.SYS_METHOD_NOT_ALLOWED.getCode())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    // ========================================================================
    // Exceptions techniques / génériques
    // ========================================================================

    /**
     * HttpMessageNotReadableException : corps JSON invalide → 400.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Corps de requête invalide — {}", ex.getMessage());

        ProblemResponse body = ProblemResponse.builder()
                .type("/errors/bad-request")
                .title("Requête invalide")
                .status(400)
                .detail("Le corps de la requête est mal formaté ou illisible.")
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.SYS_BAD_REQUEST.getCode())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * MethodArgumentTypeMismatchException : paramètre de requête mal typé → 400.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemResponse> handleArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Argument incompatible — {}", ex.getName());
        ProblemResponse body = ProblemResponse.builder()
                .type("/errors/bad-request")
                .title("Requête invalide")
                .status(400)
                .detail("Le paramètre '%s' n'a pas un format valide.".formatted(ex.getName()))
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.SYS_BAD_REQUEST.getCode())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ========================================================================
    // Fallback — toutes les exceptions non gérées (dernier recours)
    // ========================================================================

    /**
     * Exception générique — dernier rempart pour toutes les exceptions non gérées.
     * Ne JAMAIS exposer la stack trace au client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemResponse> handleAllUncaught(
            Exception ex, HttpServletRequest request) {

        log.error("Exception non gérée — {} {}", request.getMethod(), request.getRequestURI(), ex);

        ProblemResponse body = ProblemResponse.builder()
                .type("/errors/internal-error")
                .title("Erreur interne du serveur")
                .status(500)
                .detail("Une erreur inattendue s'est produite. Veuillez réessayer plus tard.")
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.SYS_INTERNAL_ERROR.getCode())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
