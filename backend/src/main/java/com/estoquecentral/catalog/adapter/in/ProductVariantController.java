package com.estoquecentral.catalog.adapter.in;

import com.estoquecentral.catalog.adapter.in.dto.*;
import com.estoquecentral.catalog.application.ProductService;
import com.estoquecentral.catalog.application.variant.ProductVariantService;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductType;
import com.estoquecentral.catalog.domain.variant.ProductVariant;
import com.estoquecentral.shared.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ProductVariantController - REST API for product variant management
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>POST /api/products/variants - Create variant parent product</li>
 *   <li>POST /api/products/{id}/variants/generate - Generate all variant combinations</li>
 *   <li>GET /api/products/{id}/variants - List all variants</li>
 *   <li>POST /api/products/{id}/variants - Create single variant</li>
 *   <li>PUT /api/products/{id}/variants/{variantId} - Update variant</li>
 *   <li>DELETE /api/products/{id}/variants/{variantId} - Delete variant</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Variants", description = "Product variant management (matrix)")
public class ProductVariantController {

    private final ProductVariantService variantService;
    private final ProductService productService;

    @Autowired
    public ProductVariantController(ProductVariantService variantService,
                                    ProductService productService) {
        this.variantService = variantService;
        this.productService = productService;
    }

    /**
     * Creates variant parent product with attributes
     *
     * POST /api/products/variants
     */
    @PostMapping("/variants")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Create variant parent product",
               description = "Creates product with variants (type=VARIANT_PARENT)")
    public ResponseEntity<ProductDTO> createVariantProduct(
            @Valid @RequestBody CreateVariantProductRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        // Validate attributes (max 3)
        if (request.getAttributes().size() > 3) {
            throw new IllegalArgumentException("Maximum 3 attributes allowed");
        }

        // Create parent product
        Product parent = productService.create(
                tenantId,
                ProductType.VARIANT_PARENT,
                request.getName(),
                request.getBaseSku(),
                null, // no barcode for parent
                request.getDescription(),
                request.getCategoryId(),
                request.getPrice(),
                request.getCost(),
                request.getUnit(),
                true, // variants always control inventory
                userId
        );

        // TODO: Store attributes (will be used in generate endpoint)
        // For now, return parent product
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductDTO.fromEntity(parent));
    }

    /**
     * Generates all variant combinations (cartesian product)
     *
     * POST /api/products/{id}/variants/generate
     */
    @PostMapping("/{id}/variants/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Generate variant matrix",
               description = "Generates all variant combinations from attributes")
    public ResponseEntity<List<ProductVariantDTO>> generateVariants(
            @PathVariable UUID id,
            @RequestBody List<VariantAttributeDTO> attributes,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        // Validate max 3 attributes
        if (attributes.size() > 3) {
            throw new IllegalArgumentException("Maximum 3 attributes allowed");
        }

        // Calculate total variants
        int totalVariants = attributes.stream()
                .mapToInt(attr -> attr.getValues().size())
                .reduce(1, (a, b) -> a * b);

        if (totalVariants > 100) {
            throw new IllegalArgumentException(
                    "Maximum 100 variants allowed. Current combination would generate " + totalVariants + " variants"
            );
        }

        // Generate all combinations (cartesian product)
        List<Map<String, String>> combinations = generateCombinations(attributes);

        // Create variants
        List<ProductVariant> variants = new ArrayList<>();
        for (Map<String, String> combination : combinations) {
            ProductVariant variant = variantService.createVariant(
                    tenantId, id, combination, null, null, userId
            );
            variants.add(variant);
        }

        // Convert to DTOs
        List<ProductVariantDTO> dtos = variants.stream()
                .map(variant -> {
                    ProductVariantDTO dto = ProductVariantDTO.fromEntity(variant);
                    // TODO: Populate attribute combination from junction table
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Lists all variants for a parent product
     *
     * GET /api/products/{id}/variants
     */
    @GetMapping("/{id}/variants")
    @Operation(summary = "List product variants",
               description = "Returns all variants for a parent product")
    public ResponseEntity<List<ProductVariantDTO>> listVariants(@PathVariable UUID id) {
        List<ProductVariant> variants = variantService.listVariantsByParent(id);

        List<ProductVariantDTO> dtos = variants.stream()
                .map(ProductVariantDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Updates variant (SKU, price, cost, barcode)
     *
     * PUT /api/products/{id}/variants/{variantId}
     */
    @PutMapping("/{id}/variants/{variantId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Update variant", description = "Updates variant details")
    public ResponseEntity<ProductVariantDTO> updateVariant(
            @PathVariable UUID id,
            @PathVariable UUID variantId,
            @RequestBody ProductVariantDTO request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        ProductVariant variant = variantService.updateVariant(
                variantId,
                request.getName(),
                request.getBarcode(),
                request.getPrice(),
                request.getCost(),
                userId
        );

        return ResponseEntity.ok(ProductVariantDTO.fromEntity(variant));
    }

    /**
     * Deletes variant (soft delete)
     *
     * DELETE /api/products/{id}/variants/{variantId}
     */
    @DeleteMapping("/{id}/variants/{variantId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Delete variant", description = "Soft deletes variant")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable UUID id,
            @PathVariable UUID variantId) {

        variantService.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Generates cartesian product of attribute combinations
     *
     * Example: [Color: [Red, Blue], Size: [S, M]] →
     *   [{Color: Red, Size: S}, {Color: Red, Size: M}, {Color: Blue, Size: S}, {Color: Blue, Size: M}]
     */
    private List<Map<String, String>> generateCombinations(List<VariantAttributeDTO> attributes) {
        if (attributes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, String>> result = new ArrayList<>();
        result.add(new HashMap<>());

        for (VariantAttributeDTO attribute : attributes) {
            List<Map<String, String>> temp = new ArrayList<>();

            for (Map<String, String> existing : result) {
                for (String value : attribute.getValues()) {
                    Map<String, String> newCombination = new HashMap<>(existing);
                    newCombination.put(attribute.getName(), value);
                    temp.add(newCombination);
                }
            }

            result = temp;
        }

        return result;
    }
}
