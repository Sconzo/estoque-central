package com.estoquecentral.catalog.domain;

/**
 * ProductStatus - Enum for product status
 *
 * <p>Defines the current status of a product:
 * <ul>
 *   <li>ACTIVE - Product is active and available for sale</li>
 *   <li>INACTIVE - Product is temporarily inactive (not deleted)</li>
 *   <li>DISCONTINUED - Product is discontinued and no longer sold</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This is different from the soft delete flag (ativo).
 * Status is a business state, while ativo is a technical deletion flag.
 */
public enum ProductStatus {
    /**
     * Product is active and available for sale
     */
    ACTIVE,

    /**
     * Product is temporarily inactive
     */
    INACTIVE,

    /**
     * Product is discontinued and no longer sold
     */
    DISCONTINUED
}
