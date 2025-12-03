package com.estoquecentral.inventory.application;

import com.estoquecentral.inventory.adapter.out.InventoryRepository;
import com.estoquecentral.inventory.adapter.out.LocationRepository;
import com.estoquecentral.inventory.domain.Location;
import com.estoquecentral.inventory.domain.LocationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * LocationService - Business logic for stock location management
 *
 * <p>Handles location operations with comprehensive validation:
 * <ul>
 *   <li>Code uniqueness per tenant</li>
 *   <li>Stock allocation validation before deletion</li>
 *   <li>Soft delete for inactive locations</li>
 * </ul>
 */
@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository,
                          InventoryRepository inventoryRepository) {
        this.locationRepository = locationRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Creates new location
     *
     * @param tenantId tenant ID
     * @param code location code (unique per tenant)
     * @param name location name
     * @param type location type
     * @param address address (optional)
     * @param managerId manager/responsible user ID (optional)
     * @param createdBy user creating the location
     * @return created location
     * @throws IllegalArgumentException if code already exists
     */
    public Location create(UUID tenantId, String code, String name, LocationType type,
                          String address, UUID managerId, UUID createdBy) {
        // Validate code uniqueness
        validateCodeUniqueness(tenantId, code);

        // Create location
        Location location = new Location(tenantId, code, name, type);
        location.setAddress(address);
        location.setManagerId(managerId);
        location.setCreatedBy(createdBy);

        return locationRepository.save(location);
    }

    /**
     * Updates existing location
     *
     * @param id location ID
     * @param name new name
     * @param description new description
     * @param address new address
     * @param city new city
     * @param state new state
     * @param postalCode new postal code
     * @param country new country
     * @param managerId new manager ID
     * @param updatedBy user updating the location
     * @return updated location
     * @throws IllegalArgumentException if location not found
     */
    public Location update(UUID id, String name, String description, String address,
                          String city, String state, String postalCode, String country,
                          UUID managerId, UUID updatedBy) {
        Location location = locationRepository.findByIdAndActive(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + id));

        location.update(name, description, address, city, state, postalCode, country, updatedBy);
        location.setManagerId(managerId);

        return locationRepository.save(location);
    }

    /**
     * Soft deletes location (marks as inactive)
     *
     * @param id location ID
     * @throws IllegalArgumentException if location not found or has allocated stock
     */
    public void delete(UUID id) {
        Location location = locationRepository.findByIdAndActive(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + id));

        // Validate no stock allocated
        if (hasAllocatedStock(id)) {
            throw new IllegalStateException(
                    "Não é possível inativar local com estoque alocado. Transfira o estoque antes.");
        }

        location.setAtivo(false);
        locationRepository.save(location);
    }

    /**
     * Lists all active locations for a tenant
     *
     * @param tenantId tenant ID
     * @return list of active locations
     */
    @Transactional(readOnly = true)
    public List<Location> listAll(UUID tenantId) {
        return locationRepository.findAllByTenantId(tenantId);
    }

    /**
     * Lists all locations for a tenant (including inactive)
     *
     * @param tenantId tenant ID
     * @param includeInactive if true, includes inactive locations
     * @return list of locations
     */
    @Transactional(readOnly = true)
    public List<Location> listAll(UUID tenantId, boolean includeInactive) {
        if (includeInactive) {
            return locationRepository.findAllByTenantIdIncludingInactive(tenantId);
        }
        return listAll(tenantId);
    }

    /**
     * Gets location by ID
     *
     * @param id location ID
     * @return location
     * @throws IllegalArgumentException if location not found
     */
    @Transactional(readOnly = true)
    public Location getById(UUID id) {
        return locationRepository.findByIdAndActive(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + id));
    }

    /**
     * Gets location by code
     *
     * @param tenantId tenant ID
     * @param code location code
     * @return optional location
     */
    @Transactional(readOnly = true)
    public Optional<Location> getByCode(UUID tenantId, String code) {
        return locationRepository.findByTenantIdAndCode(tenantId, code);
    }

    /**
     * Checks if location has allocated stock
     *
     * @param locationId location ID
     * @return true if location has stock with quantity_available > 0
     */
    @Transactional(readOnly = true)
    public boolean hasAllocatedStock(UUID locationId) {
        return inventoryRepository.countByLocationIdWithAvailableStock(locationId) > 0;
    }

    /**
     * Validates code uniqueness per tenant
     *
     * @param tenantId tenant ID
     * @param code location code
     * @throws IllegalArgumentException if code already exists
     */
    private void validateCodeUniqueness(UUID tenantId, String code) {
        Optional<Location> existing = locationRepository.findByTenantIdAndCode(tenantId, code);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Código " + code + " já está em uso");
        }
    }
}
