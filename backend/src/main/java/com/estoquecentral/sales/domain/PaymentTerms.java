package com.estoquecentral.sales.domain;

/**
 * PaymentTerms - Payment terms for B2B sales orders
 * Story 4.5: Sales Order B2B Interface
 */
public enum PaymentTerms {
    A_VISTA,    // Cash/Immediate payment
    DIAS_7,     // Net 7 days
    DIAS_14,    // Net 14 days
    DIAS_30,    // Net 30 days
    DIAS_60,    // Net 60 days
    DIAS_90     // Net 90 days
}
