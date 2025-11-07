package com.estoquecentral.sales.domain;

public enum OrderStatus {
    PENDING,           // Order created, awaiting payment
    CONFIRMED,         // Payment confirmed, ready to process
    PROCESSING,        // Order being prepared/packed
    READY_TO_SHIP,     // Order ready for pickup/shipping
    SHIPPED,           // Order shipped to customer
    DELIVERED,         // Order delivered successfully
    CANCELLED,         // Order cancelled
    REFUNDED,          // Order refunded
    FAILED             // Order failed (payment/other issues)
}
