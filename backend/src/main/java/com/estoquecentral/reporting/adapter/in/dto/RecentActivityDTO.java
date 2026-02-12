package com.estoquecentral.reporting.adapter.in.dto;

import java.time.LocalDateTime;

/**
 * Recent Activity DTO
 * Represents a recent activity event (inventory movement or order status change)
 */
public record RecentActivityDTO(
        String tipo,
        String descricao,
        LocalDateTime timestamp
) {}
