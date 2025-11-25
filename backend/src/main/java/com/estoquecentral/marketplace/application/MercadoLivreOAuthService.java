package com.estoquecentral.marketplace.application;

import com.estoquecentral.marketplace.adapter.out.MarketplaceConnectionRepository;
import com.estoquecentral.marketplace.application.dto.MercadoLivreUserResponse;
import com.estoquecentral.marketplace.application.dto.TokenResponse;
import com.estoquecentral.marketplace.domain.ConnectionStatus;
import com.estoquecentral.marketplace.domain.EncryptedStringConverter;
import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.MarketplaceConnection;
import com.estoquecentral.shared.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Service for Mercado Livre OAuth2 authentication flow
 * Story 5.1: Mercado Livre OAuth2 Authentication - AC2, AC3, AC4
 */
@Service
public class MercadoLivreOAuthService {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreOAuthService.class);

    private static final String AUTH_URL = "https://auth.mercadolivre.com.br/authorization";
    private static final String TOKEN_URL = "https://api.mercadolibre.com/oauth/token";
    private static final String USER_ME_URL = "https://api.mercadolibre.com/users/me";
    private static final String REVOKE_URL_TEMPLATE = "https://api.mercadolibre.com/applications/%s/tokens/%s";

    @Value("${mercadolivre.app-id:}")
    private String appId;

    @Value("${mercadolivre.secret-key:}")
    private String secretKey;

    @Value("${mercadolivre.redirect-uri:http://localhost:4200/integrations/mercadolivre/callback}")
    private String redirectUri;

    @Value("${encryption.key:}")
    private String encryptionKey;

    private final MarketplaceConnectionRepository connectionRepository;
    private final EncryptedStringConverter encryptedStringConverter;
    private final RestTemplate restTemplate;

    public MercadoLivreOAuthService(
        MarketplaceConnectionRepository connectionRepository,
        EncryptedStringConverter encryptedStringConverter,
        RestTemplate restTemplate
    ) {
        this.connectionRepository = connectionRepository;
        this.encryptedStringConverter = encryptedStringConverter;
        this.restTemplate = restTemplate;
    }

    /**
     * AC2: Generate OAuth2 authorization URL
     * @param tenantId Tenant requesting authorization
     * @return Authorization URL to redirect user
     */
    public String getAuthorizationUrl(UUID tenantId) {
        log.info("Generating authorization URL for tenant: {}", tenantId);

        // Encrypt tenant ID in state parameter for security
        String state = encryptState(tenantId.toString());

        String authUrl = UriComponentsBuilder.fromHttpUrl(AUTH_URL)
            .queryParam("response_type", "code")
            .queryParam("client_id", appId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("state", state)
            .toUriString();

        log.debug("Authorization URL generated: {}", authUrl);
        return authUrl;
    }

    /**
     * AC3: Handle OAuth2 callback and exchange code for tokens
     * @param code Authorization code from Mercado Livre
     * @param state Encrypted tenant ID
     */
    @Transactional
    public void handleCallback(String code, String state) {
        log.info("Handling OAuth2 callback");

        // Decrypt and validate state
        UUID tenantId = UUID.fromString(decryptState(state));
        TenantContext.setTenantId(tenantId.toString());

        log.info("Processing callback for tenant: {}", tenantId);

        try {
            // Exchange code for access token
            TokenResponse tokenResponse = exchangeCodeForToken(code);
            log.debug("Token received. User ID: {}, Expires in: {}s", tokenResponse.getUserId(), tokenResponse.getExpiresIn());

            // Get user details from Mercado Livre
            String userIdMarketplace = String.valueOf(tokenResponse.getUserId());

            // Save or update connection
            MarketplaceConnection connection = connectionRepository
                .findByTenantIdAndMarketplace(tenantId, Marketplace.MERCADO_LIVRE.name())
                .orElse(new MarketplaceConnection());

            connection.setTenantId(tenantId);
            connection.setMarketplace(Marketplace.MERCADO_LIVRE);
            connection.setUserIdMarketplace(userIdMarketplace);

            // Encrypt tokens before saving
            connection.setAccessToken(encryptedStringConverter.encrypt(tokenResponse.getAccessToken()));
            connection.setRefreshToken(encryptedStringConverter.encrypt(tokenResponse.getRefreshToken()));

            connection.setTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
            connection.setStatus(ConnectionStatus.CONNECTED);
            connection.setErrorMessage(null);
            connection.setLastSyncAt(LocalDateTime.now());

            connectionRepository.save(connection);
            log.info("Mercado Livre connection saved successfully for tenant: {}", tenantId);

        } catch (Exception e) {
            log.error("Error handling OAuth2 callback for tenant: {}", tenantId, e);
            throw new RuntimeException("Failed to complete OAuth2 authentication", e);
        }
    }

    /**
     * AC4: Refresh access token using refresh token
     * @param connectionId Connection ID to refresh
     */
    @Transactional
    public void refreshToken(UUID connectionId) {
        log.info("Refreshing token for connection: {}", connectionId);

        MarketplaceConnection connection = connectionRepository.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + connectionId));

        TenantContext.setTenantId(connection.getTenantId().toString());

        try {
            // Decrypt refresh token
            String decryptedRefreshToken = encryptedStringConverter.decrypt(connection.getRefreshToken());

            // Request new token
            TokenResponse tokenResponse = refreshAccessToken(decryptedRefreshToken);
            log.debug("Token refreshed successfully. New expiration: {}s", tokenResponse.getExpiresIn());

            // Update connection with new tokens
            connection.setAccessToken(encryptedStringConverter.encrypt(tokenResponse.getAccessToken()));
            connection.setRefreshToken(encryptedStringConverter.encrypt(tokenResponse.getRefreshToken()));
            connection.setTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
            connection.setStatus(ConnectionStatus.CONNECTED);
            connection.setErrorMessage(null);

            connectionRepository.save(connection);
            log.info("Token refresh completed for connection: {}", connectionId);

        } catch (Exception e) {
            log.error("Error refreshing token for connection: {}", connectionId, e);

            // Update connection status to ERROR
            connection.setStatus(ConnectionStatus.ERROR);
            connection.setErrorMessage("Token refresh failed: " + e.getMessage());
            connectionRepository.save(connection);

            throw new RuntimeException("Failed to refresh token", e);
        }
    }

    /**
     * AC7: Disconnect and revoke tokens
     * @param tenantId Tenant ID
     */
    @Transactional
    public void disconnect(UUID tenantId) {
        log.info("Disconnecting Mercado Livre for tenant: {}", tenantId);

        MarketplaceConnection connection = connectionRepository
            .findByTenantIdAndMarketplace(tenantId, Marketplace.MERCADO_LIVRE.name())
            .orElseThrow(() -> new IllegalArgumentException("No connection found for tenant"));

        try {
            // Revoke tokens in Mercado Livre (optional, but recommended)
            String revokeUrl = String.format(REVOKE_URL_TEMPLATE, appId, connection.getUserIdMarketplace());

            String decryptedAccessToken = encryptedStringConverter.decrypt(connection.getAccessToken());
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(decryptedAccessToken);

            restTemplate.exchange(revokeUrl, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
            log.debug("Tokens revoked successfully in Mercado Livre");

        } catch (Exception e) {
            log.warn("Failed to revoke tokens in Mercado Livre: {}", e.getMessage());
            // Continue with disconnection even if revoke fails
        }

        // Update status to DISCONNECTED
        connection.setStatus(ConnectionStatus.DISCONNECTED);
        connection.setErrorMessage(null);
        connectionRepository.save(connection);

        log.info("Mercado Livre disconnected successfully for tenant: {}", tenantId);
    }

    // Private helper methods

    private TokenResponse exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", appId);
        body.add("client_secret", secretKey);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
            TOKEN_URL,
            request,
            TokenResponse.class
        );

        return response.getBody();
    }

    private TokenResponse refreshAccessToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", appId);
        body.add("client_secret", secretKey);
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
            TOKEN_URL,
            request,
            TokenResponse.class
        );

        return response.getBody();
    }

    /**
     * Encrypt state parameter (tenant ID) for OAuth2 flow
     */
    private String encryptState(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey),
                "AES"
            );

            byte[] iv = new byte[12];
            SecureRandom.getInstanceStrong().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] encrypted = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt state", e);
        }
    }

    /**
     * Decrypt state parameter (tenant ID) from OAuth2 callback
     */
    private String decryptState(String encryptedText) {
        try {
            byte[] encrypted = Base64.getUrlDecoder().decode(encryptedText);

            SecretKeySpec keySpec = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey),
                "AES"
            );

            byte[] iv = new byte[12];
            System.arraycopy(encrypted, 0, iv, 0, iv.length);
            byte[] cipherText = new byte[encrypted.length - iv.length];
            System.arraycopy(encrypted, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt state", e);
        }
    }
}
