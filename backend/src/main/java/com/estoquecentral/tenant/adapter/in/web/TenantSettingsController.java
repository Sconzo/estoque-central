package com.estoquecentral.tenant.adapter.in.web;

import com.estoquecentral.shared.tenant.TenantContext;
import com.estoquecentral.tenant.adapter.in.dto.TenantSettingDTO;
import com.estoquecentral.tenant.adapter.in.dto.UpdateAutoReleaseDaysRequest;
import com.estoquecentral.tenant.application.TenantSettingsService;
import com.estoquecentral.tenant.domain.TenantSetting;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TenantSettingsController - REST API for tenant configuration settings
 * Story 4.6: Stock Reservation and Automatic Release - AC5
 *
 * <p>Endpoints for managing tenant-specific settings like sales order auto-release days.
 */
@RestController
@RequestMapping("/api/settings")
public class TenantSettingsController {

    private final TenantSettingsService tenantSettingsService;

    public TenantSettingsController(TenantSettingsService tenantSettingsService) {
        this.tenantSettingsService = tenantSettingsService;
    }

    /**
     * Get auto-release days setting
     * GET /api/settings/sales-order-release-days
     */
    @GetMapping("/sales-order-release-days")
    public ResponseEntity<Map<String, Integer>> getAutoReleaseDays() {
        UUID tenantId = TenantContext.getTenantId();
        int days = tenantSettingsService.getAutoReleaseDays(tenantId);

        return ResponseEntity.ok(Map.of("days", days));
    }

    /**
     * Update auto-release days setting
     * PUT /api/settings/sales-order-release-days
     *
     * @param request request with new days value
     */
    @PutMapping("/sales-order-release-days")
    public ResponseEntity<Map<String, String>> updateAutoReleaseDays(
            @RequestBody UpdateAutoReleaseDaysRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        tenantSettingsService.updateAutoReleaseDays(tenantId, request.days());

        return ResponseEntity.ok(Map.of(
            "message", "Auto-release days updated successfully",
            "days", String.valueOf(request.days())
        ));
    }

    /**
     * Get all settings for current tenant
     * GET /api/settings
     */
    @GetMapping
    public ResponseEntity<List<TenantSettingDTO>> getAllSettings() {
        UUID tenantId = TenantContext.getTenantId();
        List<TenantSetting> settings = tenantSettingsService.getAllSettings(tenantId);

        List<TenantSettingDTO> dtos = settings.stream()
            .map(s -> new TenantSettingDTO(
                s.getId(),
                s.getTenantId(),
                s.getSettingKey(),
                s.getSettingValue(),
                s.getCreatedAt(),
                s.getUpdatedAt()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get a specific setting value
     * GET /api/settings/{key}
     */
    @GetMapping("/{key}")
    public ResponseEntity<Map<String, String>> getSetting(@PathVariable String key) {
        UUID tenantId = TenantContext.getTenantId();

        return tenantSettingsService.getSetting(tenantId, key)
            .map(value -> ResponseEntity.ok(Map.of("key", key, "value", value)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Set a setting value
     * PUT /api/settings/{key}
     */
    @PutMapping("/{key}")
    public ResponseEntity<Map<String, String>> setSetting(
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        UUID tenantId = TenantContext.getTenantId();
        String value = body.get("value");

        if (value == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Value is required"));
        }

        tenantSettingsService.setSetting(tenantId, key, value);

        return ResponseEntity.ok(Map.of(
            "message", "Setting updated successfully",
            "key", key,
            "value", value
        ));
    }

    /**
     * Delete a setting
     * DELETE /api/settings/{key}
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, String>> deleteSetting(@PathVariable String key) {
        UUID tenantId = TenantContext.getTenantId();

        tenantSettingsService.deleteSetting(tenantId, key);

        return ResponseEntity.ok(Map.of(
            "message", "Setting deleted successfully",
            "key", key
        ));
    }
}
