package com.estoquecentral.inventory.application;

import com.estoquecentral.inventory.adapter.out.InventoryRepository;
import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.inventory.adapter.out.StockAdjustmentRepository;
import com.estoquecentral.inventory.adapter.out.LocationRepository;
import com.estoquecentral.inventory.adapter.out.StockMovementRepository;
import com.estoquecentral.inventory.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * StockAdjustmentService - Business logic for stock adjustments
 * Story 3.5: Stock Adjustment (Ajuste de Estoque)
 */
@Service
public class StockAdjustmentService {

    private final StockAdjustmentRepository adjustmentRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final StockMovementRepository stockMovementRepository;
    private final AdjustmentNumberGenerator numberGenerator;

    public StockAdjustmentService(
            StockAdjustmentRepository adjustmentRepository,
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            LocationRepository locationRepository,
            StockMovementRepository stockMovementRepository,
            AdjustmentNumberGenerator numberGenerator) {
        this.adjustmentRepository = adjustmentRepository;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.numberGenerator = numberGenerator;
    }

    /**
     * Create stock adjustment (Story 3.5 AC2-AC5)
     * Atomic transaction: validates, updates stock, creates audit movement
     */
    @Transactional
    public StockAdjustment createAdjustment(
            UUID tenantId,
            UUID userId,
            UUID productId,
            UUID variantId,
            UUID stockLocationId,
            AdjustmentType adjustmentType,
            BigDecimal quantity,
            AdjustmentReasonCode reasonCode,
            String reasonDescription,
            LocalDate adjustmentDate) {

        // 1. Validate product exists (AC2)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!product.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Product does not belong to tenant");
        }

        // 2. Validate location exists (AC2)
        Location location = locationRepository.findById(stockLocationId)
                .orElseThrow(() -> new IllegalArgumentException("Stock location not found"));

        if (!location.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Stock location does not belong to tenant");
        }

        // 3. Validate reason description (AC2)
        if (reasonDescription == null || reasonDescription.trim().length() < 10) {
            throw new IllegalArgumentException("Reason description must be at least 10 characters");
        }

        // 4. Find or create inventory record
        Optional<Inventory> inventoryOpt = inventoryRepository
                .findByTenantIdAndProductIdAndLocationId(tenantId, productId, stockLocationId);

        Inventory inventory;
        if (inventoryOpt.isPresent()) {
            inventory = inventoryOpt.get();
        } else {
            // Create new inventory record with zero quantity
            inventory = new Inventory(tenantId, productId, stockLocationId, BigDecimal.ZERO);
            inventory.setCost(BigDecimal.ZERO);
        }

        BigDecimal balanceBefore = inventory.getQuantityAvailable();

        // 5. Validate sufficient stock for DECREASE (AC2)
        if (adjustmentType == AdjustmentType.DECREASE) {
            if (balanceBefore.compareTo(quantity) < 0) {
                throw new IllegalArgumentException(
                        String.format("Insufficient stock. Available: %s, Requested: %s",
                                balanceBefore, quantity));
            }
        }

        // 6. Create adjustment record (AC5)
        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setTenantId(tenantId);
        adjustment.setAdjustmentNumber(numberGenerator.generateAdjustmentNumber(tenantId));
        adjustment.setProductId(productId);
        adjustment.setVariantId(variantId);
        adjustment.setStockLocationId(stockLocationId);
        adjustment.setAdjustmentType(adjustmentType);
        adjustment.setQuantity(quantity);
        adjustment.setReasonCode(reasonCode);
        adjustment.setReasonDescription(reasonDescription.trim());
        adjustment.setAdjustedByUserId(userId);
        adjustment.setAdjustmentDate(adjustmentDate != null ? adjustmentDate : LocalDate.now());
        adjustment.setBalanceBefore(balanceBefore);
        adjustment.setCreatedAt(LocalDateTime.now());

        // 7. Update inventory (AC3)
        if (adjustmentType == AdjustmentType.INCREASE) {
            inventory.addQuantity(quantity);
        } else {
            inventory.removeQuantity(quantity);
        }

        BigDecimal balanceAfter = inventory.getQuantityAvailable();
        adjustment.setBalanceAfter(balanceAfter);

        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);

        StockAdjustment savedAdjustment = adjustmentRepository.save(adjustment);

        // 8. Create stock movement for audit (AC4)
        StockMovement movement = new StockMovement();
        movement.setTenantId(tenantId);
        movement.setProductId(productId);
        movement.setVariantId(variantId);
        movement.setStockLocationId(stockLocationId);
        movement.setType(MovementType.ADJUSTMENT);

        // Quantity is positive for INCREASE, negative for DECREASE
        BigDecimal movementQuantity = adjustmentType == AdjustmentType.INCREASE ?
                quantity : quantity.negate();
        movement.setQuantity(movementQuantity);

        movement.setBalanceBefore(balanceBefore);
        movement.setBalanceAfter(balanceAfter);
        movement.setDocumentId(savedAdjustment.getId());
        movement.setUserId(userId);
        movement.setReason(String.format("%s - %s: %s",
                adjustmentType.name(),
                reasonCode.name(),
                reasonDescription));
        movement.setCreatedAt(LocalDateTime.now());
        stockMovementRepository.save(movement);

        return savedAdjustment;
    }

    /**
     * Get adjustment history with filters (AC6)
     */
    @Transactional(readOnly = true)
    public Page<StockAdjustment> getAdjustmentHistory(
            UUID tenantId,
            UUID productId,
            UUID stockLocationId,
            AdjustmentType adjustmentType,
            AdjustmentReasonCode reasonCode,
            LocalDate adjustmentDateFrom,
            LocalDate adjustmentDateTo,
            UUID userId,
            Pageable pageable) {

        String adjustmentTypeStr = adjustmentType != null ? adjustmentType.name() : null;
        String reasonCodeStr = reasonCode != null ? reasonCode.name() : null;

        // Get paginated data
        List<StockAdjustment> content = adjustmentRepository.search(
                tenantId,
                productId,
                stockLocationId,
                adjustmentTypeStr,
                reasonCodeStr,
                adjustmentDateFrom,
                adjustmentDateTo,
                userId,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        // Get total count
        long total = adjustmentRepository.countSearch(
                tenantId,
                productId,
                stockLocationId,
                adjustmentTypeStr,
                reasonCodeStr,
                adjustmentDateFrom,
                adjustmentDateTo,
                userId
        );

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Get adjustment by ID (AC7)
     */
    @Transactional(readOnly = true)
    public Optional<StockAdjustment> getAdjustmentById(UUID tenantId, UUID adjustmentId) {
        Optional<StockAdjustment> adjustmentOpt = adjustmentRepository.findById(adjustmentId);

        if (adjustmentOpt.isEmpty() || !adjustmentOpt.get().getTenantId().equals(tenantId)) {
            return Optional.empty();
        }

        return adjustmentOpt;
    }

    /**
     * Get products with frequent adjustments (AC11)
     * Returns products with 3+ adjustments in the last 30 days
     */
    @Transactional(readOnly = true)
    public List<FrequentAdjustmentData> getFrequentAdjustments(UUID tenantId, int daysBack) {
        LocalDate dateFrom = LocalDate.now().minusDays(daysBack);

        List<Map<String, Object>> rawData = adjustmentRepository.findFrequentAdjustmentsRaw(tenantId, dateFrom);

        return rawData.stream().map(row -> {
            UUID productId = (UUID) row.get("product_id");
            UUID locationId = (UUID) row.get("stock_location_id");
            Long totalAdjustments = ((Number) row.get("total_adjustments")).longValue();
            BigDecimal totalIncrease = (BigDecimal) row.get("total_increase");
            BigDecimal totalDecrease = (BigDecimal) row.get("total_decrease");

            return new FrequentAdjustmentData(
                productId,
                locationId,
                totalAdjustments,
                totalIncrease != null ? totalIncrease : BigDecimal.ZERO,
                totalDecrease != null ? totalDecrease : BigDecimal.ZERO
            );
        }).collect(Collectors.toList());
    }

    /**
     * Data class for frequent adjustment results
     */
    public record FrequentAdjustmentData(
            UUID productId,
            UUID stockLocationId,
            long totalAdjustments,
            BigDecimal totalIncrease,
            BigDecimal totalDecrease
    ) {}
}
