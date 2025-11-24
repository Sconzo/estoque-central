package com.estoquecentral.tenant.adapter.out;

import com.estoquecentral.tenant.domain.TenantSetting;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TenantSettingRepository - Data access for TenantSetting entity
 * Story 4.6: Stock Reservation and Automatic Release
 *
 * <p>Provides CRUD operations and custom queries for tenant configuration settings.
 */
@Repository
public interface TenantSettingRepository extends CrudRepository<TenantSetting, UUID> {

    /**
     * Finds a specific setting by tenant and key
     *
     * @param tenantId tenant ID
     * @param settingKey setting key
     * @return optional tenant setting
     */
    @Query("""
        SELECT * FROM tenant_settings
        WHERE tenant_id = :tenantId
          AND setting_key = :settingKey
        """)
    Optional<TenantSetting> findByTenantIdAndSettingKey(
        @Param("tenantId") UUID tenantId,
        @Param("settingKey") String settingKey);

    /**
     * Finds all settings for a tenant
     *
     * @param tenantId tenant ID
     * @return list of tenant settings
     */
    @Query("""
        SELECT * FROM tenant_settings
        WHERE tenant_id = :tenantId
        ORDER BY setting_key
        """)
    List<TenantSetting> findAllByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Checks if a setting exists for a tenant
     *
     * @param tenantId tenant ID
     * @param settingKey setting key
     * @return true if exists
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM tenant_settings
            WHERE tenant_id = :tenantId
              AND setting_key = :settingKey
        )
        """)
    boolean existsByTenantIdAndSettingKey(
        @Param("tenantId") UUID tenantId,
        @Param("settingKey") String settingKey);

    /**
     * Deletes a setting by tenant and key
     *
     * @param tenantId tenant ID
     * @param settingKey setting key
     */
    @Query("""
        DELETE FROM tenant_settings
        WHERE tenant_id = :tenantId
          AND setting_key = :settingKey
        """)
    void deleteByTenantIdAndSettingKey(
        @Param("tenantId") UUID tenantId,
        @Param("settingKey") String settingKey);
}
