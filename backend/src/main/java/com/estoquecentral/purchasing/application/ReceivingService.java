package com.estoquecentral.purchasing.application;

import com.estoquecentral.inventory.adapter.out.InventoryRepository;
import com.estoquecentral.inventory.domain.Inventory;
import com.estoquecentral.inventory.domain.MovementType;
import com.estoquecentral.inventory.domain.StockMovement;
import com.estoquecentral.inventory.adapter.out.StockMovementRepository;
import com.estoquecentral.purchasing.adapter.in.dto.ProcessReceivingRequest;
import com.estoquecentral.purchasing.adapter.out.*;
import com.estoquecentral.purchasing.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ReceivingService - Business logic for receiving processing
 * Story 3.4: Receiving Processing and Weighted Average Cost Update
 */
@Service
public class ReceivingService {

    private final ReceivingRepository receivingRepository;
    private final ReceivingItemRepository receivingItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ReceivingNumberGenerator numberGenerator;
    private final WeightedAverageCostCalculator costCalculator;

    public ReceivingService(
            ReceivingRepository receivingRepository,
            ReceivingItemRepository receivingItemRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            ReceivingNumberGenerator numberGenerator,
            WeightedAverageCostCalculator costCalculator) {
        this.receivingRepository = receivingRepository;
        this.receivingItemRepository = receivingItemRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.numberGenerator = numberGenerator;
        this.costCalculator = costCalculator;
    }

    /**
     * Process receiving transaction (Story 3.4 AC2-AC6)
     * Atomic transaction: stock + cost + movements + PO status
     */
    @Transactional
    public Receiving processReceiving(
            UUID tenantId,
            UUID userId,
            ProcessReceivingRequest request) {

        // 1. Validate purchase order (AC2)
        PurchaseOrder po = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));

        if (!po.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Purchase order does not belong to tenant");
        }

        if (po.getStatus() != PurchaseOrderStatus.SENT_TO_SUPPLIER &&
            po.getStatus() != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new IllegalStateException("Purchase order not available for receiving. Status: " + po.getStatus());
        }

        // 2. Load all PO items
        List<PurchaseOrderItem> poItems = purchaseOrderItemRepository.findByPurchaseOrderId(po.getId());
        Map<UUID, PurchaseOrderItem> poItemsMap = new HashMap<>();
        for (PurchaseOrderItem item : poItems) {
            poItemsMap.put(item.getId(), item);
        }

        // 3. Create receiving record (AC7)
        Receiving receiving = new Receiving();
        receiving.setTenantId(tenantId);
        receiving.setReceivingNumber(numberGenerator.generateReceivingNumber(tenantId));
        receiving.setPurchaseOrderId(po.getId());
        receiving.setStockLocationId(po.getLocationId());
        receiving.setReceivingDate(request.getReceivingDate() != null ? request.getReceivingDate() : LocalDate.now());
        receiving.setReceivedByUserId(userId);
        receiving.setNotes(request.getNotes());
        receiving.setStatus(ReceivingStatus.COMPLETED);
        receiving.setCreatedAt(LocalDateTime.now());

        Receiving savedReceiving = receivingRepository.save(receiving);

        // 4. Process each item (AC3, AC4, AC5, AC6)
        for (ProcessReceivingRequest.ReceivingItemRequest itemRequest : request.getItems()) {
            PurchaseOrderItem poItem = poItemsMap.get(itemRequest.getPurchaseOrderItemId());

            if (poItem == null) {
                throw new IllegalArgumentException("Item " + itemRequest.getPurchaseOrderItemId() + " does not belong to this purchase order");
            }

            // Validate quantity (AC2)
            BigDecimal quantityReceived = poItem.getQuantityReceived() != null ? poItem.getQuantityReceived() : BigDecimal.ZERO;
            BigDecimal quantityPending = poItem.getQuantityOrdered().subtract(quantityReceived);

            if (itemRequest.getQuantityReceived().compareTo(quantityPending) > 0) {
                throw new IllegalArgumentException(
                        String.format("Quantity received (%s) exceeds pending quantity (%s) for item %s",
                                itemRequest.getQuantityReceived(), quantityPending, poItem.getProductSku()));
            }

            // Find or create inventory record (AC3)
            Optional<Inventory> inventoryOpt = inventoryRepository
                    .findByTenantIdAndProductIdAndLocationId(tenantId, poItem.getProductId(), po.getLocationId());

            Inventory inventory;
            BigDecimal currentQty;
            BigDecimal currentCost;

            if (inventoryOpt.isPresent()) {
                inventory = inventoryOpt.get();
                currentQty = inventory.getQuantityAvailable();
                currentCost = inventory.getCost() != null ? inventory.getCost() : BigDecimal.ZERO;
            } else {
                // Create new inventory record
                inventory = new Inventory(tenantId, poItem.getProductId(), po.getLocationId(), BigDecimal.ZERO);
                inventory.setCost(BigDecimal.ZERO);
                currentQty = BigDecimal.ZERO;
                currentCost = BigDecimal.ZERO;
            }

            // Calculate new weighted average cost (AC4)
            BigDecimal newCost = costCalculator.calculateNewCost(
                    currentQty,
                    currentCost,
                    itemRequest.getQuantityReceived(),
                    poItem.getUnitCost()
            );

            // Update inventory (AC3)
            BigDecimal balanceBefore = inventory.getQuantityAvailable();
            inventory.addQuantity(itemRequest.getQuantityReceived());
            inventory.setCost(newCost);
            inventory.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);

            // Create receiving item
            ReceivingItem receivingItem = new ReceivingItem();
            receivingItem.setReceivingId(savedReceiving.getId());
            receivingItem.setPurchaseOrderItemId(poItem.getId());
            receivingItem.setProductId(poItem.getProductId());
            receivingItem.setVariantId(poItem.getProductVariantId());
            receivingItem.setQuantityReceived(itemRequest.getQuantityReceived());
            receivingItem.setUnitCost(poItem.getUnitCost());
            receivingItem.setNewWeightedAverageCost(newCost);
            receivingItem.setNotes(itemRequest.getNotes());
            receivingItemRepository.save(receivingItem);

            // Create stock movement (AC5)
            StockMovement movement = new StockMovement();
            movement.setTenantId(tenantId);
            movement.setProductId(poItem.getProductId());
            movement.setVariantId(poItem.getProductVariantId());
            movement.setStockLocationId(po.getLocationId());
            movement.setType(MovementType.PURCHASE);
            movement.setQuantity(itemRequest.getQuantityReceived());
            movement.setBalanceBefore(balanceBefore);
            movement.setBalanceAfter(inventory.getQuantityAvailable());
            movement.setDocumentId(savedReceiving.getId());
            movement.setUserId(userId);
            movement.setReason("Recebimento OC " + po.getPoNumber());
            movement.setReason(itemRequest.getNotes());
            movement.setCreatedAt(LocalDateTime.now());
            stockMovementRepository.save(movement);

            // Update PO item quantity received (AC6)
            poItem.setQuantityReceived(quantityReceived.add(itemRequest.getQuantityReceived()));
            purchaseOrderItemRepository.save(poItem);
        }

        // 5. Update purchase order status (AC6)
        boolean allItemsFullyReceived = poItems.stream()
                .allMatch(item -> {
                    BigDecimal received = item.getQuantityReceived() != null ? item.getQuantityReceived() : BigDecimal.ZERO;
                    return received.compareTo(item.getQuantityOrdered()) >= 0;
                });

        if (allItemsFullyReceived) {
            po.setStatus(PurchaseOrderStatus.COMPLETED);
        } else {
            po.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }
        po.setUpdatedAt(LocalDateTime.now());
        po.setUpdatedBy(userId);
        purchaseOrderRepository.save(po);

        return savedReceiving;
    }

    /**
     * Get receiving history with filters (AC9)
     */
    @Transactional(readOnly = true)
    public Page<Receiving> getReceivingHistory(
            UUID tenantId,
            UUID purchaseOrderId,
            UUID stockLocationId,
            LocalDate receivingDateFrom,
            LocalDate receivingDateTo,
            String status,
            Pageable pageable) {

        List<Receiving> content = receivingRepository.search(
                tenantId,
                purchaseOrderId,
                stockLocationId,
                receivingDateFrom,
                receivingDateTo,
                status,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        long total = receivingRepository.countSearch(
                tenantId,
                purchaseOrderId,
                stockLocationId,
                receivingDateFrom,
                receivingDateTo,
                status
        );

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Get receiving by ID with items (AC10)
     */
    @Transactional(readOnly = true)
    public Optional<ReceivingWithItems> getReceivingById(UUID tenantId, UUID receivingId) {
        Optional<Receiving> receivingOpt = receivingRepository.findById(receivingId);

        if (receivingOpt.isEmpty() || !receivingOpt.get().getTenantId().equals(tenantId)) {
            return Optional.empty();
        }

        Receiving receiving = receivingOpt.get();
        List<ReceivingItem> items = receivingItemRepository.findByReceivingId(receivingId);

        return Optional.of(new ReceivingWithItems(receiving, items));
    }

    /**
     * Data class for receiving with items
     */
    public record ReceivingWithItems(
            Receiving receiving,
            List<ReceivingItem> items
    ) {}
}
