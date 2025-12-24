package com.estoquecentral.common.exception;

/**
 * Business logic exception (Story 8.5).
 *
 * Thrown when business rules are violated (e.g., duplicate resources,
 * invalid state transitions, etc.).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
