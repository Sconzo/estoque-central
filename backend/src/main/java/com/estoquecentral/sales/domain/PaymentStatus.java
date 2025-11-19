package com.estoquecentral.sales.domain;

public enum PaymentStatus {
    PENDING,              // Payment not yet processed
    AUTHORIZED,           // Payment authorized but not captured
    CAPTURED,             // Payment successfully captured
    FAILED,               // Payment failed
    REFUNDED,             // Full refund issued
    PARTIALLY_REFUNDED,   // Partial refund issued
    CANCELLED,
    EXPIRED
}
