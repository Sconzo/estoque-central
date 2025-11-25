package com.estoquecentral.marketplace.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MarketplaceConnection entity - OAuth2 connections to marketplace platforms
 * Story 5.1: Mercado Livre OAuth2 Authentication - AC1
 *
 * Stores encrypted access tokens and refresh tokens for marketplace integrations.
 * Tokens are encrypted using AES-256-GCM (NFR14 compliance).
 */
@Table("marketplace_connections")
public class MarketplaceConnection {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("marketplace")
    private Marketplace marketplace;

    @Column("user_id_marketplace")
    private String userIdMarketplace;

    /**
     * Encrypted OAuth2 access token (encrypted via service layer)
     */
    @Column("access_token")
    private String accessToken;

    /**
     * Encrypted OAuth2 refresh token (encrypted via service layer)
     */
    @Column("refresh_token")
    private String refreshToken;

    @Column("token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column("status")
    private ConnectionStatus status;

    @Column("last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column("error_message")
    private String errorMessage;

    @Column("data_criacao")
    private LocalDateTime dataCriacao;

    @Column("data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Constructors

    public MarketplaceConnection() {
        this.status = ConnectionStatus.PENDING;
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    public String getUserIdMarketplace() {
        return userIdMarketplace;
    }

    public void setUserIdMarketplace(String userIdMarketplace) {
        this.userIdMarketplace = userIdMarketplace;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectionStatus status) {
        this.status = status;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    /**
     * Check if token is expired or will expire within the next N minutes
     */
    public boolean isTokenExpiring(int minutesThreshold) {
        if (tokenExpiresAt == null) {
            return true;
        }
        return LocalDateTime.now().plusMinutes(minutesThreshold).isAfter(tokenExpiresAt);
    }

    /**
     * Check if connection is active
     */
    public boolean isActive() {
        return status == ConnectionStatus.CONNECTED && !isTokenExpiring(0);
    }
}
