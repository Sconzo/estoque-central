package com.estoquecentral.sales.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ExpiringSalesOrderDTO - DTO for sales orders expiring soon
 * Story 4.6: Stock Reservation and Automatic Release - AC8, AC9
 *
 * <p>Contains order information plus calculated days until expiration
 */
public record ExpiringSalesOrderDTO(
    UUID id,
    String orderNumber,
    UUID customerId,
    String customerName,
    UUID stockLocationId,
    String locationName,
    LocalDate orderDate,
    LocalDate deliveryDateExpected,
    BigDecimal totalAmount,
    String status,
    LocalDateTime createdAt,
    int daysUntilExpiration,
    LocalDate expirationDate
) {}
