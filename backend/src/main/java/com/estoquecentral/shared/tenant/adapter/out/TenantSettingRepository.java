package com.estoquecentral.shared.tenant.adapter.out;

import com.estoquecentral.shared.tenant.domain.TenantSetting;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * TenantSettingRepository - Data access for tenant settings
 * Story 4.6: Stock Reservation and Automatic Release - AC5
 */
@Repository
public interface TenantSettingRepository extends CrudRepository<TenantSetting, UUID> {

    /**
     * Finds a setting by tenant ID and setting key
     *
     * @param tenantId   tenant ID
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
     * Checks if a setting exists for tenant and key
     *
     * @param tenantId   tenant ID
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
     * Deletes a setting by tenant ID and setting key
     *
     * @param tenantId   tenant ID
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
