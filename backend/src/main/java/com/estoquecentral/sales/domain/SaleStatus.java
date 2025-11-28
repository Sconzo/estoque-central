package com.estoquecentral.sales.domain;

/**
 * SaleStatus - Status of a sale
 * Story 5.6: Process Mercado Livre Cancellations - AC3
 */
public enum SaleStatus {
    ACTIVE("Ativa", "✅"),
    CANCELLED("Cancelada", "❌");

    private final String displayName;
    private final String icon;

    SaleStatus(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }
}
