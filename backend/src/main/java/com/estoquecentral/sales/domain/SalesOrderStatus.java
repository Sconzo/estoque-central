package com.estoquecentral.sales.domain;

/**
 * SalesOrderStatus - Status lifecycle for B2B sales orders
 * Story 4.5: Sales Order B2B Interface
 */
public enum SalesOrderStatus {
    DRAFT,          // Draft order, being created/edited
    CONFIRMED,      // Confirmed, ready to process (stock validation passed)
    INVOICED,       // Invoice issued, order completed
    CANCELLED       // Cancelled order
}
