package com.estoquecentral.inventory.domain;

/**
 * MovementType - Enum for inventory movement types
 *
 * <p>Defines the type of inventory movement:
 * <ul>
 *   <li>IN - Incoming stock (purchase, return from customer)</li>
 *   <li>OUT - Outgoing stock (sale, return to supplier)</li>
 *   <li>ADJUSTMENT - Manual adjustment (correction, count)</li>
 *   <li>TRANSFER - Transfer between locations (future)</li>
 *   <li>RESERVE - Reserve quantity for order</li>
 *   <li>UNRESERVE - Cancel reservation</li>
 * </ul>
 */
public enum MovementType {
    /**
     * Incoming stock - increases quantity
     */
    IN,

    /**
     * Outgoing stock - decreases quantity
     */
    OUT,

    /**
     * Manual adjustment - can increase or decrease
     */
    ADJUSTMENT,

    /**
     * Transfer between locations (future)
     */
    TRANSFER,

    /**
     * Reserve quantity for order
     */
    RESERVE,

    /**
     * Cancel reservation
     */
    UNRESERVE
}
