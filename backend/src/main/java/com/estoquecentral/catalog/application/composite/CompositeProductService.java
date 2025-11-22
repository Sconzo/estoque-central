package com.estoquecentral.catalog.application.composite;

import com.estoquecentral.catalog.adapter.out.ProductComponentRepository;
import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.domain.BomType;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductComponent;
import com.estoquecentral.catalog.domain.ProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * CompositeProductService - Business logic for composite products/kits (BOM)
 *
 * <p>Key features:
 * <ul>
 *   <li>Manage Bill of Materials (BOM) - add/remove components</li>
 *   <li>Calculate available stock for virtual BOMs</li>
 *   <li>Assemble physical kits (deduct components, add kit stock)</li>
 *   <li>Validate components cannot be COMPOSITE (prevents recursion)</li>
 * </ul>
 */
@Service
@Transactional
public class CompositeProductService {

    private final ProductRepository productRepository;
    private final ProductComponentRepository componentRepository;

    @Autowired
    public CompositeProductService(ProductRepository productRepository,
                                   ProductComponentRepository componentRepository) {
        this.productRepository = productRepository;
        this.componentRepository = componentRepository;
    }

    /**
     * Adds component to composite product BOM
     *
     * @param productId composite product ID
     * @param componentProductId component product ID
     * @param quantityRequired quantity needed
     * @param userId user performing operation
     * @return created component
     * @throws IllegalArgumentException if validation fails
     */
    public ProductComponent addComponent(UUID productId, UUID componentProductId,
                                        BigDecimal quantityRequired, UUID userId) {
        // Validate composite product
        Product product = productRepository.findByIdAndActive(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (product.getType() != ProductType.COMPOSITE) {
            throw new IllegalArgumentException("Only COMPOSITE products can have components");
        }

        // Validate component product
        Product componentProduct = productRepository.findByIdAndActive(componentProductId)
                .orElseThrow(() -> new IllegalArgumentException("Component product not found: " + componentProductId));

        // CRITICAL: Prevent recursion - component cannot be COMPOSITE
        if (componentProduct.getType() == ProductType.COMPOSITE) {
            throw new IllegalArgumentException(
                    "Component cannot be a COMPOSITE product. Only SIMPLE or VARIANT products allowed.");
        }

        // Check if component already exists
        if (componentRepository.findByProductIdAndComponentProductId(productId, componentProductId).isPresent()) {
            throw new IllegalArgumentException("Component already exists in this product");
        }

        // Create component
        ProductComponent component = new ProductComponent(productId, componentProductId, quantityRequired);
        component.setCreatedBy(userId);

        return componentRepository.save(component);
    }

    /**
     * Updates component quantity
     *
     * @param productId composite product ID
     * @param componentProductId component ID
     * @param newQuantity new quantity required
     * @param userId user performing operation
     * @return updated component
     */
    public ProductComponent updateComponentQuantity(UUID productId, UUID componentProductId,
                                                   BigDecimal newQuantity, UUID userId) {
        ProductComponent component = componentRepository.findByProductIdAndComponentProductId(
                productId, componentProductId)
                .orElseThrow(() -> new IllegalArgumentException("Component not found"));

        component.updateQuantity(newQuantity);
        component.setUpdatedBy(userId);

        return componentRepository.save(component);
    }

    /**
     * Removes component from BOM
     *
     * @param productId composite product ID
     * @param componentProductId component ID
     */
    public void removeComponent(UUID productId, UUID componentProductId) {
        componentRepository.deleteByProductIdAndComponentProductId(productId, componentProductId);
    }

    /**
     * Lists all components of a composite product
     *
     * @param productId composite product ID
     * @return list of components
     */
    @Transactional(readOnly = true)
    public List<ProductComponent> listComponents(UUID productId) {
        return componentRepository.findByProductId(productId);
    }

    /**
     * Calculates available stock for virtual BOM
     *
     * <p>Algorithm: MIN(component_stock / quantity_required) for all components
     * <p>Example: Kit needs 2 espetos (stock: 10) and 1 carvão (stock: 3)
     *              → can make 3 kits (limited by carvão: 3/1 = 3, espetos: 10/2 = 5)
     *
     * @param productId composite product ID
     * @return available quantity (kits that can be assembled)
     */
    @Transactional(readOnly = true)
    public AvailableStockResponse calculateAvailableStock(UUID productId) {
        Product product = productRepository.findByIdAndActive(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (product.getType() != ProductType.COMPOSITE) {
            throw new IllegalArgumentException("Can only calculate stock for COMPOSITE products");
        }

        if (product.getBomType() != BomType.VIRTUAL) {
            throw new IllegalArgumentException(
                    "Stock calculation only applies to VIRTUAL BOMs. PHYSICAL BOMs have their own stock.");
        }

        List<ProductComponent> components = componentRepository.findByProductId(productId);

        if (components.isEmpty()) {
            return new AvailableStockResponse(productId, 0, null, "No components defined");
        }

        // TODO: Implement actual stock calculation when Stock module is available
        // For now, return placeholder response
        return new AvailableStockResponse(productId, 0, null,
                "Stock calculation requires Stock module (Story 2.7)");
    }

    /**
     * Validates if product can have components added
     *
     * @param productId product ID
     * @return true if valid
     */
    @Transactional(readOnly = true)
    public boolean canAddComponents(UUID productId) {
        Product product = productRepository.findByIdAndActive(productId).orElse(null);
        return product != null && product.getType() == ProductType.COMPOSITE;
    }

    // ==================== Inner Classes ====================

    /**
     * Response for available stock calculation
     */
    public static class AvailableStockResponse {
        private final UUID productId;
        private final int availableQuantity;
        private final UUID limitingComponentId;
        private final String message;

        public AvailableStockResponse(UUID productId, int availableQuantity,
                                     UUID limitingComponentId, String message) {
            this.productId = productId;
            this.availableQuantity = availableQuantity;
            this.limitingComponentId = limitingComponentId;
            this.message = message;
        }

        public UUID getProductId() {
            return productId;
        }

        public int getAvailableQuantity() {
            return availableQuantity;
        }

        public UUID getLimitingComponentId() {
            return limitingComponentId;
        }

        public String getMessage() {
            return message;
        }
    }
}
