package com.estoquecentral.inventory.domain;

/**
 * MovementReason - Enum for inventory movement reasons
 *
 * <p>Provides detailed reason codes for movements:
 * <ul>
 *   <li>PURCHASE - Incoming from supplier purchase</li>
 *   <li>SALE - Outgoing from customer sale</li>
 *   <li>RETURN - Return (from customer or to supplier)</li>
 *   <li>ADJUSTMENT - Manual adjustment/correction</li>
 *   <li>TRANSFER - Transfer between locations</li>
 *   <li>DAMAGED - Damaged goods write-off</li>
 *   <li>LOST - Lost goods write-off</li>
 *   <li>FOUND - Found goods adjustment</li>
 *   <li>INITIAL - Initial inventory setup</li>
 *   <li>RESERVATION - Quantity reserved</li>
 *   <li>UNRESERVATION - Reservation cancelled</li>
 * </ul>
 */
public enum MovementReason {
    /**
     * Incoming from supplier purchase
     */
    PURCHASE,

    /**
     * Outgoing from customer sale
     */
    SALE,

    /**
     * Return (from customer or to supplier)
     */
    RETURN,

    /**
     * Manual adjustment/correction
     */
    ADJUSTMENT,

    /**
     * Transfer between locations
     */
    TRANSFER,

    /**
     * Damaged goods write-off
     */
    DAMAGED,

    /**
     * Lost goods write-off
     */
    LOST,

    /**
     * Found goods adjustment
     */
    FOUND,

    /**
     * Initial inventory setup
     */
    INITIAL,

    /**
     * Quantity reserved for order
     */
    RESERVATION,

    /**
     * Reservation cancelled
     */
    UNRESERVATION
}
