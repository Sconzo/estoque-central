package com.estoquecentral.marketplace.domain;

/**
 * Status of marketplace synchronization
 * Story 5.4: Stock Synchronization to Mercado Livre
 */
public enum SyncStatus {
    PENDING,     // Waiting to be processed
    PROCESSING,  // Currently being processed
    SUCCESS,     // Successfully completed
    ERROR,       // Failed with error
    FAILED       // Failed after all retries
}
