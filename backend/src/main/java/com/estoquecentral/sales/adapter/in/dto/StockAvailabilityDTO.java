package com.estoquecentral.sales.adapter.in.dto;

import java.math.BigDecimal;

/**
 * DTO for stock availability endpoint response
 * Story 4.5: Sales Order B2B Interface
 */
public class StockAvailabilityDTO {

    private BigDecimal available;
    private BigDecimal reserved;
    private BigDecimal forSale;
    private boolean inStock;

    public StockAvailabilityDTO() {}

    public StockAvailabilityDTO(BigDecimal available, BigDecimal reserved, BigDecimal forSale, boolean inStock) {
        this.available = available;
        this.reserved = reserved;
        this.forSale = forSale;
        this.inStock = inStock;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    public BigDecimal getReserved() {
        return reserved;
    }

    public void setReserved(BigDecimal reserved) {
        this.reserved = reserved;
    }

    public BigDecimal getForSale() {
        return forSale;
    }

    public void setForSale(BigDecimal forSale) {
        this.forSale = forSale;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
}
