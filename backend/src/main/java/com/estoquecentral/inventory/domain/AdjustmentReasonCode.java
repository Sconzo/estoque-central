package com.estoquecentral.inventory.domain;

/**
 * AdjustmentReasonCode - Predefined reason codes for stock adjustments
 * Story 3.5: Stock Adjustment
 */
public enum AdjustmentReasonCode {
    /**
     * Physical inventory count correction
     */
    INVENTORY,

    /**
     * Product loss (untracked exit)
     */
    LOSS,

    /**
     * Product damaged or broken
     */
    DAMAGE,

    /**
     * Product theft
     */
    THEFT,

    /**
     * Error in previous stock entry
     */
    ERROR,

    /**
     * Other reason (requires detailed description)
     */
    OTHER
}
