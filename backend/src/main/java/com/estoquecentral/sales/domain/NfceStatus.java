package com.estoquecentral.sales.domain;

/**
 * NFCe emission status
 * Story 4.3: NFCe Emission and Stock Decrease
 */
public enum NfceStatus {
    PENDING,    // Waiting for emission
    EMITTED,    // Successfully emitted
    FAILED,     // Emission failed
    CANCELLED   // NFCe cancelled
}
