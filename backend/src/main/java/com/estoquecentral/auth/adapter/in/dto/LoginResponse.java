package com.estoquecentral.auth.adapter.in.dto;

/**
 * LoginResponse - DTO for login response
 *
 * <p>Response body for POST /api/auth/google/callback endpoint.
 *
 * <p>Contains the JWT token that the frontend should store and include
 * in all subsequent requests.
 *
 * <p>Example:
 * <pre>{@code
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 * }
 * }</pre>
 *
 * @see com.estoquecentral.auth.adapter.in.AuthController
 */
public class LoginResponse {

    private String token;

    public LoginResponse() {
    }

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
