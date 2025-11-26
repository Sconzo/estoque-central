package com.estoquecentral.marketplace.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled worker for processing marketplace stock sync queue
 * Story 5.4: Stock Synchronization to Mercado Livre - AC2
 * Runs every 1 minute to ensure < 5min latency (NFR4)
 */
@Component
public class StockSyncScheduledWorker {

    private static final Logger log = LoggerFactory.getLogger(StockSyncScheduledWorker.class);

    private final MarketplaceStockSyncService syncService;

    public StockSyncScheduledWorker(MarketplaceStockSyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * AC2: Worker roda a cada 1 minuto
     * Ensures latência < 5min in 95% of cases (NFR4)
     */
    @Scheduled(fixedDelay = 60000) // 1 minute
    public void processStockSyncQueue() {
        try {
            log.debug("Starting stock sync queue processing");
            syncService.processSyncQueue();
            log.debug("Finished stock sync queue processing");
        } catch (Exception e) {
            log.error("Error processing stock sync queue", e);
        }
    }
}
