package com.estoquecentral.inventory.adapter.in;

import com.estoquecentral.inventory.adapter.in.dto.*;
import com.estoquecentral.inventory.application.InventoryService;
import com.estoquecentral.inventory.domain.Inventory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Inventory management")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory for product")
    public ResponseEntity<InventoryDTO> getInventory(@PathVariable UUID productId,
                                                      @RequestParam(required = false) String location) {
        return inventoryService.getInventory(productId, location)
                .map(inv -> ResponseEntity.ok(InventoryDTO.fromEntity(inv)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Add stock")
    public ResponseEntity<InventoryDTO> addStock(@Valid @RequestBody StockMovementRequest request,
                                                  Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        Inventory inventory = inventoryService.addStock(
                request.getProductId(), request.getQuantity(), request.getLocation(),
                request.getReason(), request.getNotes(),
                request.getReferenceType(), request.getReferenceId(), userId
        );
        return ResponseEntity.ok(InventoryDTO.fromEntity(inventory));
    }

    @PostMapping("/remove")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Remove stock")
    public ResponseEntity<InventoryDTO> removeStock(@Valid @RequestBody StockMovementRequest request,
                                                     Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        Inventory inventory = inventoryService.removeStock(
                request.getProductId(), request.getQuantity(), request.getLocation(),
                request.getReason(), request.getNotes(),
                request.getReferenceType(), request.getReferenceId(), userId
        );
        return ResponseEntity.ok(InventoryDTO.fromEntity(inventory));
    }

    @PostMapping("/reserve")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Reserve stock")
    public ResponseEntity<InventoryDTO> reserveStock(@Valid @RequestBody StockMovementRequest request,
                                                      Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        Inventory inventory = inventoryService.reserveStock(
                request.getProductId(), request.getQuantity(), request.getLocation(),
                request.getReferenceType(), request.getReferenceId(), userId
        );
        return ResponseEntity.ok(InventoryDTO.fromEntity(inventory));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products")
    public ResponseEntity<List<InventoryDTO>> getLowStockProducts() {
        List<InventoryDTO> dtos = inventoryService.getLowStockProducts().stream()
                .map(InventoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock products")
    public ResponseEntity<List<InventoryDTO>> getOutOfStockProducts() {
        List<InventoryDTO> dtos = inventoryService.getOutOfStockProducts().stream()
                .map(InventoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get inventory statistics")
    public ResponseEntity<InventoryStatsDTO> getStats() {
        long lowStock = inventoryService.countLowStockProducts();
        long outOfStock = inventoryService.countOutOfStockProducts();
        Double totalValue = inventoryService.getTotalInventoryValue();

        InventoryStatsDTO stats = new InventoryStatsDTO(lowStock, outOfStock, totalValue);
        return ResponseEntity.ok(stats);
    }

    public static class InventoryStatsDTO {
        private long lowStockCount;
        private long outOfStockCount;
        private Double totalValue;

        public InventoryStatsDTO(long lowStockCount, long outOfStockCount, Double totalValue) {
            this.lowStockCount = lowStockCount;
            this.outOfStockCount = outOfStockCount;
            this.totalValue = totalValue;
        }

        public long getLowStockCount() { return lowStockCount; }
        public long getOutOfStockCount() { return outOfStockCount; }
        public Double getTotalValue() { return totalValue; }
    }
}
