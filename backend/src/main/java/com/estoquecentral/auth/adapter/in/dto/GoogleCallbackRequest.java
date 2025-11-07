package com.estoquecentral.auth.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * GoogleCallbackRequest - DTO for Google OAuth callback
 *
 * <p>Request body for POST /api/auth/google/callback endpoint.
 *
 * <p>This DTO contains the Google ID token received from the frontend
 * and the tenant ID that the user wants to log into.
 *
 * <p>Example:
 * <pre>{@code
 * {
 *   "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE...",
 *   "tenantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
 * }
 * }</pre>
 *
 * @see com.estoquecentral.auth.adapter.in.AuthController
 */
public class GoogleCallbackRequest {

    @NotBlank(message = "Google ID token is required")
    private String idToken;

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    public GoogleCallbackRequest() {
    }

    public GoogleCallbackRequest(String idToken, UUID tenantId) {
        this.idToken = idToken;
        this.tenantId = tenantId;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
}
