package com.estoquecentral.marketplace.adapter.in;

import com.estoquecentral.marketplace.adapter.in.dto.SafetyMarginRuleRequest;
import com.estoquecentral.marketplace.adapter.in.dto.SafetyMarginRuleResponse;
import com.estoquecentral.marketplace.application.SafetyMarginService;
import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.SafetyMarginRule;
import com.estoquecentral.shared.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Safety Margin Rules
 * Story 5.7: Configurable Safety Stock Margin - AC3,4,5,6
 */
@RestController
@RequestMapping("/api/safety-margins")
public class SafetyMarginController {

    private final SafetyMarginService safetyMarginService;

    public SafetyMarginController(SafetyMarginService safetyMarginService) {
        this.safetyMarginService = safetyMarginService;
    }

    /**
     * AC3: Create new safety margin rule
     * POST /api/safety-margins
     */
    @PostMapping
    public ResponseEntity<SafetyMarginRuleResponse> createRule(
            @RequestBody SafetyMarginRuleRequest request,
            Authentication authentication) {

        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        UUID userId = authentication != null ? UUID.fromString(authentication.getName()) : null;

        SafetyMarginRule rule = request.toEntity(tenantId, userId);
        SafetyMarginRule created = safetyMarginService.createRule(rule);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SafetyMarginRuleResponse.fromEntity(created));
    }

    /**
     * AC4: List all safety margin rules
     * GET /api/safety-margins?marketplace=MERCADO_LIVRE
     */
    @GetMapping
    public ResponseEntity<List<SafetyMarginRuleResponse>> listRules(
            @RequestParam(required = false) Marketplace marketplace) {

        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        List<SafetyMarginRule> rules = safetyMarginService.listRules(tenantId, marketplace);
        List<SafetyMarginRuleResponse> response = rules.stream()
                .map(SafetyMarginRuleResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * AC5: Update safety margin rule
     * PUT /api/safety-margins/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SafetyMarginRuleResponse> updateRule(
            @PathVariable UUID id,
            @RequestBody UpdateMarginRequest request) {

        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        SafetyMarginRule updated = safetyMarginService.updateRule(tenantId, id, request.marginPercentage);

        return ResponseEntity.ok(SafetyMarginRuleResponse.fromEntity(updated));
    }

    /**
     * AC6: Delete safety margin rule
     * DELETE /api/safety-margins/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        safetyMarginService.deleteRule(tenantId, id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Inner class for update request
     */
    public static class UpdateMarginRequest {
        private BigDecimal marginPercentage;

        public BigDecimal getMarginPercentage() {
            return marginPercentage;
        }

        public void setMarginPercentage(BigDecimal marginPercentage) {
            this.marginPercentage = marginPercentage;
        }
    }
}
