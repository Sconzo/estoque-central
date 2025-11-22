package com.estoquecentral.inventory.application;

import com.estoquecentral.inventory.adapter.in.dto.CreateStockMovementRequest;
import com.estoquecentral.inventory.adapter.in.dto.StockMovementFilters;
import com.estoquecentral.inventory.adapter.in.dto.StockMovementResponse;
import com.estoquecentral.inventory.adapter.out.InventoryRepository;
import com.estoquecentral.inventory.adapter.out.StockMovementRepository;
import com.estoquecentral.inventory.domain.Inventory;
import com.estoquecentral.inventory.domain.StockMovement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * StockMovementService - Business logic for stock movement history
 * Story 2.8: Stock Movement History
 *
 * AC1: Create stock movements with balance tracking
 * AC2: Query movements with filters
 * AC3: Provide complete audit trail
 * AC4: Validate balance consistency
 */
@Service
@Transactional(readOnly = true)
public class StockMovementService {

    private final StockMovementRepository movementRepository;
    private final InventoryRepository inventoryRepository;
    private final JdbcTemplate jdbcTemplate;

    public StockMovementService(StockMovementRepository movementRepository,
                               InventoryRepository inventoryRepository,
                               JdbcTemplate jdbcTemplate) {
        this.movementRepository = movementRepository;
        this.inventoryRepository = inventoryRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ============================================================
    // AC1: Create Stock Movement
    // ============================================================

    /**
     * Creates a stock movement and updates inventory
     * This method is transactional and ensures consistency between movement and inventory
     *
     * NOTE: This is for manual movements. Automated movements (SALE, PURCHASE, etc.)
     * should be created through their respective services.
     */
    @Transactional
    public StockMovementResponse createMovement(UUID tenantId, CreateStockMovementRequest request, UUID userId) {
        request.validate();

        // Get current inventory
        Inventory inventory = getOrCreateInventory(tenantId, request);

        // Get balance before movement
        BigDecimal balanceBefore = inventory.getQuantityAvailable();

        // Create movement record
        StockMovement movement = new StockMovement(
                tenantId,
                request.getProductId(),
                request.getVariantId(),
                request.getStockLocationId(),
                request.getType(),
                request.getQuantity(),
                balanceBefore,
                userId,
                request.getDocumentType(),
                request.getDocumentId(),
                request.getReason()
        );

        movement.validate();

        // Update inventory based on movement type
        updateInventoryForMovement(inventory, request.getQuantity(), request.getType());

        // Save both (order matters: inventory first, then movement)
        inventoryRepository.save(inventory);
        StockMovement savedMovement = movementRepository.save(movement);

        // Convert to response and enrich with names
        StockMovementResponse response = StockMovementResponse.fromEntity(savedMovement);
        enrichWithNames(List.of(response), tenantId);

        return response;
    }

    /**
     * Internal method to create a movement without updating inventory
     * Used by other services (e.g., SaleService, PurchaseService) that manage inventory themselves
     */
    @Transactional
    public StockMovement recordMovement(UUID tenantId, UUID productId, UUID variantId, UUID locationId,
                                       com.estoquecentral.inventory.domain.MovementType type,
                                       BigDecimal quantity, BigDecimal balanceBefore, UUID userId,
                                       String documentType, UUID documentId, String reason) {
        StockMovement movement = new StockMovement(
                tenantId, productId, variantId, locationId, type,
                quantity, balanceBefore, userId, documentType, documentId, reason
        );

        movement.validate();
        return movementRepository.save(movement);
    }

    // ============================================================
    // AC2: Query Movements with Filters
    // ============================================================

    /**
     * Get movements with flexible filtering
     */
    public List<StockMovementResponse> getMovements(UUID tenantId, StockMovementFilters filters) {
        filters.validate();

        List<StockMovement> movements;

        // Apply filters based on what's provided
        if (filters.getProductId() != null && filters.getLocationId() != null) {
            movements = movementRepository.findByTenantIdAndProductIdAndLocationId(
                    tenantId, filters.getProductId(), filters.getLocationId()
            );
        } else if (filters.getProductId() != null) {
            movements = movementRepository.findByTenantIdAndProductId(tenantId, filters.getProductId());
        } else if (filters.getVariantId() != null && filters.getLocationId() != null) {
            movements = movementRepository.findByTenantIdAndVariantIdAndLocationId(
                    tenantId, filters.getVariantId(), filters.getLocationId()
            );
        } else if (filters.getVariantId() != null) {
            movements = movementRepository.findByTenantIdAndVariantId(tenantId, filters.getVariantId());
        } else if (filters.getLocationId() != null) {
            movements = movementRepository.findByTenantIdAndLocationId(tenantId, filters.getLocationId());
        } else if (filters.getDocumentType() != null && filters.getDocumentId() != null) {
            movements = movementRepository.findByTenantIdAndDocument(
                    tenantId, filters.getDocumentType(), filters.getDocumentId()
            );
        } else if (filters.getUserId() != null) {
            movements = movementRepository.findByTenantIdAndUserId(tenantId, filters.getUserId());
        } else if (filters.getType() != null) {
            movements = movementRepository.findByTenantIdAndType(tenantId, filters.getType().name());
        } else if (filters.getStartDate() != null && filters.getEndDate() != null) {
            movements = movementRepository.findByTenantIdAndDateRange(
                    tenantId, filters.getStartDate(), filters.getEndDate()
            );
        } else {
            // No specific filter - get all (limited)
            movements = movementRepository.findAllByTenantId(tenantId);
        }

        // Apply additional filters in memory if needed
        movements = applyAdditionalFilters(movements, filters);

        // Convert to responses and enrich
        List<StockMovementResponse> responses = movements.stream()
                .map(StockMovementResponse::fromEntity)
                .collect(Collectors.toList());

        enrichWithNames(responses, tenantId);

        return responses;
    }

    /**
     * Get movement timeline for a specific product/variant
     * AC3: Complete audit trail
     */
    public List<StockMovementResponse> getMovementTimeline(UUID tenantId, UUID productId, UUID variantId, UUID locationId) {
        List<StockMovement> movements;

        if (productId != null && locationId != null) {
            movements = movementRepository.findByTenantIdAndProductIdAndLocationId(tenantId, productId, locationId);
        } else if (productId != null) {
            movements = movementRepository.findByTenantIdAndProductId(tenantId, productId);
        } else if (variantId != null && locationId != null) {
            movements = movementRepository.findByTenantIdAndVariantIdAndLocationId(tenantId, variantId, locationId);
        } else if (variantId != null) {
            movements = movementRepository.findByTenantIdAndVariantId(tenantId, variantId);
        } else {
            throw new IllegalArgumentException("Must provide productId or variantId");
        }

        List<StockMovementResponse> responses = movements.stream()
                .map(StockMovementResponse::fromEntity)
                .collect(Collectors.toList());

        enrichWithNames(responses, tenantId);

        return responses;
    }

    // ============================================================
    // AC4: Validate Balance Consistency
    // ============================================================

    /**
     * Validates that the movement history balance matches the current inventory
     */
    public boolean validateBalance(UUID tenantId, UUID productId, UUID variantId, UUID locationId) {
        // Get latest movement balance
        StockMovement latestMovement;
        if (productId != null) {
            latestMovement = movementRepository.findLatestByTenantIdAndProductIdAndLocationId(
                    tenantId, productId, locationId
            );
        } else {
            latestMovement = movementRepository.findLatestByTenantIdAndVariantIdAndLocationId(
                    tenantId, variantId, locationId
            );
        }

        if (latestMovement == null) {
            // No movements yet - inventory should be zero
            return true;
        }

        // Get current inventory
        Inventory inventory;
        if (productId != null) {
            inventory = inventoryRepository.findByTenantIdAndProductIdAndLocationId(
                    tenantId, productId, locationId
            ).orElse(null);
        } else {
            inventory = inventoryRepository.findByTenantIdAndVariantIdAndLocationId(
                    tenantId, variantId, locationId
            ).orElse(null);
        }

        if (inventory == null) {
            return false;
        }

        // Compare balances
        return latestMovement.getBalanceAfter().compareTo(inventory.getQuantityAvailable()) == 0;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private Inventory getOrCreateInventory(UUID tenantId, CreateStockMovementRequest request) {
        if (request.getProductId() != null) {
            return inventoryRepository.findByTenantIdAndProductIdAndLocationId(
                    tenantId, request.getProductId(), request.getStockLocationId()
            ).orElseGet(() -> {
                // Create new inventory with zero balance
                Inventory inv = new Inventory(tenantId, request.getProductId(), request.getStockLocationId(), BigDecimal.ZERO);
                return inventoryRepository.save(inv);
            });
        } else {
            return inventoryRepository.findByTenantIdAndVariantIdAndLocationId(
                    tenantId, request.getVariantId(), request.getStockLocationId()
            ).orElseGet(() -> {
                Inventory inv = new Inventory(tenantId, request.getVariantId(), request.getStockLocationId());
                return inventoryRepository.save(inv);
            });
        }
    }

    private void updateInventoryForMovement(Inventory inventory, BigDecimal quantity,
                                           com.estoquecentral.inventory.domain.MovementType type) {
        switch (type) {
            case ENTRY:
            case TRANSFER_IN:
            case PURCHASE:
            case RELEASE:
            case BOM_DISASSEMBLY:
                // Add to inventory
                inventory.addQuantity(quantity);
                break;

            case EXIT:
            case TRANSFER_OUT:
            case SALE:
            case BOM_ASSEMBLY:
                // Remove from inventory (quantity should be positive, we negate it)
                inventory.removeQuantity(quantity);
                break;

            case ADJUSTMENT:
                // Adjustment can be positive or negative
                if (quantity.compareTo(BigDecimal.ZERO) > 0) {
                    inventory.addQuantity(quantity);
                } else {
                    inventory.removeQuantity(quantity.abs());
                }
                break;

            case RESERVE:
                // Reserve doesn't change quantity_available, it changes reserved_quantity
                inventory.reserve(quantity);
                break;

            default:
                throw new IllegalArgumentException("Unknown movement type: " + type);
        }
    }

    private List<StockMovement> applyAdditionalFilters(List<StockMovement> movements, StockMovementFilters filters) {
        // Apply type filter if not already applied
        if (filters.getType() != null) {
            movements = movements.stream()
                    .filter(m -> m.getType() == filters.getType())
                    .collect(Collectors.toList());
        }

        // Apply date range if not already applied
        if (filters.getStartDate() != null) {
            movements = movements.stream()
                    .filter(m -> m.getCreatedAt().isAfter(filters.getStartDate()) ||
                                m.getCreatedAt().isEqual(filters.getStartDate()))
                    .collect(Collectors.toList());
        }
        if (filters.getEndDate() != null) {
            movements = movements.stream()
                    .filter(m -> m.getCreatedAt().isBefore(filters.getEndDate()) ||
                                m.getCreatedAt().isEqual(filters.getEndDate()))
                    .collect(Collectors.toList());
        }

        return movements;
    }

    private void enrichWithNames(List<StockMovementResponse> responses, UUID tenantId) {
        if (responses.isEmpty()) {
            return;
        }

        // Collect all IDs we need to look up
        List<UUID> productIds = responses.stream()
                .map(StockMovementResponse::getProductId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        List<UUID> variantIds = responses.stream()
                .map(StockMovementResponse::getVariantId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        List<UUID> locationIds = responses.stream()
                .map(StockMovementResponse::getStockLocationId)
                .distinct()
                .collect(Collectors.toList());

        // Query names (simplified - in production would use batch queries)
        String sql = """
            SELECT
                sm.id,
                COALESCE(p.name, parent_p.name || ' - ' || pv.sku) as product_name,
                COALESCE(p.sku, pv.sku) as product_sku,
                l.name as location_name,
                l.code as location_code
            FROM stock_movements sm
            LEFT JOIN products p ON sm.product_id = p.id
            LEFT JOIN product_variants pv ON sm.variant_id = pv.id
            LEFT JOIN products parent_p ON pv.parent_product_id = parent_p.id
            INNER JOIN locations l ON sm.stock_location_id = l.id
            WHERE sm.tenant_id = ?
              AND sm.id IN (?)
            """;

        // Build a map of movement ID to names
        String movementIds = responses.stream()
                .map(r -> "'" + r.getId().toString() + "'")
                .collect(Collectors.joining(","));

        String finalSql = sql.replace("(?)", "(" + movementIds + ")");

        jdbcTemplate.query(finalSql, new Object[]{tenantId}, rs -> {
            UUID movementId = UUID.fromString(rs.getString("id"));
            StockMovementResponse response = responses.stream()
                    .filter(r -> r.getId().equals(movementId))
                    .findFirst()
                    .orElse(null);

            if (response != null) {
                response.setProductName(rs.getString("product_name"));
                response.setProductSku(rs.getString("product_sku"));
                response.setLocationName(rs.getString("location_name"));
                response.setLocationCode(rs.getString("location_code"));
            }
        });

        // Note: User names would be enriched here as well in a real implementation
        // For now, we'll leave userName as null (would require user service integration)
    }
}
