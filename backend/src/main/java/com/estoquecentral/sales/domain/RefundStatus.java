package com.estoquecentral.sales.domain;

public enum RefundStatus {
    PENDING,      // Refund requested, not yet processed
    PROCESSING,   // Refund being processed by gateway
    COMPLETED,    // Refund successfully completed
    FAILED,       // Refund failed
    CANCELLED     // Refund cancelled
}
