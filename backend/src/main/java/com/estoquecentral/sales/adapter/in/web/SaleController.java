package com.estoquecentral.sales.adapter.in.web;

import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.common.CurrentUser;
import com.estoquecentral.sales.adapter.in.dto.CancelSaleRequestDTO;
import com.estoquecentral.sales.adapter.in.dto.SaleRequestDTO;
import com.estoquecentral.sales.adapter.in.dto.SaleResponseDTO;
import com.estoquecentral.sales.adapter.out.SaleRepository;
import com.estoquecentral.sales.application.SaleService;
import com.estoquecentral.sales.domain.NfceStatus;
import com.estoquecentral.sales.domain.Sale;
import com.estoquecentral.sales.domain.SaleItem;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SaleController - REST API for sale processing
 * Story 4.3: NFCe Emission and Stock Decrease
 * Story 4.4: NFCe Retry and Cancellation
 *
 * Endpoints:
 * - POST /api/sales - Process a sale (validate stock, create sale, emit NFCe, decrease stock)
 * - GET /api/sales/pending-fiscal - List sales with PENDING or FAILED NFCe status
 * - POST /api/sales/{id}/retry - Retry NFCe emission for a failed sale
 * - POST /api/sales/{id}/cancel-with-refund - Cancel a sale with refund and stock restoration
 */
@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public SaleController(
            SaleService saleService,
            SaleRepository saleRepository,
            ProductRepository productRepository) {
        this.saleService = saleService;
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    /**
     * Process sale transaction
     * POST /api/sales
     *
     * Story 4.3 - Process sale with NFCe emission and stock decrease
     * AC1: Validate stock availability before processing
     * AC2: Create sale and decrease stock
     * AC3: Emit NFCe (or mark as PENDING on failure)
     * AC4: Create fiscal event audit trail
     * AC5: Create stock movement records
     *
     * @param currentUser Authenticated user (provides tenantId and userId)
     * @param request Sale request DTO with items
     * @return ResponseEntity with SaleResponseDTO (HTTP 201) or error
     */
    @PostMapping
    public ResponseEntity<?> processSale(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody SaleRequestDTO request) {

        try {
            // Map DTO to service request
            SaleService.SaleRequest saleRequest = new SaleService.SaleRequest(
                    currentUser.getTenantId(),
                    request.getCustomerId(),
                    request.getStockLocationId(),
                    request.getPaymentMethod(),
                    request.getPaymentAmountReceived(),
                    null, // discount not in request DTO yet
                    mapItemsToServiceRequest(request.getItems()),
                    currentUser.getUserId()
            );

            // Process sale
            Sale sale = saleService.processSale(saleRequest);

            // Map entity to response DTO
            SaleResponseDTO response = mapToResponseDTO(sale);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (SaleService.InsufficientStockException e) {
            // AC1: Return HTTP 409 CONFLICT when stock is insufficient
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Insufficient stock",
                    "message", e.getMessage()
            ));

        } catch (IllegalArgumentException e) {
            // Validation errors
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation error",
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            // AC: Return HTTP 500 for unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Internal server error",
                    "message", "An unexpected error occurred while processing the sale"
            ));
        }
    }

    /**
     * Get pending fiscal sales
     * GET /api/sales/pending-fiscal
     *
     * Story 4.4 - AC7: List sales with PENDING or FAILED NFCe status
     *
     * @param currentUser Authenticated user (provides tenantId)
     * @param status Optional filter for specific status (PENDING or FAILED)
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return ResponseEntity with Page of SaleResponseDTO
     */
    @GetMapping("/pending-fiscal")
    public ResponseEntity<?> getPendingFiscalSales(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(required = false) NfceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            List<Sale> content;
            long total;

            if (status != null) {
                // Filter by specific status
                if (status != NfceStatus.PENDING && status != NfceStatus.FAILED) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Invalid status",
                            "message", "Status must be PENDING or FAILED"
                    ));
                }
                content = saleRepository.findByTenantIdAndNfceStatus(
                        currentUser.getTenantId(),
                        status.name(),
                        pageable.getPageSize(),
                        pageable.getOffset()
                );
                total = saleRepository.countByTenantIdAndNfceStatus(
                        currentUser.getTenantId(),
                        status.name()
                );
            } else {
                // Get all PENDING or FAILED sales
                content = saleRepository.findByTenantIdAndNfceStatusPendingOrFailed(
                        currentUser.getTenantId(),
                        pageable.getPageSize(),
                        pageable.getOffset()
                );
                total = saleRepository.countByTenantIdAndNfceStatusPendingOrFailed(
                        currentUser.getTenantId()
                );
            }

            // Build Page and map to response DTOs
            Page<Sale> sales = new PageImpl<>(content, pageable, total);
            Page<SaleResponseDTO> responsePage = sales.map(this::mapToResponseDTO);

            return ResponseEntity.ok(responsePage);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Internal server error",
                    "message", "An unexpected error occurred while fetching pending fiscal sales"
            ));
        }
    }

    /**
     * Retry NFCe emission for a failed sale
     * POST /api/sales/{id}/retry
     *
     * Story 4.4 - AC8: Retry NFCe emission
     *
     * @param currentUser Authenticated user (provides tenantId and userId)
     * @param id Sale ID
     * @return ResponseEntity with SaleResponseDTO (HTTP 200) or error
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<?> retrySale(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID id) {

        try {
            Sale sale = saleService.retrySale(
                    currentUser.getTenantId(),
                    id,
                    currentUser.getUserId()
            );

            SaleResponseDTO response = mapToResponseDTO(sale);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Validation errors (e.g., sale not found, invalid status)
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation error",
                    "message", e.getMessage()
            ));

        } catch (IllegalStateException e) {
            // State errors (e.g., sale not in PENDING/FAILED status)
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid state",
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Internal server error",
                    "message", "An unexpected error occurred while retrying the sale"
            ));
        }
    }

    /**
     * Cancel a sale with refund and stock restoration
     * POST /api/sales/{id}/cancel-with-refund
     *
     * Story 4.4 - AC9: Cancel sale with refund
     *
     * @param currentUser Authenticated user (provides tenantId and userId)
     * @param id Sale ID
     * @param request Cancel request with justification
     * @return ResponseEntity with SaleResponseDTO (HTTP 200) or error
     */
    @PostMapping("/{id}/cancel-with-refund")
    public ResponseEntity<?> cancelSaleWithRefund(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody CancelSaleRequestDTO request) {

        try {
            Sale sale = saleService.cancelSaleWithRefund(
                    currentUser.getTenantId(),
                    id,
                    currentUser.getUserId(),
                    request.getJustification()
            );

            SaleResponseDTO response = mapToResponseDTO(sale);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Validation errors (e.g., sale not found, invalid justification)
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation error",
                    "message", e.getMessage()
            ));

        } catch (IllegalStateException e) {
            // State errors (e.g., sale already cancelled)
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid state",
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Internal server error",
                    "message", "An unexpected error occurred while cancelling the sale"
            ));
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Map request DTO items to service request items
     */
    private List<SaleService.ItemRequest> mapItemsToServiceRequest(List<SaleRequestDTO.ItemRequestDTO> items) {
        List<SaleService.ItemRequest> serviceItems = new ArrayList<>();

        for (SaleRequestDTO.ItemRequestDTO item : items) {
            serviceItems.add(new SaleService.ItemRequest(
                    item.getProductId(),
                    item.getVariantId(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    null // discount not in request DTO yet
            ));
        }

        return serviceItems;
    }

    /**
     * Map Sale entity to response DTO
     * Note: This is a simplified mapping. For a complete implementation,
     * we would also load and map SaleItems.
     */
    private SaleResponseDTO mapToResponseDTO(Sale sale) {
        SaleResponseDTO response = new SaleResponseDTO();
        response.setId(sale.getId());
        response.setSaleNumber(sale.getSaleNumber());
        response.setCustomerId(sale.getCustomerId());
        response.setTotalAmount(sale.getTotalAmount());
        response.setChangeAmount(sale.getChangeAmount());
        response.setNfceStatus(sale.getNfceStatus());
        response.setNfceKey(sale.getNfceKey());
        response.setSaleDate(sale.getSaleDate());

        // TODO: Load and map SaleItems if needed
        // This would require injecting SaleItemRepository and loading items by saleId
        response.setItems(new ArrayList<>());

        return response;
    }
}
