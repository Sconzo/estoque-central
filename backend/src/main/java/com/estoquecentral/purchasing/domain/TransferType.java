package com.estoquecentral.purchasing.domain;

public enum TransferType {
    STANDARD,      // Standard transfer
    EMERGENCY,     // Emergency/urgent transfer
    REBALANCING,   // Stock rebalancing between locations
    RETURN         // Return transfer
}
