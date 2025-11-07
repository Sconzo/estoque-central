package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pending Orders Summary DTO
 * Orders pending fulfillment by channel and status
 */
public record PendingOrdersSummaryDTO(
        String salesChannel,
        String status,
        Integer orderCount,
        Integer totalItems,
        BigDecimal totalValue,
        BigDecimal averageOrderValue,
        LocalDateTime oldestOrderDate,
        LocalDateTime newestOrderDate,
        Integer overdueCount
) {
    public PendingOrdersSummaryDTO {
        orderCount = orderCount != null ? orderCount : 0;
        totalItems = totalItems != null ? totalItems : 0;
        totalValue = totalValue != null ? totalValue : BigDecimal.ZERO;
        averageOrderValue = averageOrderValue != null ? averageOrderValue : BigDecimal.ZERO;
        overdueCount = overdueCount != null ? overdueCount : 0;
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
     * Returns status display name
     */
    public String getStatusDisplayName() {
        return switch (status) {
            case "PENDING" -> "Pendente";
            case "PROCESSING" -> "Processando";
            case "CONFIRMED" -> "Confirmado";
            case "READY_TO_SHIP" -> "Pronto p/ Envio";
            default -> status;
        };
    }

    /**
     * Returns true if has overdue orders
     */
    public boolean hasOverdueOrders() {
        return overdueCount != null && overdueCount > 0;
    }

    /**
     * Returns overdue percentage
     */
    public double getOverduePercentage() {
        if (orderCount == null || orderCount == 0) {
            return 0.0;
        }
        return ((double) overdueCount / orderCount) * 100;
    }
}
