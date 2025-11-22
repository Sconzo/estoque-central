package com.estoquecentral.purchasing.adapter.in.web;

import com.estoquecentral.purchasing.adapter.in.dto.CreateSupplierRequest;
import com.estoquecentral.purchasing.adapter.in.dto.SupplierResponse;
import com.estoquecentral.purchasing.adapter.in.dto.UpdateSupplierRequest;
import com.estoquecentral.purchasing.application.SupplierService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * SupplierController - REST API for supplier management
 * Story 3.1: Supplier Management
 *
 * Endpoints:
 * - POST /api/suppliers - Create supplier
 * - GET /api/suppliers - List suppliers with pagination and filters
 * - GET /api/suppliers/{id} - Get supplier by ID
 * - PUT /api/suppliers/{id} - Update supplier
 * - DELETE /api/suppliers/{id} - Soft delete supplier (mark as inactive)
 * - POST /api/suppliers/{id}/activate - Activate supplier
 * - GET /api/suppliers/preferred - Get preferred suppliers
 */
@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * Create a new supplier
     * POST /api/suppliers
     */
    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId,
            @Valid @RequestBody CreateSupplierRequest request
    ) {
        try {
            SupplierResponse response = supplierService.createSupplier(tenantId, request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Validation errors or duplicates
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update existing supplier
     * PUT /api/suppliers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplierRequest request
    ) {
        try {
            SupplierResponse response = supplierService.updateSupplier(tenantId, id, request, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get supplier by ID
     * GET /api/suppliers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID id
    ) {
        try {
            SupplierResponse response = supplierService.getSupplierById(tenantId, id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * List suppliers with pagination and filters
     * GET /api/suppliers?search=termo&status=ACTIVE&ativo=true&page=0&size=20
     *
     * Query parameters:
     * - search: Search term (searches in company name, trade name, CNPJ)
     * - status: Filter by status (ACTIVE, INACTIVE, BLOCKED, PENDING_APPROVAL)
     * - ativo: Filter by ativo flag (true/false)
     * - page: Page number (default: 0)
     * - size: Page size (default: 20, max: 100)
     * - sort: Sort field (default: companyName)
     */
    @GetMapping
    public ResponseEntity<Page<SupplierResponse>> listSuppliers(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "companyName") String sort
    ) {
        // Limit max page size to 100
        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));

        Page<SupplierResponse> suppliers;
        if (search != null || status != null || ativo != null) {
            // Use search with filters
            suppliers = supplierService.searchSuppliers(tenantId, search, status, ativo, pageable);
        } else {
            // Simple pagination without filters
            suppliers = supplierService.getSuppliers(tenantId, pageable);
        }

        return ResponseEntity.ok(suppliers);
    }

    /**
     * Soft delete supplier (mark as inactive)
     * DELETE /api/suppliers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId,
            @PathVariable UUID id
    ) {
        try {
            supplierService.deleteSupplier(tenantId, id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Activate supplier (mark as active)
     * POST /api/suppliers/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<SupplierResponse> activateSupplier(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestHeader("X-User-ID") UUID userId,
            @PathVariable UUID id
    ) {
        try {
            SupplierResponse response = supplierService.activateSupplier(tenantId, id, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all preferred suppliers
     * GET /api/suppliers/preferred
     */
    @GetMapping("/preferred")
    public ResponseEntity<List<SupplierResponse>> getPreferredSuppliers(
            @RequestHeader("X-Tenant-ID") UUID tenantId
    ) {
        List<SupplierResponse> suppliers = supplierService.getPreferredSuppliers(tenantId);
        return ResponseEntity.ok(suppliers);
    }
}
