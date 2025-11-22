package com.estoquecentral.inventory.application;

import com.estoquecentral.inventory.adapter.in.dto.CreateStockTransferRequest;
import com.estoquecentral.inventory.adapter.in.dto.StockTransferResponse;
import com.estoquecentral.inventory.adapter.out.InventoryRepository;
import com.estoquecentral.inventory.adapter.out.StockTransferRepository;
import com.estoquecentral.inventory.domain.Inventory;
import com.estoquecentral.inventory.domain.MovementType;
import com.estoquecentral.inventory.domain.StockTransfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * StockTransferService - Business logic for stock transfers between locations
 * Story 2.9: Stock Transfer Between Locations
 *
 * AC2: Validação de transferências
 * AC3: Transação atômica (saída origem + entrada destino)
 * AC4: Criação de movimentações de auditoria
 * AC5: Histórico com filtros
 */
@Service
@Transactional(readOnly = true)
public class StockTransferService {

    private final StockTransferRepository transferRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;
    private final JdbcTemplate jdbcTemplate;

    public StockTransferService(StockTransferRepository transferRepository,
                               InventoryRepository inventoryRepository,
                               StockMovementService stockMovementService,
                               JdbcTemplate jdbcTemplate) {
        this.transferRepository = transferRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementService = stockMovementService;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ============================================================
    // AC2 & AC3 & AC4: Create Transfer with Atomic Transaction
    // ============================================================

    /**
     * Creates a stock transfer between locations
     * This is a critical transactional operation that:
     * 1. Validates stock availability at origin
     * 2. Creates transfer record
     * 3. Updates inventory at origin (decrease)
     * 4. Updates inventory at destination (increase)
     * 5. Creates two stock movements (TRANSFER_OUT, TRANSFER_IN)
     *
     * All operations are atomic - if any fails, everything rolls back
     */
    @Transactional
    public StockTransferResponse createTransfer(UUID tenantId, CreateStockTransferRequest request, UUID userId) {
        request.validate();

        // 1. Get origin inventory and validate availability
        Inventory originInventory = getInventoryForTransfer(tenantId, request, true);

        if (originInventory.getQuantityAvailable().compareTo(request.getQuantity()) < 0) {
            throw new IllegalArgumentException(
                String.format("Insufficient stock at origin location. Available: %s, Requested: %s",
                    originInventory.getQuantityAvailable(), request.getQuantity())
            );
        }

        // 2. Create transfer record
        StockTransfer transfer;
        if (request.getProductId() != null) {
            transfer = new StockTransfer(
                tenantId,
                request.getProductId(),
                request.getOriginLocationId(),
                request.getDestinationLocationId(),
                request.getQuantity(),
                request.getReason(),
                userId
            );
        } else {
            transfer = new StockTransfer(
                tenantId,
                request.getVariantId(),
                request.getOriginLocationId(),
                request.getDestinationLocationId(),
                request.getQuantity(),
                request.getReason(),
                userId,
                true // isVariant
            );
        }

        transfer.validate();
        StockTransfer savedTransfer = transferRepository.save(transfer);

        // 3. Update origin inventory (decrease stock)
        BigDecimal originBalanceBefore = originInventory.getQuantityAvailable();
        originInventory.removeQuantity(request.getQuantity());
        inventoryRepository.save(originInventory);
        BigDecimal originBalanceAfter = originInventory.getQuantityAvailable();

        // 4. Update destination inventory (increase stock)
        Inventory destinationInventory = getInventoryForTransfer(tenantId, request, false);
        BigDecimal destBalanceBefore = destinationInventory.getQuantityAvailable();
        destinationInventory.addQuantity(request.getQuantity());
        inventoryRepository.save(destinationInventory);
        BigDecimal destBalanceAfter = destinationInventory.getQuantityAvailable();

        // 5. Create two linked stock movements for audit trail

        // Movement 1: TRANSFER_OUT from origin
        stockMovementService.recordMovement(
            tenantId,
            request.getProductId(),
            request.getVariantId(),
            request.getOriginLocationId(),
            MovementType.TRANSFER_OUT,
            request.getQuantity().negate(), // negative for exit
            originBalanceBefore,
            userId,
            "TRANSFER",
            savedTransfer.getId(),
            "Transfer to " + getLocationName(request.getDestinationLocationId())
        );

        // Movement 2: TRANSFER_IN to destination
        stockMovementService.recordMovement(
            tenantId,
            request.getProductId(),
            request.getVariantId(),
            request.getDestinationLocationId(),
            MovementType.TRANSFER_IN,
            request.getQuantity(), // positive for entry
            destBalanceBefore,
            userId,
            "TRANSFER",
            savedTransfer.getId(),
            "Transfer from " + getLocationName(request.getOriginLocationId())
        );

        // 6. Convert to response and enrich with names
        StockTransferResponse response = StockTransferResponse.fromEntity(savedTransfer);
        enrichWithNames(List.of(response), tenantId);

        return response;
    }

    // ============================================================
    // AC5: Get Transfer History with Filters
    // ============================================================

    /**
     * Get transfer history with optional filters
     */
    public List<StockTransferResponse> getTransferHistory(UUID tenantId, StockTransferFilters filters) {
        List<StockTransfer> transfers;

        // Apply filters based on what's provided
        if (filters.getProductId() != null && filters.getStartDate() != null && filters.getEndDate() != null) {
            transfers = transferRepository.findByTenantIdAndProductIdAndDateRange(
                tenantId, filters.getProductId(), filters.getStartDate(), filters.getEndDate()
            );
        } else if (filters.getProductId() != null) {
            transfers = transferRepository.findByTenantIdAndProductId(tenantId, filters.getProductId());
        } else if (filters.getVariantId() != null) {
            transfers = transferRepository.findByTenantIdAndVariantId(tenantId, filters.getVariantId());
        } else if (filters.getOriginLocationId() != null && filters.getDestinationLocationId() != null) {
            transfers = transferRepository.findByTenantIdAndLocations(
                tenantId, filters.getOriginLocationId(), filters.getDestinationLocationId()
            );
        } else if (filters.getOriginLocationId() != null) {
            transfers = transferRepository.findByTenantIdAndOriginLocationId(tenantId, filters.getOriginLocationId());
        } else if (filters.getDestinationLocationId() != null) {
            transfers = transferRepository.findByTenantIdAndDestinationLocationId(tenantId, filters.getDestinationLocationId());
        } else if (filters.getStartDate() != null && filters.getEndDate() != null) {
            transfers = transferRepository.findByTenantIdAndDateRange(
                tenantId, filters.getStartDate(), filters.getEndDate()
            );
        } else if (filters.getUserId() != null) {
            transfers = transferRepository.findByTenantIdAndUserId(tenantId, filters.getUserId());
        } else {
            transfers = transferRepository.findByTenantId(tenantId);
        }

        // Convert to responses and enrich
        List<StockTransferResponse> responses = transfers.stream()
            .map(StockTransferResponse::fromEntity)
            .collect(Collectors.toList());

        enrichWithNames(responses, tenantId);

        return responses;
    }

    /**
     * Get all transfers for a product
     */
    public List<StockTransferResponse> getTransfersForProduct(UUID tenantId, UUID productId) {
        List<StockTransfer> transfers = transferRepository.findByTenantIdAndProductId(tenantId, productId);
        List<StockTransferResponse> responses = transfers.stream()
            .map(StockTransferResponse::fromEntity)
            .collect(Collectors.toList());
        enrichWithNames(responses, tenantId);
        return responses;
    }

    /**
     * Get all transfers from a location
     */
    public List<StockTransferResponse> getTransfersFromLocation(UUID tenantId, UUID locationId) {
        List<StockTransfer> transfers = transferRepository.findByTenantIdAndOriginLocationId(tenantId, locationId);
        List<StockTransferResponse> responses = transfers.stream()
            .map(StockTransferResponse::fromEntity)
            .collect(Collectors.toList());
        enrichWithNames(responses, tenantId);
        return responses;
    }

    /**
     * Get all transfers to a location
     */
    public List<StockTransferResponse> getTransfersToLocation(UUID tenantId, UUID locationId) {
        List<StockTransfer> transfers = transferRepository.findByTenantIdAndDestinationLocationId(tenantId, locationId);
        List<StockTransferResponse> responses = transfers.stream()
            .map(StockTransferResponse::fromEntity)
            .collect(Collectors.toList());
        enrichWithNames(responses, tenantId);
        return responses;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Gets or creates inventory for the transfer
     */
    private Inventory getInventoryForTransfer(UUID tenantId, CreateStockTransferRequest request, boolean isOrigin) {
        UUID locationId = isOrigin ? request.getOriginLocationId() : request.getDestinationLocationId();

        if (request.getProductId() != null) {
            return inventoryRepository.findByTenantIdAndProductIdAndLocationId(
                tenantId, request.getProductId(), locationId
            ).orElseGet(() -> {
                if (isOrigin) {
                    throw new IllegalArgumentException("No inventory found at origin location");
                }
                // Create new inventory at destination if doesn't exist
                Inventory newInventory = new Inventory(tenantId, request.getProductId(), locationId, BigDecimal.ZERO);
                return inventoryRepository.save(newInventory);
            });
        } else {
            return inventoryRepository.findByTenantIdAndVariantIdAndLocationId(
                tenantId, request.getVariantId(), locationId
            ).orElseGet(() -> {
                if (isOrigin) {
                    throw new IllegalArgumentException("No inventory found at origin location");
                }
                // Create new inventory at destination if doesn't exist
                Inventory newInventory = new Inventory(tenantId, request.getVariantId(), locationId);
                return inventoryRepository.save(newInventory);
            });
        }
    }

    /**
     * Gets location name by ID
     */
    private String getLocationName(UUID locationId) {
        String sql = "SELECT name FROM locations WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, locationId);
        } catch (Exception e) {
            return "Unknown Location";
        }
    }

    /**
     * Enriches responses with product and location names
     */
    private void enrichWithNames(List<StockTransferResponse> responses, UUID tenantId) {
        if (responses.isEmpty()) {
            return;
        }

        String sql = """
            SELECT
                st.id,
                COALESCE(p.name, parent_p.name || ' - ' || pv.sku) as product_name,
                COALESCE(p.sku, pv.sku) as product_sku,
                loc_origin.name as origin_name,
                loc_origin.code as origin_code,
                loc_dest.name as dest_name,
                loc_dest.code as dest_code
            FROM stock_transfers st
            LEFT JOIN products p ON st.product_id = p.id
            LEFT JOIN product_variants pv ON st.variant_id = pv.id
            LEFT JOIN products parent_p ON pv.parent_product_id = parent_p.id
            INNER JOIN locations loc_origin ON st.origin_location_id = loc_origin.id
            INNER JOIN locations loc_dest ON st.destination_location_id = loc_dest.id
            WHERE st.tenant_id = ?
              AND st.id IN (?)
            """;

        String transferIds = responses.stream()
            .map(r -> "'" + r.getId().toString() + "'")
            .collect(Collectors.joining(","));

        String finalSql = sql.replace("(?)", "(" + transferIds + ")");

        jdbcTemplate.query(finalSql, new Object[]{tenantId}, rs -> {
            UUID transferId = UUID.fromString(rs.getString("id"));
            StockTransferResponse response = responses.stream()
                .filter(r -> r.getId().equals(transferId))
                .findFirst()
                .orElse(null);

            if (response != null) {
                response.setProductName(rs.getString("product_name"));
                response.setProductSku(rs.getString("product_sku"));
                response.setOriginLocationName(rs.getString("origin_name"));
                response.setOriginLocationCode(rs.getString("origin_code"));
                response.setDestinationLocationName(rs.getString("dest_name"));
                response.setDestinationLocationCode(rs.getString("dest_code"));
            }
        });
    }

    // ============================================================
    // Filters Class
    // ============================================================

    public static class StockTransferFilters {
        private UUID productId;
        private UUID variantId;
        private UUID originLocationId;
        private UUID destinationLocationId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private UUID userId;

        // Getters and Setters
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        public UUID getVariantId() { return variantId; }
        public void setVariantId(UUID variantId) { this.variantId = variantId; }
        public UUID getOriginLocationId() { return originLocationId; }
        public void setOriginLocationId(UUID originLocationId) { this.originLocationId = originLocationId; }
        public UUID getDestinationLocationId() { return destinationLocationId; }
        public void setDestinationLocationId(UUID destinationLocationId) { this.destinationLocationId = destinationLocationId; }
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
    }
}
