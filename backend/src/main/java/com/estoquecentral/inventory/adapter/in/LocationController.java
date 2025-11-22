package com.estoquecentral.inventory.adapter.in;

import com.estoquecentral.inventory.adapter.in.dto.CreateLocationRequest;
import com.estoquecentral.inventory.adapter.in.dto.LocationResponse;
import com.estoquecentral.inventory.adapter.in.dto.UpdateLocationRequest;
import com.estoquecentral.inventory.application.LocationService;
import com.estoquecentral.inventory.domain.Location;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * LocationController - REST API for stock location management
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/stock-locations - Create location</li>
 *   <li>GET /api/stock-locations - List locations</li>
 *   <li>GET /api/stock-locations/{id} - Get location details</li>
 *   <li>PUT /api/stock-locations/{id} - Update location</li>
 *   <li>DELETE /api/stock-locations/{id} - Soft delete location</li>
 *   <li>GET /api/stock-locations/{id}/has-stock - Check if location has stock</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/stock-locations")
@Tag(name = "Stock Locations", description = "Manage stock storage locations (warehouses, stores, distribution centers)")
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Creates new location
     *
     * POST /api/stock-locations
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Create stock location",
               description = "Creates a new stock location (warehouse, store, etc.)")
    public ResponseEntity<LocationResponse> create(
            @Valid @RequestBody CreateLocationRequest request,
            @RequestParam("tenantId") UUID tenantId,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        Location location = locationService.create(
                tenantId,
                request.getCode(),
                request.getName(),
                request.getType(),
                request.getAddress(),
                request.getManagerId(),
                userId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LocationResponse.from(location));
    }

    /**
     * Lists all locations
     *
     * GET /api/stock-locations
     */
    @GetMapping
    @Operation(summary = "List stock locations",
               description = "Returns list of stock locations (active only by default)")
    public ResponseEntity<List<LocationResponse>> listAll(
            @RequestParam("tenantId") UUID tenantId,
            @RequestParam(value = "includeInactive", defaultValue = "false") boolean includeInactive) {

        List<Location> locations = locationService.listAll(tenantId, includeInactive);

        List<LocationResponse> responses = locations.stream()
                .map(LocationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Gets location details
     *
     * GET /api/stock-locations/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get location details",
               description = "Returns details of a specific location")
    public ResponseEntity<LocationResponse> getById(@PathVariable UUID id) {

        Location location = locationService.getById(id);

        return ResponseEntity.ok(LocationResponse.from(location));
    }

    /**
     * Updates location
     *
     * PUT /api/stock-locations/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Update stock location",
               description = "Updates location information")
    public ResponseEntity<LocationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLocationRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        Location location = locationService.update(
                id,
                request.getName(),
                request.getDescription(),
                request.getAddress(),
                request.getCity(),
                request.getState(),
                request.getPostalCode(),
                request.getCountry(),
                request.getManagerId(),
                userId
        );

        return ResponseEntity.ok(LocationResponse.from(location));
    }

    /**
     * Soft deletes location
     *
     * DELETE /api/stock-locations/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Delete stock location",
               description = "Soft deletes location (marks as inactive). Fails if location has allocated stock.")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        locationService.delete(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Checks if location has allocated stock
     *
     * GET /api/stock-locations/{id}/has-stock
     */
    @GetMapping("/{id}/has-stock")
    @Operation(summary = "Check if location has stock",
               description = "Returns true if location has products with available quantity > 0")
    public ResponseEntity<Boolean> hasStock(@PathVariable UUID id) {

        boolean hasStock = locationService.hasAllocatedStock(id);

        return ResponseEntity.ok(hasStock);
    }
}
