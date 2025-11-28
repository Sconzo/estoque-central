package com.estoquecentral.marketplace.application;

import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.marketplace.adapter.out.SafetyMarginRuleRepository;
import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.RulePriority;
import com.estoquecentral.marketplace.domain.SafetyMarginRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SafetyMarginService - Business logic for safety margin rules
 * Story 5.7: Configurable Safety Stock Margin - AC2
 *
 * <p>Implements priority-based rule selection and margin calculation:
 * <ul>
 *   <li>Priority 1 (PRODUCT): Check for product-specific rule first</li>
 *   <li>Priority 2 (CATEGORY): Check category rule if no product rule</li>
 *   <li>Priority 3 (GLOBAL): Use marketplace global rule as fallback</li>
 *   <li>Default: 100% if no rules configured</li>
 * </ul>
 *
 * <p><strong>Calculation Formula:</strong>
 * <pre>quantity_to_publish = floor(quantity_available * (margin_percentage / 100))</pre>
 */
@Service
public class SafetyMarginService {

    private static final Logger log = LoggerFactory.getLogger(SafetyMarginService.class);
    private static final BigDecimal DEFAULT_MARGIN = new BigDecimal("100.00");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final SafetyMarginRuleRepository ruleRepository;
    private final ProductRepository productRepository;
    private final MarketplaceStockSyncService stockSyncService;

    public SafetyMarginService(
            SafetyMarginRuleRepository ruleRepository,
            ProductRepository productRepository,
            MarketplaceStockSyncService stockSyncService) {
        this.ruleRepository = ruleRepository;
        this.productRepository = productRepository;
        this.stockSyncService = stockSyncService;
    }

    /**
     * AC2: Calculates quantity to publish applying safety margin
     *
     * @param tenantId tenant ID
     * @param marketplace marketplace
     * @param productId product ID
     * @param quantityAvailable available quantity in inventory
     * @return quantity to publish (floor of available * margin%)
     */
    public int calculateQuantityToPublish(UUID tenantId, Marketplace marketplace, UUID productId, BigDecimal quantityAvailable) {
        // Get applicable margin percentage
        BigDecimal marginPercentage = getApplicableMargin(tenantId, marketplace, productId);

        // Calculate: quantity_available * (margin_percentage / 100)
        BigDecimal multiplier = marginPercentage.divide(HUNDRED, 4, RoundingMode.HALF_UP);
        BigDecimal result = quantityAvailable.multiply(multiplier);

        // Round down (floor) to prevent overselling
        int quantityToPublish = result.setScale(0, RoundingMode.DOWN).intValue();

        log.debug("Safety margin calculation: available={}, margin={}%, result={}",
                quantityAvailable, marginPercentage, quantityToPublish);

        return quantityToPublish;
    }

    /**
     * AC2: Gets applicable margin percentage using priority-based selection
     *
     * Priority order: Product (1) → Category (2) → Global (3) → Default (100%)
     *
     * @param tenantId tenant ID
     * @param marketplace marketplace
     * @param productId product ID
     * @return margin percentage (0-100)
     */
    public BigDecimal getApplicableMargin(UUID tenantId, Marketplace marketplace, UUID productId) {
        // Priority 1: Product-specific rule
        Optional<SafetyMarginRule> productRule = ruleRepository.findProductRule(
                tenantId, marketplace.name(), productId);

        if (productRule.isPresent()) {
            log.debug("Using product-specific margin rule for product {}: {}%",
                    productId, productRule.get().getMarginPercentage());
            return productRule.get().getMarginPercentage();
        }

        // Priority 2: Category rule (need to fetch product's category)
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent() && product.get().getCategoryId() != null) {
            UUID categoryId = product.get().getCategoryId();
            Optional<SafetyMarginRule> categoryRule = ruleRepository.findCategoryRule(
                    tenantId, marketplace.name(), categoryId);

            if (categoryRule.isPresent()) {
                log.debug("Using category margin rule for product {} (category {}): {}%",
                        productId, categoryId, categoryRule.get().getMarginPercentage());
                return categoryRule.get().getMarginPercentage();
            }
        }

        // Priority 3: Global marketplace rule
        Optional<SafetyMarginRule> globalRule = ruleRepository.findGlobalRule(
                tenantId, marketplace.name());

        if (globalRule.isPresent()) {
            log.debug("Using global margin rule for marketplace {}: {}%",
                    marketplace, globalRule.get().getMarginPercentage());
            return globalRule.get().getMarginPercentage();
        }

        // Default: 100% (no margin)
        log.debug("No margin rule found, using default 100% for product {}", productId);
        return DEFAULT_MARGIN;
    }

    /**
     * AC3: Creates a new safety margin rule
     */
    @Transactional
    public SafetyMarginRule createRule(SafetyMarginRule rule) {
        rule.validate();

        // Check for duplicates
        if (rule.isProductRule() && ruleRepository.existsProductRule(
                rule.getTenantId(), rule.getMarketplace().name(), rule.getProductId())) {
            throw new IllegalArgumentException("A rule already exists for this product");
        }
        if (rule.isCategoryRule() && ruleRepository.existsCategoryRule(
                rule.getTenantId(), rule.getMarketplace().name(), rule.getCategoryId())) {
            throw new IllegalArgumentException("A rule already exists for this category");
        }
        if (rule.isGlobalRule() && ruleRepository.existsGlobalRule(
                rule.getTenantId(), rule.getMarketplace().name())) {
            throw new IllegalArgumentException("A global rule already exists for this marketplace");
        }

        SafetyMarginRule saved = ruleRepository.save(rule);
        log.info("Created safety margin rule: id={}, priority={}, margin={}%",
                saved.getId(), saved.getPriorityEnum(), saved.getMarginPercentage());

        return saved;
    }

    /**
     * AC4: Lists all rules for a tenant
     */
    @Transactional(readOnly = true)
    public List<SafetyMarginRule> listRules(UUID tenantId, Marketplace marketplace) {
        if (marketplace != null) {
            return ruleRepository.findByTenantIdAndMarketplaceForListing(tenantId, marketplace.name());
        }
        return ruleRepository.findAllByTenantId(tenantId);
    }

    /**
     * AC5: Updates a rule's margin percentage
     */
    @Transactional
    public SafetyMarginRule updateRule(UUID tenantId, UUID ruleId, BigDecimal newMargin) {
        Optional<SafetyMarginRule> ruleOpt = ruleRepository.findById(ruleId);

        if (ruleOpt.isEmpty() || !ruleOpt.get().getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Rule not found");
        }

        SafetyMarginRule rule = ruleOpt.get();
        rule.updateMargin(newMargin);
        SafetyMarginRule updated = ruleRepository.save(rule);

        log.info("Updated safety margin rule: id={}, new margin={}%", ruleId, newMargin);

        // AC5: Trigger re-sync of affected products
        triggerResyncForRule(rule);

        return updated;
    }

    /**
     * AC6: Deletes a rule
     */
    @Transactional
    public void deleteRule(UUID tenantId, UUID ruleId) {
        Optional<SafetyMarginRule> ruleOpt = ruleRepository.findById(ruleId);

        if (ruleOpt.isEmpty() || !ruleOpt.get().getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Rule not found");
        }

        SafetyMarginRule rule = ruleOpt.get();
        ruleRepository.deleteById(ruleId);

        log.info("Deleted safety margin rule: id={}, priority={}", ruleId, rule.getPriorityEnum());

        // AC6: Trigger re-sync (will use fallback rule or default 100%)
        triggerResyncForRule(rule);
    }

    /**
     * Triggers stock re-sync for all products affected by a rule change/deletion
     */
    private void triggerResyncForRule(SafetyMarginRule rule) {
        try {
            if (rule.isProductRule()) {
                // Re-sync single product
                stockSyncService.enqueueStockSync(
                        rule.getTenantId(),
                        rule.getProductId(),
                        null,
                        rule.getMarketplace()
                );
                log.info("Triggered re-sync for product {}", rule.getProductId());

            } else if (rule.isCategoryRule()) {
                // Re-sync all products in category
                List<UUID> affectedProducts = ruleRepository.findAffectedProducts(
                        rule.getTenantId(),
                        rule.getMarketplace().name(),
                        rule.getCategoryId()
                );

                for (UUID productId : affectedProducts) {
                    stockSyncService.enqueueStockSync(
                            rule.getTenantId(),
                            productId,
                            null,
                            rule.getMarketplace()
                    );
                }
                log.info("Triggered re-sync for {} products in category {}",
                        affectedProducts.size(), rule.getCategoryId());

            } else if (rule.isGlobalRule()) {
                // Re-sync all products in marketplace
                List<UUID> affectedProducts = ruleRepository.findAffectedProducts(
                        rule.getTenantId(),
                        rule.getMarketplace().name(),
                        null
                );

                for (UUID productId : affectedProducts) {
                    stockSyncService.enqueueStockSync(
                            rule.getTenantId(),
                            productId,
                            null,
                            rule.getMarketplace()
                    );
                }
                log.info("Triggered re-sync for {} products in marketplace {}",
                        affectedProducts.size(), rule.getMarketplace());
            }

        } catch (Exception e) {
            log.error("Error triggering re-sync after rule change: {}", e.getMessage(), e);
            // Don't fail the rule operation if re-sync fails
        }
    }
}
