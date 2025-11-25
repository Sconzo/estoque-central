package com.estoquecentral.marketplace.adapter.in.web;

import com.estoquecentral.marketplace.adapter.out.MarketplaceConnectionRepository;
import com.estoquecentral.marketplace.application.MercadoLivreOAuthService;
import com.estoquecentral.marketplace.application.MercadoLivreProductImportService;
import com.estoquecentral.marketplace.application.dto.ImportListingsRequest;
import com.estoquecentral.marketplace.application.dto.ImportListingsResponse;
import com.estoquecentral.marketplace.application.dto.ListingPreviewResponse;
import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.MarketplaceConnection;
import com.estoquecentral.shared.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Mercado Livre OAuth2 integration
 * Story 5.1: Mercado Livre OAuth2 Authentication - AC2, AC3, AC7
 */
@RestController
@RequestMapping("/api/integrations/mercadolivre")
public class MercadoLivreController {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreController.class);

    private final MercadoLivreOAuthService oauthService;
    private final MarketplaceConnectionRepository connectionRepository;
    private final MercadoLivreProductImportService importService;

    public MercadoLivreController(
        MercadoLivreOAuthService oauthService,
        MarketplaceConnectionRepository connectionRepository,
        MercadoLivreProductImportService importService
    ) {
        this.oauthService = oauthService;
        this.connectionRepository = connectionRepository;
        this.importService = importService;
    }

    /**
     * AC2: Initialize OAuth2 flow
     * GET /api/integrations/mercadolivre/auth/init
     *
     * Returns the authorization URL to redirect user to Mercado Livre
     */
    @GetMapping("/auth/init")
    public ResponseEntity<Map<String, String>> initAuth() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        log.info("Initializing Mercado Livre OAuth for tenant: {}", tenantId);

        String authorizationUrl = oauthService.getAuthorizationUrl(tenantId);

        return ResponseEntity.ok(Map.of(
            "authorization_url", authorizationUrl,
            "message", "Redirect user to authorization_url"
        ));
    }

    /**
     * AC3: OAuth2 callback handler
     * GET /api/integrations/mercadolivre/auth/callback?code={code}&state={state}
     *
     * Handles callback from Mercado Livre after user authorization
     * Exchanges code for access token and saves connection
     */
    @GetMapping("/auth/callback")
    public ResponseEntity<Void> handleCallback(
        @RequestParam("code") String code,
        @RequestParam("state") String state
    ) {
        log.info("Received OAuth callback with code and state");

        try {
            oauthService.handleCallback(code, state);

            // Redirect to frontend success page
            String redirectUrl = "http://localhost:4200/integrations?status=success";
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();

        } catch (Exception e) {
            log.error("Error handling OAuth callback", e);

            // Redirect to frontend error page
            String redirectUrl = "http://localhost:4200/integrations?status=error&message=" + e.getMessage();
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
        }
    }

    /**
     * Get current connection status
     * GET /api/integrations/mercadolivre/status
     */
    @GetMapping("/status")
    public ResponseEntity<ConnectionStatusResponse> getStatus() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        return connectionRepository
            .findByTenantIdAndMarketplace(tenantId, Marketplace.MERCADO_LIVRE.name())
            .map(connection -> {
                ConnectionStatusResponse response = new ConnectionStatusResponse();
                response.setConnected(connection.isActive());
                response.setStatus(connection.getStatus().name());
                response.setUserIdMarketplace(connection.getUserIdMarketplace());
                response.setLastSyncAt(connection.getLastSyncAt());
                response.setTokenExpiresAt(connection.getTokenExpiresAt());
                response.setErrorMessage(connection.getErrorMessage());
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.ok(ConnectionStatusResponse.disconnected()));
    }

    /**
     * AC7: Disconnect Mercado Livre integration
     * POST /api/integrations/mercadolivre/disconnect
     *
     * Revokes tokens and updates status to DISCONNECTED
     */
    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, String>> disconnect() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        log.info("Disconnecting Mercado Livre for tenant: {}", tenantId);

        try {
            oauthService.disconnect(tenantId);

            return ResponseEntity.ok(Map.of(
                "message", "Mercado Livre disconnected successfully"
            ));

        } catch (Exception e) {
            log.error("Error disconnecting Mercado Livre for tenant: {}", tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * AC2: Get listings preview (for import selection)
     * GET /api/integrations/mercadolivre/listings
     * Story 5.2: Import Products from Mercado Livre
     */
    @GetMapping("/listings")
    public ResponseEntity<List<ListingPreviewResponse>> getListings() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        log.info("Fetching listings for tenant: {}", tenantId);

        try {
            List<ListingPreviewResponse> listings = importService.getListingsPreview(tenantId);
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            log.error("Error fetching listings for tenant: {}", tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * AC3: Import selected listings
     * POST /api/integrations/mercadolivre/import-listings
     * Story 5.2: Import Products from Mercado Livre
     */
    @PostMapping("/import-listings")
    public ResponseEntity<ImportListingsResponse> importListings(
        @RequestBody ImportListingsRequest request
    ) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        log.info("Importing {} listings for tenant: {}", request.getListingIds().size(), tenantId);

        try {
            ImportListingsResponse response = importService.importListings(tenantId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error importing listings for tenant: {}", tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ImportListingsResponse());
        }
    }

    /**
     * Test endpoint - Get Mercado Livre user info
     * GET /api/integrations/mercadolivre/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> testConnection() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        log.info("Testing Mercado Livre connection for tenant: {}", tenantId);

        // This would use MercadoLivreApiClient to call /users/me
        // Implemented in future stories
        return ResponseEntity.ok("Test endpoint - to be implemented");
    }

    // Response DTOs

    public static class ConnectionStatusResponse {
        private boolean connected;
        private String status;
        private String userIdMarketplace;
        private java.time.LocalDateTime lastSyncAt;
        private java.time.LocalDateTime tokenExpiresAt;
        private String errorMessage;

        public static ConnectionStatusResponse disconnected() {
            ConnectionStatusResponse response = new ConnectionStatusResponse();
            response.setConnected(false);
            response.setStatus("DISCONNECTED");
            return response;
        }

        // Getters and Setters

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getUserIdMarketplace() {
            return userIdMarketplace;
        }

        public void setUserIdMarketplace(String userIdMarketplace) {
            this.userIdMarketplace = userIdMarketplace;
        }

        public java.time.LocalDateTime getLastSyncAt() {
            return lastSyncAt;
        }

        public void setLastSyncAt(java.time.LocalDateTime lastSyncAt) {
            this.lastSyncAt = lastSyncAt;
        }

        public java.time.LocalDateTime getTokenExpiresAt() {
            return tokenExpiresAt;
        }

        public void setTokenExpiresAt(java.time.LocalDateTime tokenExpiresAt) {
            this.tokenExpiresAt = tokenExpiresAt;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
