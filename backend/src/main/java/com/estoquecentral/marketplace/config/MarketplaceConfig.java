package com.estoquecentral.marketplace.config;

import com.estoquecentral.marketplace.application.MarketplaceStockSyncService;
import com.estoquecentral.marketplace.application.SafetyMarginService;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration for marketplace services
 * Story 5.7: Resolves circular dependency between SafetyMarginService and MarketplaceStockSyncService
 */
@Configuration
public class MarketplaceConfig {

    private final MarketplaceStockSyncService stockSyncService;
    private final SafetyMarginService safetyMarginService;

    public MarketplaceConfig(MarketplaceStockSyncService stockSyncService,
                            SafetyMarginService safetyMarginService) {
        this.stockSyncService = stockSyncService;
        this.safetyMarginService = safetyMarginService;
    }

    @PostConstruct
    public void init() {
        // Inject SafetyMarginService into MarketplaceStockSyncService after both beans are created
        stockSyncService.setSafetyMarginService(safetyMarginService);
    }
}
