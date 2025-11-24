package com.estoquecentral.purchasing.domain;

public enum PurchaseOrderStatus {
    DRAFT,                  // Draft, being created
    PENDING_APPROVAL,       // Waiting for approval
    APPROVED,               // Approved, ready to send
    SENT_TO_SUPPLIER,       // Sent to supplier
    PARTIALLY_RECEIVED,     // Some items received
    RECEIVED,               // All items received
    COMPLETED,              // Completed (all items fully received)
    CANCELLED,              // Cancelled
    CLOSED                  // Closed (finished/archived)
}
