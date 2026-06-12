package com.stockmaster.shared.exception;

import lombok.Getter;

/**
 * Exception métier de base pour toutes les erreurs fonctionnelles.
 *
 * <p>Utilise {@link ErrorCode} pour catégoriser l'erreur de façon standardisée.
 * Le {@code GlobalExceptionHandler} intercepte cette exception et produit
 * une réponse RFC 7807 ({@code ProblemResponse}).</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object[] args;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = new Object[0];
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }

    public BusinessException(ErrorCode errorCode, String detail, Object... args) {
        super(detail);
        this.errorCode = errorCode;
        this.args = args;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }

    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
