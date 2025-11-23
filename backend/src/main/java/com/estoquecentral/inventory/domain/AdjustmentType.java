package com.estoquecentral.inventory.domain;

/**
 * AdjustmentType - Types of stock adjustments
 * Story 3.5: Stock Adjustment
 */
public enum AdjustmentType {
    /**
     * Manual stock increase (entry without purchase order)
     */
    INCREASE,

    /**
     * Manual stock decrease (exit for loss, damage, theft, etc.)
     */
    DECREASE
}
