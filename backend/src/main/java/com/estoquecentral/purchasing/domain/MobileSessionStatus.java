package com.estoquecentral.purchasing.domain;

public enum MobileSessionStatus {
    IN_PROGRESS,    // Session active, scanning in progress
    PAUSED,         // Session paused
    COMPLETED,      // Session completed successfully
    CANCELLED       // Session cancelled
}
