package com.estoquecentral.sales.domain;

/**
 * Fiscal event types for audit trail
 * Story 4.3: NFCe Emission and Stock Decrease - NFR16
 */
public enum FiscalEventType {
    NFCE_EMITTED,   // NFCe successfully emitted
    NFCE_CANCELLED, // NFCe cancelled
    NFCE_FAILED,    // NFCe emission failed
    NFCE_RETRY      // NFCe retry attempt
}
