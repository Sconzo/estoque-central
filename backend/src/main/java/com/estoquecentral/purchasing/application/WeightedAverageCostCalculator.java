package com.estoquecentral.purchasing.application;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * WeightedAverageCostCalculator - Calculates weighted average cost for inventory
 * Story 3.4: Receiving Processing and Weighted Average Cost Update
 *
 * Formula: (currentQty * currentCost + receivedQty * receivedCost) / (currentQty + receivedQty)
 * Special case: If currentQty = 0, newCost = receivedCost
 */
@Service
public class WeightedAverageCostCalculator {

    /**
     * Calculates new weighted average cost
     *
     * @param currentQty Current stock quantity
     * @param currentCost Current average cost
     * @param receivedQty Quantity being received
     * @param receivedCost Cost of received items
     * @return New weighted average cost rounded to 2 decimal places
     */
    public BigDecimal calculateNewCost(
            BigDecimal currentQty,
            BigDecimal currentCost,
            BigDecimal receivedQty,
            BigDecimal receivedCost
    ) {
        // Validate inputs
        if (currentQty == null || currentQty.compareTo(BigDecimal.ZERO) < 0) {
            currentQty = BigDecimal.ZERO;
        }
        if (currentCost == null) {
            currentCost = BigDecimal.ZERO;
        }
        if (receivedQty == null || receivedQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Received quantity must be positive");
        }
        if (receivedCost == null || receivedCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Received cost cannot be negative");
        }

        // Special case: no current stock, new cost = received cost
        if (currentQty.compareTo(BigDecimal.ZERO) == 0) {
            return receivedCost.setScale(2, RoundingMode.HALF_UP);
        }

        // Calculate weighted average
        // totalValue = (currentQty * currentCost) + (receivedQty * receivedCost)
        BigDecimal currentValue = currentQty.multiply(currentCost);
        BigDecimal receivedValue = receivedQty.multiply(receivedCost);
        BigDecimal totalValue = currentValue.add(receivedValue);

        // totalQty = currentQty + receivedQty
        BigDecimal totalQty = currentQty.add(receivedQty);

        // newCost = totalValue / totalQty
        return totalValue.divide(totalQty, 2, RoundingMode.HALF_UP);
    }
}
