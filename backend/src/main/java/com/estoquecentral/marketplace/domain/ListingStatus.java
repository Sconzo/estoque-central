package com.estoquecentral.marketplace.domain;

/**
 * ListingStatus enum - Marketplace listing statuses
 * Story 5.2: Import Products from Mercado Livre
 */
public enum ListingStatus {
    /**
     * Listing is active and visible on marketplace
     */
    ACTIVE,

    /**
     * Listing is paused (not visible but can be reactivated)
     */
    PAUSED,

    /**
     * Listing is permanently closed
     */
    CLOSED,

    /**
     * Listing has synchronization errors
     */
    ERROR,

    /**
     * Listing is pending publication
     */
    PENDING
}
