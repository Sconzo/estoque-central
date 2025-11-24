package com.estoquecentral.sales.domain;

/**
 * Payment methods for sales and payments
 * Story 4.3: NFCe Emission and Stock Decrease
 */
public enum PaymentMethod {
    DINHEIRO,      // Cash
    DEBITO,        // Debit card
    CREDITO,       // Credit card
    PIX,           // PIX instant payment
    CREDIT_CARD,   // Credit card (English variant)
    DEBIT_CARD,    // Debit card (English variant)
    BOLETO         // Bank slip/boleto
}
