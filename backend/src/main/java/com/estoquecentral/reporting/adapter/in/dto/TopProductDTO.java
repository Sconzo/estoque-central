package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Top Product DTO - Complete product performance metrics
 */
public record TopProductDTO(
        UUID productId,
        String sku,
        String productName,
        String categoryName,
        Long orderCount,
        Long uniqueCustomers,
        BigDecimal totalQuantitySold,
        BigDecimal totalRevenue,
        BigDecimal averagePrice,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal revenuePerUnit,
        BigDecimal avgQuantityPerOrder,
        BigDecimal currentStock,
        LocalDateTime firstSaleDate,
        LocalDateTime lastSaleDate,
        Long rankPosition
) {
    public TopProductDTO {
        orderCount = orderCount != null ? orderCount : 0L;
        uniqueCustomers = uniqueCustomers != null ? uniqueCustomers : 0L;
        totalQuantitySold = totalQuantitySold != null ? totalQuantitySold : BigDecimal.ZERO;
        totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        averagePrice = averagePrice != null ? averagePrice : BigDecimal.ZERO;
        currentStock = currentStock != null ? currentStock : BigDecimal.ZERO;
        rankPosition = rankPosition != null ? rankPosition : 0L;
    }

    public double getCustomerRetentionRate() {
        if (orderCount == null || orderCount == 0) return 0.0;
        return ((double) orderCount / uniqueCustomers);
    }
}
