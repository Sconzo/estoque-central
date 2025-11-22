package com.estoquecentral.inventory.domain;

/**
 * MovementType - Types of stock movements
 * Story 2.8: Stock Movement History - AC2
 */
public enum MovementType {
    ENTRY("Entrada Manual", "📥"),
    EXIT("Saída Manual", "📤"),
    TRANSFER_OUT("Transferência - Saída", "🔄"),
    TRANSFER_IN("Transferência - Entrada", "🔄"),
    ADJUSTMENT("Ajuste de Inventário", "⚖️"),
    SALE("Venda", "🛒"),
    PURCHASE("Compra", "📦"),
    RESERVE("Reserva", "🔒"),
    RELEASE("Liberação de Reserva", "🔓"),
    BOM_ASSEMBLY("Montagem de Kit", "🔧"),
    BOM_DISASSEMBLY("Desmontagem de Kit", "🔨");

    private final String displayName;
    private final String icon;

    MovementType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isEntry() {
        return this == ENTRY || this == TRANSFER_IN || this == PURCHASE || this == RELEASE;
    }

    public boolean isExit() {
        return this == EXIT || this == TRANSFER_OUT || this == SALE || this == RESERVE;
    }

    public boolean isAdjustable() {
        return this == ADJUSTMENT || this == BOM_ASSEMBLY || this == BOM_DISASSEMBLY;
    }
}
