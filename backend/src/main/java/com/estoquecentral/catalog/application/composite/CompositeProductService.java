package com.estoquecentral.catalog.application.composite;

import com.estoquecentral.catalog.adapter.out.ProductComponentRepository;
import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.domain.BomType;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductComponent;
import com.estoquecentral.catalog.domain.ProductType;
import com.estoquecentral.inventory.adapter.out.InventoryRepository;
import com.estoquecentral.inventory.domain.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
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
    private final InventoryRepository inventoryRepository;

    @Autowired
    public CompositeProductService(ProductRepository productRepository,
                                   ProductComponentRepository componentRepository,
                                   InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.componentRepository = componentRepository;
        this.inventoryRepository = inventoryRepository;
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
     * Story 2.7 - AC4: BOM Virtual Stock Calculation
     *
     * @param productId composite product ID
     * @param tenantId tenant ID
     * @param locationId optional location ID (if null, calculates across all locations)
     * @return available quantity (kits that can be assembled)
     */
    @Transactional(readOnly = true)
    public AvailableStockResponse calculateAvailableStock(UUID productId, UUID tenantId, UUID locationId) {
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
            return new AvailableStockResponse(productId, BigDecimal.ZERO, null, null,
                    "No components defined for this composite product");
        }

        // Calculate available kits based on component stock
        BigDecimal minKits = null;
        UUID limitingComponentId = null;
        String limitingComponentName = null;
        BigDecimal limitingComponentStock = null;

        for (ProductComponent component : components) {
            // Get stock for this component
            BigDecimal componentStock;

            if (locationId != null) {
                // Stock at specific location
                Optional<Inventory> inventory = inventoryRepository.findByTenantIdAndProductIdAndLocationId(
                        tenantId, component.getComponentProductId(), locationId);

                componentStock = inventory.map(Inventory::getComputedQuantityForSale)
                                         .orElse(BigDecimal.ZERO);
            } else {
                // Aggregate stock across all locations
                List<Inventory> inventories = inventoryRepository.findAllByTenantIdAndProductId(
                        tenantId, component.getComponentProductId());

                componentStock = inventories.stream()
                        .map(Inventory::getComputedQuantityForSale)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            // If any component has zero stock, we can't make any kits
            if (componentStock.compareTo(BigDecimal.ZERO) == 0) {
                Product componentProduct = productRepository.findByIdAndActive(component.getComponentProductId())
                        .orElse(null);
                String componentName = componentProduct != null ? componentProduct.getName() : "Unknown";

                return new AvailableStockResponse(productId, BigDecimal.ZERO,
                        component.getComponentProductId(), componentName,
                        "Component '" + componentName + "' is out of stock");
            }

            // Calculate how many kits we can make with this component
            // possibleKits = component_stock / quantity_required (rounded down)
            BigDecimal possibleKits = componentStock.divide(
                    component.getQuantityRequired(), 0, RoundingMode.DOWN);

            // Track the minimum (limiting component)
            if (minKits == null || possibleKits.compareTo(minKits) < 0) {
                minKits = possibleKits;
                limitingComponentId = component.getComponentProductId();
                limitingComponentStock = componentStock;

                // Get component name for better response
                Product componentProduct = productRepository.findByIdAndActive(component.getComponentProductId())
                        .orElse(null);
                limitingComponentName = componentProduct != null ? componentProduct.getName() : "Unknown";
            }
        }

        String message = minKits != null && minKits.compareTo(BigDecimal.ZERO) > 0
                ? String.format("Can make %s kits. Limited by component '%s' (stock: %s)",
                        minKits.intValue(), limitingComponentName, limitingComponentStock)
                : "Insufficient stock to make any kits";

        return new AvailableStockResponse(productId,
                minKits != null ? minKits : BigDecimal.ZERO,
                limitingComponentId,
                limitingComponentName,
                message);
    }

    /**
     * Overloaded method for backward compatibility (calculates across all locations)
     */
    @Transactional(readOnly = true)
    public AvailableStockResponse calculateAvailableStock(UUID productId) {
        // This method needs tenantId - will throw exception to force migration to new signature
        throw new UnsupportedOperationException(
                "Use calculateAvailableStock(productId, tenantId, locationId) instead");
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
     * Story 2.7 - AC4: BOM Virtual stock response
     */
    public static class AvailableStockResponse {
        private final UUID productId;
        private final BigDecimal availableQuantity;
        private final UUID limitingComponentId;
        private final String limitingComponentName;
        private final String message;

        public AvailableStockResponse(UUID productId, BigDecimal availableQuantity,
                                     UUID limitingComponentId, String limitingComponentName,
                                     String message) {
            this.productId = productId;
            this.availableQuantity = availableQuantity;
            this.limitingComponentId = limitingComponentId;
            this.limitingComponentName = limitingComponentName;
            this.message = message;
        }

        public UUID getProductId() {
            return productId;
        }

        public BigDecimal getAvailableQuantity() {
            return availableQuantity;
        }

        public UUID getLimitingComponentId() {
            return limitingComponentId;
        }

        public String getLimitingComponentName() {
            return limitingComponentName;
        }

        public String getMessage() {
            return message;
        }
    }
}
