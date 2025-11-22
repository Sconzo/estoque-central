package com.estoquecentral.catalog.domain;

/**
 * BomType - Bill of Materials type
 *
 * <p>Defines how composite products/kits manage inventory:
 * <ul>
 *   <li><b>VIRTUAL</b>: Stock calculated dynamically from components.
 *       When sold, components are deducted. No stock record for the kit itself.</li>
 *   <li><b>PHYSICAL</b>: Pre-assembled kits with own stock records.
 *       Components are deducted when kit is assembled, not when sold.</li>
 * </ul>
 */
public enum BomType {
    /**
     * Virtual BOM: Kit stock is calculated from component availability.
     * Selling the kit deducts component stock automatically.
     */
    VIRTUAL,

    /**
     * Physical BOM: Pre-assembled kits with their own stock records.
     * Kits must be assembled first (deducting components), then sold as regular products.
     */
    PHYSICAL
}
