package com.estoquecentral.purchasing.adapter.in.web;

import com.estoquecentral.common.CurrentUser;
import com.estoquecentral.purchasing.adapter.in.dto.ProcessReceivingRequest;
import com.estoquecentral.purchasing.adapter.in.dto.ReceivingItemResponseDTO;
import com.estoquecentral.purchasing.adapter.in.dto.ReceivingResponseDTO;
import com.estoquecentral.purchasing.adapter.out.PurchaseOrderRepository;
import com.estoquecentral.purchasing.adapter.out.ReceivingItemRepository;
import com.estoquecentral.purchasing.application.ReceivingService;
import com.estoquecentral.purchasing.domain.PurchaseOrder;
import com.estoquecentral.purchasing.domain.Receiving;
import com.estoquecentral.purchasing.domain.ReceivingItem;
import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.inventory.adapter.out.LocationRepository;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.inventory.domain.Location;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

/**
 * ReceivingController - REST endpoints for receiving management
 * Story 3.4: Receiving Processing and Weighted Average Cost Update (AC8)
 */
@RestController
@RequestMapping("/api/receivings")
public class ReceivingController {

    private final ReceivingService receivingService;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final ReceivingItemRepository receivingItemRepository;

    public ReceivingController(
            ReceivingService receivingService,
            PurchaseOrderRepository purchaseOrderRepository,
            ProductRepository productRepository,
            LocationRepository locationRepository,
            ReceivingItemRepository receivingItemRepository) {
        this.receivingService = receivingService;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
        this.receivingItemRepository = receivingItemRepository;
    }

    /**
     * Process receiving transaction (Story 3.4 AC8)
     * POST /api/receivings
     */
    @PostMapping
    public ResponseEntity<?> processReceiving(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody ProcessReceivingRequest request) {

        try {
            Receiving receiving = receivingService.processReceiving(
                    currentUser.getTenantId(),
                    currentUser.getUserId(),
                    request
            );

            ReceivingResponseDTO response = mapToResponseDTO(receiving, currentUser.getTenantId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation error",
                    "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Conflict",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get receiving history with filters (Story 3.4 AC9)
     * GET /api/receivings
     */
    @GetMapping
    public ResponseEntity<Page<ReceivingResponseDTO>> getReceivingHistory(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(required = false) UUID purchaseOrderId,
            @RequestParam(required = false) UUID stockLocationId,
            @RequestParam(required = false) LocalDate receivingDateFrom,
            @RequestParam(required = false) LocalDate receivingDateTo,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Receiving> receivings = receivingService.getReceivingHistory(
                currentUser.getTenantId(),
                purchaseOrderId,
                stockLocationId,
                receivingDateFrom,
                receivingDateTo,
                status,
                pageable
        );

        Page<ReceivingResponseDTO> response = receivings.map(r -> mapToResponseDTO(r, currentUser.getTenantId()));
        return ResponseEntity.ok(response);
    }

    /**
     * Get receiving by ID with items (Story 3.4 AC10)
     * GET /api/receivings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReceivingById(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID id) {

        Optional<ReceivingService.ReceivingWithItems> result =
                receivingService.getReceivingById(currentUser.getTenantId(), id);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ReceivingResponseDTO response = mapToResponseDTO(
                result.get().receiving(),
                result.get().items(),
                currentUser.getTenantId()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Map Receiving entity to response DTO (without items)
     */
    private ReceivingResponseDTO mapToResponseDTO(Receiving receiving, UUID tenantId) {
        // Load related data
        PurchaseOrder po = purchaseOrderRepository.findById(receiving.getPurchaseOrderId()).orElse(null);
        Location location = locationRepository.findById(receiving.getStockLocationId()).orElse(null);

        ReceivingResponseDTO dto = new ReceivingResponseDTO();
        dto.setId(receiving.getId());
        dto.setReceivingNumber(receiving.getReceivingNumber());
        dto.setPurchaseOrderId(receiving.getPurchaseOrderId());
        dto.setPoNumber(po != null ? po.getPoNumber() : null);
        dto.setStockLocationId(receiving.getStockLocationId());
        dto.setLocationName(location != null ? location.getName() : null);
        dto.setReceivingDate(receiving.getReceivingDate());
        dto.setReceivedByUserId(receiving.getReceivedByUserId());
        dto.setReceivedByUserName(null); // TODO: Load from user service if needed
        dto.setNotes(receiving.getNotes());
        dto.setStatus(receiving.getStatus().name());
        dto.setCreatedAt(receiving.getCreatedAt());
        dto.setItems(null); // Items not included in list view

        return dto;
    }

    /**
     * Map Receiving entity to response DTO (with items)
     */
    private ReceivingResponseDTO mapToResponseDTO(
            Receiving receiving,
            List<ReceivingItem> items,
            UUID tenantId) {

        ReceivingResponseDTO dto = mapToResponseDTO(receiving, tenantId);

        // Map items
        List<UUID> productIds = items.stream()
                .map(ReceivingItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<UUID, Product> productsMap = StreamSupport.stream(productRepository.findAllById(productIds).spliterator(), false)
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<ReceivingItemResponseDTO> itemDTOs = items.stream()
                .map(item -> {
                    Product product = productsMap.get(item.getProductId());

                    ReceivingItemResponseDTO itemDTO = new ReceivingItemResponseDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setPurchaseOrderItemId(item.getPurchaseOrderItemId());
                    itemDTO.setProductId(item.getProductId());
                    itemDTO.setProductName(product != null ? product.getName() : null);
                    itemDTO.setSku(product != null ? product.getSku() : null);
                    itemDTO.setVariantId(item.getVariantId());
                    itemDTO.setVariantName(null); // TODO: Load variant name if needed
                    itemDTO.setQuantityReceived(item.getQuantityReceived());
                    itemDTO.setUnitCost(item.getUnitCost());
                    itemDTO.setNewWeightedAverageCost(item.getNewWeightedAverageCost());
                    itemDTO.setNotes(item.getNotes());

                    return itemDTO;
                })
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);

        return dto;
    }
}
