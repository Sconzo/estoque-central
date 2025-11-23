package com.estoquecentral.inventory.adapter.in.web;

import com.estoquecentral.common.CurrentUser;
import com.estoquecentral.inventory.adapter.in.dto.FrequentAdjustmentDTO;
import com.estoquecentral.inventory.adapter.in.dto.StockAdjustmentRequestDTO;
import com.estoquecentral.inventory.adapter.in.dto.StockAdjustmentResponseDTO;
import com.estoquecentral.inventory.adapter.out.ProductRepository;
import com.estoquecentral.inventory.adapter.out.StockLocationRepository;
import com.estoquecentral.inventory.application.StockAdjustmentService;
import com.estoquecentral.inventory.domain.*;
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
import java.util.stream.Collectors;

/**
 * StockAdjustmentController - REST endpoints for stock adjustments
 * Story 3.5: Stock Adjustment (Ajuste de Estoque)
 */
@RestController
@RequestMapping("/api/stock/adjustments")
public class StockAdjustmentController {

    private final StockAdjustmentService adjustmentService;
    private final ProductRepository productRepository;
    private final StockLocationRepository stockLocationRepository;

    public StockAdjustmentController(
            StockAdjustmentService adjustmentService,
            ProductRepository productRepository,
            StockLocationRepository stockLocationRepository) {
        this.adjustmentService = adjustmentService;
        this.productRepository = productRepository;
        this.stockLocationRepository = stockLocationRepository;
    }

    /**
     * Create stock adjustment (Story 3.5 AC2)
     * POST /api/stock/adjustments
     */
    @PostMapping
    public ResponseEntity<?> createAdjustment(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody StockAdjustmentRequestDTO request) {

        try {
            StockAdjustment adjustment = adjustmentService.createAdjustment(
                    currentUser.getTenantId(),
                    currentUser.getUserId(),
                    request.getProductId(),
                    request.getVariantId(),
                    request.getStockLocationId(),
                    request.getAdjustmentType(),
                    request.getQuantity(),
                    request.getReasonCode(),
                    request.getReasonDescription(),
                    request.getAdjustmentDate()
            );

            StockAdjustmentResponseDTO response = mapToResponseDTO(adjustment, currentUser.getTenantId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get adjustment history with filters (Story 3.5 AC6)
     * GET /api/stock/adjustments
     */
    @GetMapping
    public ResponseEntity<Page<StockAdjustmentResponseDTO>> getAdjustmentHistory(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID stockLocationId,
            @RequestParam(required = false) AdjustmentType adjustmentType,
            @RequestParam(required = false) AdjustmentReasonCode reasonCode,
            @RequestParam(required = false) LocalDate adjustmentDateFrom,
            @RequestParam(required = false) LocalDate adjustmentDateTo,
            @RequestParam(required = false) UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<StockAdjustment> adjustments = adjustmentService.getAdjustmentHistory(
                currentUser.getTenantId(),
                productId,
                stockLocationId,
                adjustmentType,
                reasonCode,
                adjustmentDateFrom,
                adjustmentDateTo,
                userId,
                pageable
        );

        Page<StockAdjustmentResponseDTO> response = adjustments.map(a -> mapToResponseDTO(a, currentUser.getTenantId()));
        return ResponseEntity.ok(response);
    }

    /**
     * Get adjustment by ID (Story 3.5 AC7)
     * GET /api/stock/adjustments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdjustmentById(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID id) {

        Optional<StockAdjustment> adjustmentOpt = adjustmentService.getAdjustmentById(
                currentUser.getTenantId(),
                id
        );

        if (adjustmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StockAdjustmentResponseDTO response = mapToResponseDTO(adjustmentOpt.get(), currentUser.getTenantId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get products with frequent adjustments (Story 3.5 AC11)
     * GET /api/stock/adjustments/frequent-adjustments
     */
    @GetMapping("/frequent-adjustments")
    public ResponseEntity<List<FrequentAdjustmentDTO>> getFrequentAdjustments(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(defaultValue = "30") int daysBack) {

        List<StockAdjustmentService.FrequentAdjustmentData> frequentAdjustments =
                adjustmentService.getFrequentAdjustments(currentUser.getTenantId(), daysBack);

        // Load product and location names
        List<FrequentAdjustmentDTO> response = frequentAdjustments.stream().map(data -> {
            Product product = productRepository.findById(data.productId()).orElse(null);
            StockLocation location = stockLocationRepository.findById(data.stockLocationId()).orElse(null);

            return new FrequentAdjustmentDTO(
                    data.productId(),
                    product != null ? product.getName() : null,
                    product != null ? product.getSku() : null,
                    data.stockLocationId(),
                    location != null ? location.getName() : null,
                    data.totalAdjustments(),
                    data.totalIncrease(),
                    data.totalDecrease()
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Map StockAdjustment entity to response DTO
     */
    private StockAdjustmentResponseDTO mapToResponseDTO(StockAdjustment adjustment, UUID tenantId) {
        // Load related data
        Product product = adjustment.getProductId() != null ?
                productRepository.findById(adjustment.getProductId()).orElse(null) : null;
        StockLocation location = stockLocationRepository.findById(adjustment.getStockLocationId()).orElse(null);

        StockAdjustmentResponseDTO dto = new StockAdjustmentResponseDTO();
        dto.setId(adjustment.getId());
        dto.setAdjustmentNumber(adjustment.getAdjustmentNumber());
        dto.setProductId(adjustment.getProductId());
        dto.setProductName(product != null ? product.getName() : null);
        dto.setProductSku(product != null ? product.getSku() : null);
        dto.setVariantId(adjustment.getVariantId());
        dto.setVariantName(null); // TODO: Load from variant if needed
        dto.setStockLocationId(adjustment.getStockLocationId());
        dto.setStockLocationName(location != null ? location.getName() : null);
        dto.setAdjustmentType(adjustment.getAdjustmentType());
        dto.setQuantity(adjustment.getQuantity());
        dto.setReasonCode(adjustment.getReasonCode());
        dto.setReasonDescription(adjustment.getReasonDescription());
        dto.setAdjustedByUserId(adjustment.getAdjustedByUserId());
        dto.setAdjustedByUserName(null); // TODO: Load from user service if needed
        dto.setAdjustmentDate(adjustment.getAdjustmentDate());
        dto.setBalanceBefore(adjustment.getBalanceBefore());
        dto.setBalanceAfter(adjustment.getBalanceAfter());
        dto.setCreatedAt(adjustment.getCreatedAt());

        return dto;
    }
}
