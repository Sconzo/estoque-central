package com.estoquecentral.marketplace.domain;

/**
 * ConnectionStatus enum - Marketplace connection statuses
 * Story 5.1: Mercado Livre OAuth2 Authentication
 */
public enum ConnectionStatus {
    /**
     * Connection is active and tokens are valid
     */
    CONNECTED,

    /**
     * Connection was manually disconnected by user
     */
    DISCONNECTED,

    /**
     * Connection has an error (token refresh failed, API error, etc)
     */
    ERROR,

    /**
     * Connection is pending authorization (initial state before OAuth callback)
     */
    PENDING
}
