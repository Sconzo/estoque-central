package com.estoquecentral.catalog.adapter.in;

import com.estoquecentral.catalog.adapter.in.dto.AddBomComponentRequest;
import com.estoquecentral.catalog.adapter.in.dto.BomComponentResponse;
import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.application.composite.CompositeProductService;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductComponent;
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

/**
 * CompositeProductController - REST API for composite products/kits (BOM)
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/products/{id}/bom - Add component to BOM</li>
 *   <li>GET /api/products/{id}/bom - List all components</li>
 *   <li>PUT /api/products/{id}/bom/{componentId} - Update component quantity</li>
 *   <li>DELETE /api/products/{id}/bom/{componentId} - Remove component</li>
 *   <li>GET /api/products/{id}/available-stock - Calculate available stock (virtual BOM)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Composite Products", description = "Manage composite products/kits (BOM)")
public class CompositeProductController {

    private final CompositeProductService compositeService;
    private final ProductRepository productRepository;

    @Autowired
    public CompositeProductController(CompositeProductService compositeService,
                                     ProductRepository productRepository) {
        this.compositeService = compositeService;
        this.productRepository = productRepository;
    }

    /**
     * Adds component to composite product BOM
     *
     * POST /api/products/{id}/bom
     */
    @PostMapping("/{id}/bom")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Add component to BOM",
               description = "Adds a component product to the Bill of Materials")
    public ResponseEntity<BomComponentResponse> addComponent(
            @PathVariable UUID id,
            @Valid @RequestBody AddBomComponentRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        ProductComponent component = compositeService.addComponent(
                id,
                request.getComponentProductId(),
                request.getQuantityRequired(),
                userId
        );

        // Load component product for response
        Product componentProduct = productRepository.findById(component.getComponentProductId())
                .orElseThrow();

        BomComponentResponse response = BomComponentResponse.from(component, componentProduct);

        return ResponseEntity.ok(response);
    }

    /**
     * Lists all components of a composite product
     *
     * GET /api/products/{id}/bom
     */
    @GetMapping("/{id}/bom")
    @Operation(summary = "List BOM components",
               description = "Returns all components of a composite product")
    public ResponseEntity<List<BomComponentResponse>> listComponents(@PathVariable UUID id) {

        List<ProductComponent> components = compositeService.listComponents(id);

        List<BomComponentResponse> responses = components.stream()
                .map(comp -> {
                    Product componentProduct = productRepository.findById(comp.getComponentProductId())
                            .orElseThrow();
                    return BomComponentResponse.from(comp, componentProduct);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Updates component quantity
     *
     * PUT /api/products/{id}/bom/{componentId}
     */
    @PutMapping("/{id}/bom/{componentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Update component quantity",
               description = "Updates the quantity required for a component")
    public ResponseEntity<BomComponentResponse> updateComponentQuantity(
            @PathVariable UUID id,
            @PathVariable UUID componentId,
            @Valid @RequestBody AddBomComponentRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        ProductComponent component = compositeService.updateComponentQuantity(
                id,
                componentId,
                request.getQuantityRequired(),
                userId
        );

        Product componentProduct = productRepository.findById(component.getComponentProductId())
                .orElseThrow();

        BomComponentResponse response = BomComponentResponse.from(component, componentProduct);

        return ResponseEntity.ok(response);
    }

    /**
     * Removes component from BOM
     *
     * DELETE /api/products/{id}/bom/{componentId}
     */
    @DeleteMapping("/{id}/bom/{componentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Remove component",
               description = "Removes a component from the BOM")
    public ResponseEntity<Void> removeComponent(
            @PathVariable UUID id,
            @PathVariable UUID componentId) {

        compositeService.removeComponent(id, componentId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Calculates available stock for virtual BOM
     *
     * GET /api/products/{id}/available-stock
     */
    @GetMapping("/{id}/available-stock")
    @Operation(summary = "Calculate available stock",
               description = "Calculates how many kits can be assembled based on component stock (virtual BOM only)")
    public ResponseEntity<CompositeProductService.AvailableStockResponse> getAvailableStock(
            @PathVariable UUID id) {

        CompositeProductService.AvailableStockResponse response =
                compositeService.calculateAvailableStock(id);

        return ResponseEntity.ok(response);
    }
}
