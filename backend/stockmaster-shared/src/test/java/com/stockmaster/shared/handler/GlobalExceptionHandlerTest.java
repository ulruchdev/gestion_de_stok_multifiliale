package com.stockmaster.shared.handler;

import com.stockmaster.shared.dto.response.ProblemResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.EntityNotFoundException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
        request.setMethod("GET");
    }

    @Nested
    @DisplayName("BusinessException → statut HTTP selon ErrorCode")
    class BusinessExceptionTests {
        @Test void shouldReturn400ForValidationError() {
            var ex = new BusinessException(ErrorCode.SYS_VALIDATION_ERROR, "Champ invalide");
            var resp = handler.handleBusinessException(ex, request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resp.getBody().getStatus()).isEqualTo(400);
            assertThat(resp.getBody().getErrorCode()).isEqualTo("SYS_005");
            assertThat(resp.getBody().getInstance()).isEqualTo("/api/v1/test");
        }
        @Test void shouldReturn401ForAuthError() {
            var resp = handler.handleBusinessException(new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS), request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody().getErrorCode()).isEqualTo("AUTH_001");
        }
        @Test void shouldReturn403ForForbidden() {
            var resp = handler.handleBusinessException(new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED), request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
        @Test void shouldReturn404ForNotFound() {
            var resp = handler.handleBusinessException(new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND), request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
        @Test void shouldReturn409ForConflict() {
            var resp = handler.handleBusinessException(new BusinessException(ErrorCode.RES_DUPLICATE_CODE), request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }
        @Test void shouldReturn429ForRateLimit() {
            var resp = handler.handleBusinessException(new BusinessException(ErrorCode.AUTH_RATE_LIMIT), request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @Nested
    @DisplayName("EntityNotFoundException → 404")
    class EntityNotFoundTests {
        @Test void shouldReturn404WithEntityAndId() {
            var resp = handler.handleEntityNotFound(new EntityNotFoundException("Article", 42L), request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(resp.getBody().getDetail()).contains("Article", "42");
        }
        @Test void shouldReturn404WithFieldSearch() {
            var resp = handler.handleEntityNotFound(new EntityNotFoundException("User", "email", "x@y.com"), request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(resp.getBody().getDetail()).contains("email", "x@y.com");
        }
    }

    @Nested
    @DisplayName("InsufficientStockException → 409 avec détails")
    class InsufficientStockTests {
        @Test void shouldReturn409WithShortageDetails() {
            var s = new InsufficientStockException.StockShortage(1L, "RIZ50KG", "Riz 50kg", 3, 10);
            var resp = handler.handleInsufficientStock(new InsufficientStockException(List.of(s)), request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(resp.getBody().getErrorCode()).isEqualTo("STK_001");
            assertThat(resp.getBody().getErrors()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException → 400 avec champs")
    class ValidationTests {
        @Test void shouldReturn400WithFieldErrors() {
            var be = new BindException(new Object(), "obj");
            be.addError(new FieldError("obj", "email", "Obligatoire"));
            be.addError(new FieldError("obj", "name", "Min 3 chars"));
            var resp = handler.handleValidationErrors(new MethodArgumentNotValidException(null, be.getBindingResult()), request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resp.getBody().getErrorCode()).isEqualTo("SYS_005");
            assertThat(resp.getBody().getErrors()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("AccessDeniedException → 403")
    class AccessDeniedTests {
        @Test void shouldReturn403() {
            var resp = handler.handleAccessDenied(new AccessDeniedException("Accès refusé"), request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(resp.getBody().getErrorCode()).isEqualTo("SEC_001");
        }
    }

    @Nested
    @DisplayName("NoHandlerFoundException → 404")
    class NoHandlerFoundTests {
        @Test void shouldReturn404() {
            var ex = new NoHandlerFoundException("GET", "/api/unknown", null);
            var resp = handler.handleNoHandlerFound(ex, request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("HttpRequestMethodNotSupportedException → 405")
    class MethodNotAllowedTests {
        @Test void shouldReturn405() {
            var ex = new HttpRequestMethodNotSupportedException("DELETE", List.of("GET", "POST"));
            var resp = handler.handleMethodNotSupported(ex, request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            assertThat(resp.getBody().getErrorCode()).isEqualTo("SYS_003");
        }
    }

    @Nested
    @DisplayName("HttpMessageNotReadableException → 400")
    class MessageNotReadableTests {
        @Test void shouldReturn400() {
            var resp = handler.handleMessageNotReadable(
                new HttpMessageNotReadableException("Invalid JSON"), request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resp.getBody().getErrorCode()).isEqualTo("SYS_002");
        }
    }

    @Nested
    @DisplayName("MethodArgumentTypeMismatchException → 400")
    class ArgumentMismatchTests {
        @Test void shouldReturn400WithParamName() {
            var ex = new MethodArgumentTypeMismatchException("abc", Integer.class, "page", null, null);
            var resp = handler.handleArgumentTypeMismatch(ex, request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resp.getBody().getDetail()).contains("page");
        }
    }

    @Nested
    @DisplayName("Fallback Exception → 500 sans stack trace")
    class FallbackTests {
        @Test void shouldReturn500WithoutStackTrace() {
            var resp = handler.handleAllUncaught(new RuntimeException("Oups"), request);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(resp.getBody().getErrorCode()).isEqualTo("SYS_001");
            assertThat(resp.getBody().getDetail()).doesNotContain("at com.stockmaster");
            assertThat(resp.getBody().getDetail()).contains("Une erreur inattendue");
        }
    }

    @Nested
    @DisplayName("RFC 7807 — Format complet")
    class Rfc7807Tests {
        @Test void shouldContainAllRfc7807Fields() {
            var resp = handler.handleBusinessException(
                new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND), request);
            assertThat(resp.getBody().getType()).isNotBlank();
            assertThat(resp.getBody().getTitle()).isNotBlank();
            assertThat(resp.getBody().getStatus()).isPositive();
            assertThat(resp.getBody().getDetail()).isNotBlank();
            assertThat(resp.getBody().getInstance()).isNotBlank();
            assertThat(resp.getBody().getErrorCode()).isNotBlank();
            assertThat(resp.getBody().getTimestamp()).isNotNull();
        }
        @Test void shouldHaveCorrectTypePattern() {
            var resp = handler.handleBusinessException(
                new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS), request);
            assertThat(resp.getBody().getType()).isEqualTo("/errors/auth-001");
        }
    }
}
