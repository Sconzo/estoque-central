package com.estoquecentral.common.exception;

/**
 * Schema provisioning exception (Story 8.5 - AC4).
 *
 * Thrown when schema creation or database provisioning fails.
 * Indicates that rollback should be performed.
 */
public class SchemaProvisioningException extends RuntimeException {

    public SchemaProvisioningException(String message) {
        super(message);
    }

    public SchemaProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
