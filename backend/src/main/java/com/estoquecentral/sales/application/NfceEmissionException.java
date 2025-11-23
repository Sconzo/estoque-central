package com.estoquecentral.sales.application;

/**
 * Exception thrown when NFCe emission fails
 * Story 4.3: NFCe Emission and Stock Decrease
 */
public class NfceEmissionException extends Exception {
    public NfceEmissionException(String message) {
        super(message);
    }

    public NfceEmissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
