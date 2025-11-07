package com.estoquecentral.catalog.domain;

/**
 * ProductType - Enum for product types
 *
 * <p>Defines the type of product:
 * <ul>
 *   <li>SIMPLE - Standard product with single SKU (no variants)</li>
 *   <li>VARIANT_PARENT - Parent product that has multiple variants (future)</li>
 *   <li>VARIANT - Child variant of a parent product (future)</li>
 * </ul>
 *
 * <p><strong>Story 2.2 scope:</strong> Only SIMPLE products are supported.
 * VARIANT_PARENT and VARIANT will be implemented in Story 2.3.
 */
public enum ProductType {
    /**
     * Standard product with single SKU
     * Example: "Notebook Dell Inspiron 15"
     */
    SIMPLE,

    /**
     * Parent product with variants (future)
     * Example: "T-shirt Basic" (parent of size/color variants)
     */
    VARIANT_PARENT,

    /**
     * Child variant product (future)
     * Example: "T-shirt Basic - Red - M"
     */
    VARIANT
}
