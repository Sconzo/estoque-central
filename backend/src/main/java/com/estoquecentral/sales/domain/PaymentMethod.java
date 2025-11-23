package com.estoquecentral.sales.domain;

/**
 * Payment methods for sales
 * Story 4.3: NFCe Emission and Stock Decrease
 */
public enum PaymentMethod {
    DINHEIRO,  // Cash
    DEBITO,    // Debit card
    CREDITO,   // Credit card
    PIX        // PIX instant payment
}
