package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Daily Sales by Channel DTO
 * Shows sales breakdown by sales channel for current day
 */
public record DailySalesByChannelDTO(
        String salesChannel,
        Integer orderCount,
        Integer itemCount,
        BigDecimal totalQuantity,
        BigDecimal totalSales,
        BigDecimal averageTicket,
        LocalDateTime firstOrderTime,
        LocalDateTime lastOrderTime
) {
    public DailySalesByChannelDTO {
        orderCount = orderCount != null ? orderCount : 0;
        itemCount = itemCount != null ? itemCount : 0;
        totalQuantity = totalQuantity != null ? totalQuantity : BigDecimal.ZERO;
        totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
        averageTicket = averageTicket != null ? averageTicket : BigDecimal.ZERO;
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
     * Returns average items per order
     */
    public double getAverageItemsPerOrder() {
        if (orderCount == null || orderCount == 0) {
            return 0.0;
        }
        return (double) itemCount / orderCount;
    }
}
