package com.estoquecentral.sales.domain;

public enum PaymentMethod {
    CREDIT_CARD,     // Cartão de crédito
    DEBIT_CARD,      // Cartão de débito
    PIX,             // PIX (instant payment - Brazil)
    BOLETO,          // Boleto bancário (bank slip - Brazil)
    BANK_TRANSFER,   // Transferência bancária
    PAYPAL,          // PayPal
    WALLET,          // Carteira digital (Apple Pay, Google Pay, etc.)
    CASH             // Dinheiro
}
