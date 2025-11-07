package com.estoquecentral.purchasing.domain;

public enum SupplierStatus {
    ACTIVE,              // Active and can receive purchase orders
    INACTIVE,            // Temporarily inactive
    BLOCKED,             // Blocked due to issues
    PENDING_APPROVAL     // Pending approval to become active
}
