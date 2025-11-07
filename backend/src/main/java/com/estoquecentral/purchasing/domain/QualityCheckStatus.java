package com.estoquecentral.purchasing.domain;

public enum QualityCheckStatus {
    PENDING,     // Quality check pending
    APPROVED,    // All items approved
    REJECTED,    // All items rejected
    PARTIAL      // Some items approved, some rejected
}
