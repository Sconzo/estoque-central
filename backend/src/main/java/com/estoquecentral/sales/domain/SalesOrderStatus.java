package com.estoquecentral.sales.domain;

/**
 * SalesOrderStatus - Status lifecycle for B2B sales orders
 * Story 4.5: Sales Order B2B Interface
 * Story 4.6: Stock Reservation and Automatic Release - Added EXPIRED status
 */
public enum SalesOrderStatus {
    DRAFT,          // Draft order, being created/edited
    CONFIRMED,      // Confirmed, ready to process (stock validation passed)
    INVOICED,       // Invoice issued, order completed
    CANCELLED,      // Cancelled order
    EXPIRED         // Confirmed order expired (auto-released after N days)
}
