package com.estoquecentral.catalog.domain;

/**
 * ProductType - Enum for product types
 *
 * <p>Defines the type of product:
 * <ul>
 *   <li>SIMPLE - Standard product with single SKU (no variants)</li>
 *   <li>VARIANT_PARENT - Parent product that has multiple variants (Story 2.3)</li>
 *   <li>VARIANT - Child variant of a parent product (Story 2.3)</li>
 *   <li>COMPOSITE - Composite product/kit with BOM (Story 2.4)</li>
 * </ul>
 */
public enum ProductType {
    /**
     * Standard product with single SKU
     * Example: "Notebook Dell Inspiron 15"
     */
    SIMPLE,

    /**
     * Parent product with variants
     * Example: "T-shirt Basic" (parent of size/color variants)
     */
    VARIANT_PARENT,

    /**
     * Child variant product
     * Example: "T-shirt Basic - Red - M"
     */
    VARIANT,

    /**
     * Composite product/kit with Bill of Materials (BOM)
     * Example: "Kit Churrasco" (composed of carvão + acendedor + espetos)
     */
    COMPOSITE
}
