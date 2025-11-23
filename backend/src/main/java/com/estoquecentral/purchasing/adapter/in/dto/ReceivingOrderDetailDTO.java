package com.estoquecentral.purchasing.adapter.in.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO for purchase order receiving details (Story 3.3 AC4)
 * Used in mobile receiving interface to show order details for scanning
 */
public class ReceivingOrderDetailDTO {
    private UUID id;
    private String orderNumber;
    private String supplierName;
    private String stockLocationName;
    private List<ReceivingItemDTO> items;

    public ReceivingOrderDetailDTO() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getStockLocationName() {
        return stockLocationName;
    }

    public void setStockLocationName(String stockLocationName) {
        this.stockLocationName = stockLocationName;
    }

    public List<ReceivingItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ReceivingItemDTO> items) {
        this.items = items;
    }
}
