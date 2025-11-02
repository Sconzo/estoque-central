package com.estoquecentral.auth.adapter.in;

import com.estoquecentral.auth.adapter.in.dto.CreateTenantRequest;
import com.estoquecentral.auth.adapter.in.dto.TenantDTO;
import com.estoquecentral.auth.application.TenantService;
import com.estoquecentral.auth.domain.Tenant;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TenantController - REST API for tenant management
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/tenants - Create new tenant</li>
 *   <li>GET /api/tenants - List all active tenants</li>
 * </ul>
 *
 * <p><strong>Important:</strong> These endpoints operate on the PUBLIC schema,
 * not tenant schemas. They don't require X-Tenant-ID header.
 *
 * <p>Example usage:
 * <pre>{@code
 * # Create tenant
 * curl -X POST http://localhost:8080/api/tenants \
 *   -H "Content-Type: application/json" \
 *   -d '{"nome": "Empresa ABC", "email": "contato@empresaabc.com"}'
 *
 * # Response:
 * {
 *   "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
 *   "nome": "Empresa ABC",
 *   "schemaName": "tenant_a1b2c3d4e5f67890abcdef1234567890",
 *   "email": "contato@empresaabc.com",
 *   "ativo": true,
 *   "dataCriacao": "2025-01-30T10:15:30Z"
 * }
 * }</pre>
 *
 * @see TenantService
 * @see CreateTenantRequest
 * @see TenantDTO
 */
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    private final TenantService tenantService;

    @Autowired
    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * Creates a new tenant.
     *
     * <p>POST /api/tenants
     *
     * <p>This endpoint:
     * <ol>
     *   <li>Validates request body</li>
     *   <li>Calls TenantService to create tenant</li>
     *   <li>Returns HTTP 201 with tenant details</li>
     * </ol>
     *
     * @param request the tenant creation request
     * @return ResponseEntity with HTTP 201 and TenantDTO
     * @throws IllegalArgumentException if validation fails or email already exists
     */
    @PostMapping
    public ResponseEntity<TenantDTO> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        logger.info("Received request to create tenant: nome={}, email={}", request.getNome(), request.getEmail());

        try {
            Tenant tenant = tenantService.createTenant(request.getNome(), request.getEmail());
            TenantDTO response = TenantDTO.fromEntity(tenant);

            logger.info("Tenant created successfully: id={}, schema={}", tenant.getId(), tenant.getSchemaName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Tenant creation failed - validation error: {}", e.getMessage());
            throw e; // Will be handled by @ControllerAdvice (to be implemented)

        } catch (Exception e) {
            logger.error("Tenant creation failed - unexpected error", e);
            throw new RuntimeException("Failed to create tenant: " + e.getMessage(), e);
        }
    }

    /**
     * Lists all active tenants.
     *
     * <p>GET /api/tenants
     *
     * <p>Returns all tenants with ativo=true, ordered by creation date (newest first).
     *
     * @return ResponseEntity with HTTP 200 and list of TenantDTO
     */
    @GetMapping
    public ResponseEntity<List<TenantDTO>> listTenants() {
        logger.debug("Received request to list all active tenants");

        List<Tenant> tenants = tenantService.getAllActiveTenants();
        List<TenantDTO> response = tenants.stream()
                .map(TenantDTO::fromEntity)
                .collect(Collectors.toList());

        logger.debug("Returning {} active tenants", response.size());
        return ResponseEntity.ok(response);
    }
}
