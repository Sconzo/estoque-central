package com.estoquecentral.marketplace.application.dto.ml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Mercado Livre Webhook Notification
 * Story 5.5: Import and Process Orders from Mercado Livre - AC2
 *
 * Example payload:
 * {
 *   "resource": "/orders/123456789",
 *   "user_id": 123456,
 *   "topic": "orders_v2",
 *   "application_id": 789012,
 *   "attempts": 1,
 *   "sent": "2021-03-25T10:00:00.000-04:00",
 *   "received": "2021-03-25T10:00:00.000-04:00"
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MLWebhookNotification {

    private String resource;  // e.g., "/orders/123456789"
    private String topic;     // e.g., "orders_v2", "items", "payments"

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("application_id")
    private Long applicationId;

    private Integer attempts;
    private String sent;
    private String received;

    // Getters and Setters

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getReceived() {
        return received;
    }

    public void setReceived(String received) {
        this.received = received;
    }

    /**
     * Extract order ID from resource path
     * resource = "/orders/123456789" -> returns "123456789"
     */
    public String extractOrderId() {
        if (resource == null || !resource.startsWith("/orders/")) {
            return null;
        }
        return resource.substring("/orders/".length());
    }

    /**
     * Check if this is an order notification
     */
    public boolean isOrderNotification() {
        return "orders_v2".equalsIgnoreCase(topic);
    }
}
