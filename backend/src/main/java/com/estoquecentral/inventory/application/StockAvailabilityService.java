package com.estoquecentral.inventory.application;

import com.estoquecentral.inventory.adapter.out.InventoryRepository;
import com.estoquecentral.inventory.domain.Inventory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * StockAvailabilityService - Service for checking stock availability
 * Story 4.5: Sales Order B2B Interface
 *
 * Provides real-time stock availability information for sales order processing.
 */
@Service
public class StockAvailabilityService {

    private final InventoryRepository inventoryRepository;

    public StockAvailabilityService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Get stock availability for a product at a location
     *
     * @param tenantId Tenant ID
     * @param productId Product ID (null if variant)
     * @param variantId Variant ID (null if product)
     * @param locationId Location ID
     * @return StockAvailabilityDTO with availability details
     */
    @Transactional(readOnly = true)
    public StockAvailabilityDTO getAvailability(
            UUID tenantId,
            UUID productId,
            UUID variantId,
            UUID locationId) {

        Optional<Inventory> inventoryOpt;

        if (variantId != null) {
            // Check variant inventory
            inventoryOpt = inventoryRepository.findByTenantIdAndVariantIdAndLocationId(
                tenantId, variantId, locationId
            );
        } else if (productId != null) {
            // Check product inventory
            inventoryOpt = inventoryRepository.findByTenantIdAndProductIdAndLocationId(
                tenantId, productId, locationId
            );
        } else {
            throw new IllegalArgumentException("Either productId or variantId must be provided");
        }

        if (inventoryOpt.isEmpty()) {
            // No inventory record = zero stock
            return new StockAvailabilityDTO(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false
            );
        }

        Inventory inventory = inventoryOpt.get();
        BigDecimal available = inventory.getQuantityAvailable() != null
            ? inventory.getQuantityAvailable()
            : BigDecimal.ZERO;
        BigDecimal reserved = inventory.getReservedQuantity() != null
            ? inventory.getReservedQuantity()
            : BigDecimal.ZERO;
        BigDecimal forSale = inventory.getComputedQuantityForSale();
        boolean inStock = forSale.compareTo(BigDecimal.ZERO) > 0;

        return new StockAvailabilityDTO(available, reserved, forSale, inStock);
    }

    /**
     * Check if sufficient stock is available for a given quantity
     *
     * @param tenantId Tenant ID
     * @param productId Product ID (null if variant)
     * @param variantId Variant ID (null if product)
     * @param locationId Location ID
     * @param requiredQuantity Required quantity
     * @return true if sufficient stock available, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isAvailable(
            UUID tenantId,
            UUID productId,
            UUID variantId,
            UUID locationId,
            BigDecimal requiredQuantity) {

        StockAvailabilityDTO availability = getAvailability(
            tenantId, productId, variantId, locationId
        );

        return availability.forSale().compareTo(requiredQuantity) >= 0;
    }

    /**
     * DTO for stock availability information
     */
    public record StockAvailabilityDTO(
        BigDecimal available,
        BigDecimal reserved,
        BigDecimal forSale,
        boolean inStock
    ) {}
}
