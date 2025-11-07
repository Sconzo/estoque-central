package com.estoquecentral.reporting.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sales by Channel Summary DTO
 * Complete sales statistics by channel
 */
public record SalesByChannelSummaryDTO(
        String salesChannel,
        Long totalOrders,
        Long uniqueCustomers,
        BigDecimal ordersPerCustomer,
        Long totalItems,
        BigDecimal averageItemsPerOrder,
        BigDecimal totalSubtotal,
        BigDecimal totalDiscount,
        BigDecimal totalShipping,
        BigDecimal totalSales,
        BigDecimal averageTicket,
        BigDecimal minTicket,
        BigDecimal maxTicket,
        BigDecimal discountPercentage,
        LocalDateTime firstSale,
        LocalDateTime lastSale,
        Long pendingOrders,
        Long confirmedOrders,
        Long deliveredOrders,
        Long paidOrders
) {
    public SalesByChannelSummaryDTO {
        totalOrders = totalOrders != null ? totalOrders : 0L;
        uniqueCustomers = uniqueCustomers != null ? uniqueCustomers : 0L;
        ordersPerCustomer = ordersPerCustomer != null ? ordersPerCustomer : BigDecimal.ZERO;
        totalItems = totalItems != null ? totalItems : 0L;
        averageItemsPerOrder = averageItemsPerOrder != null ? averageItemsPerOrder : BigDecimal.ZERO;
        totalSubtotal = totalSubtotal != null ? totalSubtotal : BigDecimal.ZERO;
        totalDiscount = totalDiscount != null ? totalDiscount : BigDecimal.ZERO;
        totalShipping = totalShipping != null ? totalShipping : BigDecimal.ZERO;
        totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
        averageTicket = averageTicket != null ? averageTicket : BigDecimal.ZERO;
        minTicket = minTicket != null ? minTicket : BigDecimal.ZERO;
        maxTicket = maxTicket != null ? maxTicket : BigDecimal.ZERO;
        discountPercentage = discountPercentage != null ? discountPercentage : BigDecimal.ZERO;
        pendingOrders = pendingOrders != null ? pendingOrders : 0L;
        confirmedOrders = confirmedOrders != null ? confirmedOrders : 0L;
        deliveredOrders = deliveredOrders != null ? deliveredOrders : 0L;
        paidOrders = paidOrders != null ? paidOrders : 0L;
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
     * Returns payment rate (paid / total)
     */
    public double getPaymentRate() {
        if (totalOrders == null || totalOrders == 0) {
            return 0.0;
        }
        return ((double) paidOrders / totalOrders) * 100;
    }

    /**
     * Returns delivery rate (delivered / total)
     */
    public double getDeliveryRate() {
        if (totalOrders == null || totalOrders == 0) {
            return 0.0;
        }
        return ((double) deliveredOrders / totalOrders) * 100;
    }

    /**
     * Returns customer retention (orders per customer > 1)
     */
    public boolean hasCustomerRetention() {
        return ordersPerCustomer.compareTo(BigDecimal.ONE) > 0;
    }
}
