package com.estoquecentral.marketplace.domain;

/**
 * Status of marketplace orders
 * Story 5.5: Import and Process Orders from Mercado Livre - AC1
 */
public enum OrderStatus {
    PENDING,      // Order created, awaiting payment
    PAID,         // Payment confirmed
    SHIPPED,      // Order shipped
    DELIVERED,    // Order delivered
    CANCELLED     // Order cancelled
}
