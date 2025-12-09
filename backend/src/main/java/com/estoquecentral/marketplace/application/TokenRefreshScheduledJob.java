package com.estoquecentral.marketplace.application;

import com.estoquecentral.auth.adapter.out.TenantRepository;
import com.estoquecentral.auth.domain.Tenant;
import com.estoquecentral.marketplace.adapter.out.MarketplaceConnectionRepository;
import com.estoquecentral.marketplace.domain.ConnectionStatus;
import com.estoquecentral.marketplace.domain.MarketplaceConnection;
import com.estoquecentral.shared.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to automatically refresh expiring OAuth2 tokens
 * Story 5.1: Mercado Livre OAuth2 Authentication - AC4
 *
 * Runs every hour to check for tokens expiring within 30 minutes
 * and refreshes them proactively.
 */
@Component
public class TokenRefreshScheduledJob {

    private static final Logger log = LoggerFactory.getLogger(TokenRefreshScheduledJob.class);
    private static final int REFRESH_THRESHOLD_MINUTES = 30;

    private final MarketplaceConnectionRepository connectionRepository;
    private final MercadoLivreOAuthService oauthService;
    private final TenantRepository tenantRepository;

    public TokenRefreshScheduledJob(
        MarketplaceConnectionRepository connectionRepository,
        MercadoLivreOAuthService oauthService,
        TenantRepository tenantRepository
    ) {
        this.connectionRepository = connectionRepository;
        this.oauthService = oauthService;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Refresh tokens for connections expiring soon
     * Runs every hour (3600000 ms = 1 hour)
     */
    @Scheduled(fixedDelay = 3600000) // 1 hour
    public void refreshExpiringTokens() {
        log.info("Starting token refresh job");

        try {
            // Get all active tenants
            List<Tenant> activeTenants = tenantRepository.findAllActive();

            if (activeTenants.isEmpty()) {
                log.debug("No active tenants found");
                return;
            }

            log.debug("Checking token expiration for {} active tenants", activeTenants.size());

            LocalDateTime expirationThreshold = LocalDateTime.now().plusMinutes(REFRESH_THRESHOLD_MINUTES);
            int totalSuccessCount = 0;
            int totalFailureCount = 0;

            // Process each tenant
            for (Tenant tenant : activeTenants) {
                try {
                    // Set tenant context
                    TenantContext.setTenantId(tenant.getId().toString());

                    // Find connections with tokens expiring within 30 minutes for this tenant
                    List<MarketplaceConnection> expiringConnections = connectionRepository.findExpiringConnectionsByTenant(
                        tenant.getId(),
                        ConnectionStatus.CONNECTED.name(),
                        expirationThreshold
                    );

                    if (expiringConnections.isEmpty()) {
                        continue;
                    }

                    log.info("Found {} connections with tokens expiring soon for tenant {}",
                        expiringConnections.size(), tenant.getId());

                    for (MarketplaceConnection connection : expiringConnections) {
                        try {
                            log.debug("Refreshing token for connection: {} (tenant: {}, marketplace: {})",
                                connection.getId(),
                                connection.getTenantId(),
                                connection.getMarketplace());

                            // Refresh token
                            oauthService.refreshToken(connection.getId());

                            totalSuccessCount++;
                            log.debug("Token refreshed successfully for connection: {}", connection.getId());

                        } catch (Exception e) {
                            totalFailureCount++;
                            log.error("Failed to refresh token for connection: {} (tenant: {})",
                                connection.getId(),
                                connection.getTenantId(),
                                e);

                            // Error status is already updated by oauthService.refreshToken()
                            // TODO: Send notification to user about failed refresh
                        }
                    }

                } catch (Exception e) {
                    log.error("Error processing token refresh for tenant: {}", tenant.getId(), e);
                } finally {
                    // Always clear tenant context
                    TenantContext.clear();
                }
            }

            log.info("Token refresh job completed. Success: {}, Failures: {}", totalSuccessCount, totalFailureCount);

        } catch (Exception e) {
            log.error("Error in token refresh job", e);
        }
    }

    /**
     * Manual trigger for testing purposes
     * Can be called via admin endpoint if needed
     */
    public void triggerManualRefresh() {
        log.info("Manual token refresh triggered");
        refreshExpiringTokens();
    }
}
