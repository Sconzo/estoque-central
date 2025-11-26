package com.estoquecentral.marketplace.application;

import com.estoquecentral.marketplace.adapter.out.MarketplaceConnectionRepository;
import com.estoquecentral.marketplace.domain.EncryptedStringConverter;
import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.MarketplaceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * API Client wrapper for Mercado Livre API calls
 * Story 5.1: Mercado Livre OAuth2 Authentication - AC8
 *
 * Handles automatic token refresh on 401 errors and retry logic
 */
@Service
public class MercadoLivreApiClient {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreApiClient.class);
    private static final String ML_API_BASE = "https://api.mercadolibre.com";

    private final MarketplaceConnectionRepository connectionRepository;
    private final EncryptedStringConverter encryptedStringConverter;
    private final MercadoLivreOAuthService oauthService;
    private final RestTemplate restTemplate;

    public MercadoLivreApiClient(
        MarketplaceConnectionRepository connectionRepository,
        EncryptedStringConverter encryptedStringConverter,
        MercadoLivreOAuthService oauthService,
        RestTemplate restTemplate
    ) {
        this.connectionRepository = connectionRepository;
        this.encryptedStringConverter = encryptedStringConverter;
        this.oauthService = oauthService;
        this.restTemplate = restTemplate;
    }

    /**
     * Execute GET request to Mercado Livre API with automatic token refresh on 401
     *
     * @param endpoint API endpoint (e.g., "/users/me")
     * @param responseType Response class type
     * @param tenantId Tenant ID for auth
     * @return Response body
     */
    public <T> T get(String endpoint, Class<T> responseType, UUID tenantId) {
        return executeWithRetry(endpoint, HttpMethod.GET, null, responseType, tenantId);
    }

    /**
     * Execute POST request to Mercado Livre API with automatic token refresh on 401
     *
     * @param endpoint API endpoint
     * @param body Request body
     * @param responseType Response class type
     * @param tenantId Tenant ID for auth
     * @return Response body
     */
    public <T> T post(String endpoint, Object body, Class<T> responseType, UUID tenantId) {
        return executeWithRetry(endpoint, HttpMethod.POST, body, responseType, tenantId);
    }

    /**
     * Execute PUT request to Mercado Livre API with automatic token refresh on 401
     */
    public <T> T put(String endpoint, Object body, Class<T> responseType, UUID tenantId) {
        return executeWithRetry(endpoint, HttpMethod.PUT, body, responseType, tenantId);
    }

    /**
     * Execute DELETE request to Mercado Livre API with automatic token refresh on 401
     */
    public <T> T delete(String endpoint, Class<T> responseType, UUID tenantId) {
        return executeWithRetry(endpoint, HttpMethod.DELETE, null, responseType, tenantId);
    }

    /**
     * Execute request with automatic retry on 401 (token expired)
     */
    private <T> T executeWithRetry(
        String endpoint,
        HttpMethod method,
        Object body,
        Class<T> responseType,
        UUID tenantId
    ) {
        MarketplaceConnection connection = getConnection(tenantId);

        try {
            // First attempt
            return executeRequest(endpoint, method, body, responseType, connection);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.warn("Received 401 Unauthorized. Refreshing token and retrying for tenant: {}", tenantId);

                // Token expired - refresh and retry
                try {
                    oauthService.refreshToken(connection.getId());

                    // Reload connection with new token
                    connection = getConnection(tenantId);

                    // Retry request
                    return executeRequest(endpoint, method, body, responseType, connection);

                } catch (Exception refreshException) {
                    log.error("Token refresh failed for tenant: {}", tenantId, refreshException);
                    throw new RuntimeException("Token refresh failed", refreshException);
                }
            }

            // Non-401 error, rethrow
            log.error("API request failed: {} {} - Status: {}", method, endpoint, e.getStatusCode());
            throw e;
        }
    }

    /**
     * Execute HTTP request to Mercado Livre API
     */
    private <T> T executeRequest(
        String endpoint,
        HttpMethod method,
        Object body,
        Class<T> responseType,
        MarketplaceConnection connection
    ) {
        String url = ML_API_BASE + endpoint;
        String decryptedAccessToken = encryptedStringConverter.decrypt(connection.getAccessToken());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(decryptedAccessToken);

        HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);

        log.debug("Executing {} request to: {}", method, url);

        ResponseEntity<T> response = restTemplate.exchange(
            url,
            method,
            requestEntity,
            responseType
        );

        return response.getBody();
    }

    /**
     * Get active Mercado Livre connection for tenant
     */
    private MarketplaceConnection getConnection(UUID tenantId) {
        return connectionRepository
            .findByTenantIdAndMarketplace(tenantId, Marketplace.MERCADO_LIVRE.name())
            .orElseThrow(() -> new IllegalStateException(
                "No Mercado Livre connection found for tenant: " + tenantId
            ));
    }

    /**
     * Check if tenant has an active Mercado Livre connection
     */
    public boolean hasActiveConnection(UUID tenantId) {
        return connectionRepository
            .findByTenantIdAndMarketplace(tenantId, Marketplace.MERCADO_LIVRE.name())
            .map(MarketplaceConnection::isActive)
            .orElse(false);
    }

    /**
     * Upload image to Mercado Livre
     * AC4: Upload de Imagens - Story 5.3
     * POST /pictures with image URL in body
     *
     * @param imageUrl URL of the image to upload
     * @param tenantId Tenant ID for auth
     * @return Picture ID from ML
     */
    public String uploadPicture(String imageUrl, UUID tenantId) {
        try {
            // ML accepts image URL in request body for upload
            // POST https://api.mercadolibre.com/pictures
            // Body: { "source": "http://example.com/image.jpg" }

            MarketplaceConnection connection = getConnection(tenantId);
            String url = ML_API_BASE + "/pictures";
            String decryptedAccessToken = encryptedStringConverter.decrypt(connection.getAccessToken());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(decryptedAccessToken);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            // Create request body with image URL
            java.util.Map<String, String> body = new java.util.HashMap<>();
            body.put("source", imageUrl);

            HttpEntity<java.util.Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            log.debug("Uploading picture to ML: {}", imageUrl);

            ResponseEntity<com.estoquecentral.marketplace.application.dto.ml.MLUploadPictureResponse> response =
                restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    com.estoquecentral.marketplace.application.dto.ml.MLUploadPictureResponse.class
                );

            if (response.getBody() != null && response.getBody().getId() != null) {
                return response.getBody().getId();
            }

            throw new RuntimeException("Failed to upload picture: no ID returned");

        } catch (Exception e) {
            log.error("Error uploading picture to ML: {}", imageUrl, e);
            return null; // Return null to allow publish without images
        }
    }
}
