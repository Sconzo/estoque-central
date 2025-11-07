package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sales by Date and Channel DTO
 * Daily sales aggregated by channel
 */
public record SalesByDateChannelDTO(
        LocalDate saleDate,
        String salesChannel,
        Long orderCount,
        Long uniqueCustomers,
        Long totalItems,
        BigDecimal totalQuantity,
        BigDecimal totalSubtotal,
        BigDecimal totalDiscount,
        BigDecimal totalShipping,
        BigDecimal totalSales,
        BigDecimal averageTicket,
        BigDecimal minTicket,
        BigDecimal maxTicket,
        LocalDateTime firstSaleTime,
        LocalDateTime lastSaleTime
) {
    public SalesByDateChannelDTO {
        orderCount = orderCount != null ? orderCount : 0L;
        uniqueCustomers = uniqueCustomers != null ? uniqueCustomers : 0L;
        totalItems = totalItems != null ? totalItems : 0L;
        totalQuantity = totalQuantity != null ? totalQuantity : BigDecimal.ZERO;
        totalSubtotal = totalSubtotal != null ? totalSubtotal : BigDecimal.ZERO;
        totalDiscount = totalDiscount != null ? totalDiscount : BigDecimal.ZERO;
        totalShipping = totalShipping != null ? totalShipping : BigDecimal.ZERO;
        totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
        averageTicket = averageTicket != null ? averageTicket : BigDecimal.ZERO;
        minTicket = minTicket != null ? minTicket : BigDecimal.ZERO;
        maxTicket = maxTicket != null ? maxTicket : BigDecimal.ZERO;
    }

    /**
     * Returns channel display name
     */
    public String getChannelDisplayName() {
        return switch (salesChannel) {
            case "STORE" -> "Loja Física";
            case "ONLINE" -> "Loja Online";
            case "MARKETPLACE" -> "Marketplace";
            case "PHONE" -> "Telefone";
            case "WHATSAPP" -> "WhatsApp";
            default -> salesChannel;
        };
    }

    /**
     * Returns discount percentage
     */
    public double getDiscountPercentage() {
        if (totalSubtotal.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return totalDiscount
                .divide(totalSubtotal, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Returns orders per customer ratio
     */
    public double getOrdersPerCustomer() {
        if (uniqueCustomers == null || uniqueCustomers == 0) {
            return 0.0;
        }
        return (double) orderCount / uniqueCustomers;
    }

    /**
     * Returns average items per order
     */
    public double getAverageItemsPerOrder() {
        if (orderCount == null || orderCount == 0) {
            return 0.0;
        }
        return (double) totalItems / orderCount;
    }
}
