package com.estoquecentral.purchasing.domain;

public enum ScanMatchStatus {
    MATCHED,            // Barcode matched to product in PO
    UNMATCHED,          // Barcode not found
    MULTIPLE_MATCHES,   // Multiple products matched
    MANUAL_MATCH        // Manually matched by user
}
