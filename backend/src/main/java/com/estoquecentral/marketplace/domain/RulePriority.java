package com.estoquecentral.marketplace.domain;

/**
 * RulePriority - Priority levels for safety margin rules
 * Story 5.7: Configurable Safety Stock Margin - AC1
 *
 * Priority determines which rule applies when multiple rules could match:
 * - PRODUCT (1): Most specific - applies to a specific product
 * - CATEGORY (2): Medium specificity - applies to all products in a category
 * - GLOBAL (3): Least specific - applies to all products in a marketplace
 */
public enum RulePriority {
    PRODUCT(1, "Produto Específico", "🎯"),
    CATEGORY(2, "Categoria", "📁"),
    GLOBAL(3, "Global (Marketplace)", "🌐");

    private final int value;
    private final String displayName;
    private final String icon;

    RulePriority(int value, String displayName, String icon) {
        this.value = value;
        this.displayName = displayName;
        this.icon = icon;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public static RulePriority fromValue(int value) {
        for (RulePriority priority : values()) {
            if (priority.value == value) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Invalid priority value: " + value);
    }

    public boolean isHigherPriorityThan(RulePriority other) {
        return this.value < other.value;
    }
}
