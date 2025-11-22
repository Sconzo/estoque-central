package com.estoquecentral.catalog.application.variant;

import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.adapter.out.variant.ProductVariantRepository;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductType;
import com.estoquecentral.catalog.domain.variant.ProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * ProductVariantService - Business logic for product variants
 *
 * <p>Key features:
 * <ul>
 *   <li>Create variants with auto-generated SKUs</li>
 *   <li>Validate parent product is VARIANT_PARENT type</li>
 *   <li>List variants for parent product</li>
 * </ul>
 */
@Service
@Transactional
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ProductVariantService(ProductVariantRepository variantRepository,
                                 ProductRepository productRepository) {
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
    }

    /**
     * Creates variant for parent product with auto-generated SKU
     *
     * @param tenantId tenant ID
     * @param parentProductId parent product ID
     * @param attributeValues map of attribute values (e.g., {color: red, size: M})
     * @param price price (null to inherit from parent)
     * @param cost cost (null to inherit from parent)
     * @param userId user creating variant
     * @return created variant
     */
    public ProductVariant createVariant(UUID tenantId, UUID parentProductId,
                                        Map<String, String> attributeValues,
                                        BigDecimal price, BigDecimal cost, UUID userId) {
        // Validate parent product
        Product parent = productRepository.findByIdAndActive(parentProductId)
                .orElseThrow(() -> new IllegalArgumentException("Parent product not found: " + parentProductId));

        if (parent.getType() != ProductType.VARIANT_PARENT) {
            throw new IllegalArgumentException("Product must be VARIANT_PARENT type to have variants");
        }

        // Generate SKU
        String variantSku = generateVariantSku(parent.getSku(), attributeValues);

        // Check SKU uniqueness
        if (variantRepository.findByTenantIdAndSku(tenantId, variantSku).isPresent()) {
            throw new IllegalArgumentException("Variant SKU already exists: " + variantSku);
        }

        // Generate name
        String variantName = generateVariantName(parent.getName(), attributeValues);

        // Use parent price/cost if not provided
        BigDecimal variantPrice = price != null ? price : parent.getPrice();
        BigDecimal variantCost = cost != null ? cost : parent.getCost();

        // Create variant
        ProductVariant variant = new ProductVariant(
                tenantId, parentProductId, variantSku, variantName, variantPrice, variantCost
        );
        variant.setCreatedBy(userId);

        return variantRepository.save(variant);
    }

    /**
     * Gets variant by ID
     *
     * @param id variant ID
     * @return variant
     */
    @Transactional(readOnly = true)
    public ProductVariant getById(UUID id) {
        return variantRepository.findByIdAndActive(id)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + id));
    }

    /**
     * Lists all variants for parent product
     *
     * @param parentProductId parent product ID
     * @return list of variants
     */
    @Transactional(readOnly = true)
    public List<ProductVariant> listVariantsByParent(UUID parentProductId) {
        return variantRepository.findByParentProductId(parentProductId);
    }

    /**
     * Counts variants for parent product
     *
     * @param parentProductId parent product ID
     * @return count of variants
     */
    @Transactional(readOnly = true)
    public long countVariantsByParent(UUID parentProductId) {
        return variantRepository.countByParentProductId(parentProductId);
    }

    /**
     * Updates variant
     *
     * @param id variant ID
     * @param name variant name
     * @param barcode barcode
     * @param price price
     * @param cost cost
     * @param userId user updating variant
     * @return updated variant
     */
    public ProductVariant updateVariant(UUID id, String name, String barcode,
                                       BigDecimal price, BigDecimal cost, UUID userId) {
        ProductVariant variant = getById(id);

        if (name != null) {
            variant.setName(name);
        }
        if (barcode != null) {
            variant.setBarcode(barcode);
        }
        if (price != null) {
            variant.setPrice(price);
        }
        if (cost != null) {
            variant.setCost(cost);
        }

        variant.setUpdatedBy(userId);

        return variantRepository.save(variant);
    }

    /**
     * Deletes variant (soft delete)
     *
     * @param id variant ID
     */
    public void deleteVariant(UUID id) {
        ProductVariant variant = getById(id);
        variant.deactivate();
        variantRepository.save(variant);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Generates SKU for variant
     * Format: PARENT-SKU-ATTR1-ATTR2
     * Example: TSHIRT-BASIC-RED-M
     */
    private String generateVariantSku(String parentSku, Map<String, String> attributeValues) {
        StringBuilder sku = new StringBuilder(parentSku);

        // Sort attributes by key for consistent SKU generation
        List<String> keys = new ArrayList<>(attributeValues.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            String value = attributeValues.get(key);
            sku.append("-").append(value.toUpperCase().replace(" ", "-"));
        }

        return sku.toString();
    }

    /**
     * Generates name for variant
     * Format: Parent Name - Attr1 Value - Attr2 Value
     * Example: T-shirt Basic - Red - M
     */
    private String generateVariantName(String parentName, Map<String, String> attributeValues) {
        StringBuilder name = new StringBuilder(parentName);

        // Sort attributes by key
        List<String> keys = new ArrayList<>(attributeValues.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            String value = attributeValues.get(key);
            name.append(" - ").append(capitalizeFirst(value));
        }

        return name.toString();
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
