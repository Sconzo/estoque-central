package com.estoquecentral.sales.domain;

public enum WebhookStatus {
    PENDING,      // Webhook received, waiting to be processed
    PROCESSING,   // Webhook currently being processed
    PROCESSED,    // Webhook successfully processed
    FAILED,       // Webhook processing failed
    IGNORED       // Webhook ignored (duplicate or irrelevant)
}
