package com.estoquecentral.purchasing.domain;

public enum StockTransferStatus {
    DRAFT,                  // Draft, being created
    PENDING_APPROVAL,       // Waiting for approval
    APPROVED,               // Approved, ready to ship
    IN_TRANSIT,             // Items shipped, in transit
    PARTIALLY_RECEIVED,     // Some items received
    RECEIVED,               // All items received
    CANCELLED,              // Cancelled
    REJECTED                // Rejected (not approved)
}
