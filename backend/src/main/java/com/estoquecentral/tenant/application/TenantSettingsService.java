package com.estoquecentral.tenant.application;

import com.estoquecentral.tenant.adapter.out.TenantSettingRepository;
import com.estoquecentral.tenant.domain.TenantSetting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TenantSettingsService - Business logic for tenant configuration settings
 * Story 4.6: Stock Reservation and Automatic Release
 *
 * <p>Manages tenant-specific configuration settings like auto-release days for sales orders.
 */
@Service
public class TenantSettingsService {

    private static final String SALES_ORDER_AUTO_RELEASE_DAYS_KEY = "sales_order_auto_release_days";
    private static final int DEFAULT_AUTO_RELEASE_DAYS = 7;

    private final TenantSettingRepository tenantSettingRepository;

    public TenantSettingsService(TenantSettingRepository tenantSettingRepository) {
        this.tenantSettingRepository = tenantSettingRepository;
    }

    /**
     * Gets the auto-release days setting for a tenant
     * Returns default value (7) if not configured
     *
     * @param tenantId tenant ID
     * @return number of days before auto-releasing confirmed orders
     */
    @Transactional(readOnly = true)
    public int getAutoReleaseDays(UUID tenantId) {
        Optional<TenantSetting> setting = tenantSettingRepository.findByTenantIdAndSettingKey(
            tenantId,
            SALES_ORDER_AUTO_RELEASE_DAYS_KEY
        );

        if (setting.isPresent()) {
            Integer days = setting.get().getIntValue();
            return days != null ? days : DEFAULT_AUTO_RELEASE_DAYS;
        }

        return DEFAULT_AUTO_RELEASE_DAYS;
    }

    /**
     * Updates the auto-release days setting for a tenant
     *
     * @param tenantId tenant ID
     * @param days number of days (must be positive)
     */
    @Transactional
    public void updateAutoReleaseDays(UUID tenantId, int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Auto-release days must be positive");
        }

        Optional<TenantSetting> existingSetting = tenantSettingRepository.findByTenantIdAndSettingKey(
            tenantId,
            SALES_ORDER_AUTO_RELEASE_DAYS_KEY
        );

        if (existingSetting.isPresent()) {
            // Update existing setting
            TenantSetting setting = existingSetting.get();
            setting.updateValue(String.valueOf(days));
            tenantSettingRepository.save(setting);
        } else {
            // Create new setting
            TenantSetting newSetting = new TenantSetting(
                tenantId,
                SALES_ORDER_AUTO_RELEASE_DAYS_KEY,
                String.valueOf(days)
            );
            tenantSettingRepository.save(newSetting);
        }
    }

    /**
     * Gets a specific setting value
     *
     * @param tenantId tenant ID
     * @param settingKey setting key
     * @return optional setting value
     */
    @Transactional(readOnly = true)
    public Optional<String> getSetting(UUID tenantId, String settingKey) {
        return tenantSettingRepository.findByTenantIdAndSettingKey(tenantId, settingKey)
            .map(TenantSetting::getSettingValue);
    }

    /**
     * Sets a setting value
     *
     * @param tenantId tenant ID
     * @param settingKey setting key
     * @param settingValue setting value
     */
    @Transactional
    public void setSetting(UUID tenantId, String settingKey, String settingValue) {
        if (settingKey == null || settingKey.isBlank()) {
            throw new IllegalArgumentException("Setting key cannot be empty");
        }

        Optional<TenantSetting> existingSetting = tenantSettingRepository.findByTenantIdAndSettingKey(
            tenantId,
            settingKey
        );

        if (existingSetting.isPresent()) {
            TenantSetting setting = existingSetting.get();
            setting.updateValue(settingValue);
            tenantSettingRepository.save(setting);
        } else {
            TenantSetting newSetting = new TenantSetting(tenantId, settingKey, settingValue);
            tenantSettingRepository.save(newSetting);
        }
    }

    /**
     * Gets all settings for a tenant
     *
     * @param tenantId tenant ID
     * @return list of tenant settings
     */
    @Transactional(readOnly = true)
    public List<TenantSetting> getAllSettings(UUID tenantId) {
        return tenantSettingRepository.findAllByTenantId(tenantId);
    }

    /**
     * Deletes a setting
     *
     * @param tenantId tenant ID
     * @param settingKey setting key
     */
    @Transactional
    public void deleteSetting(UUID tenantId, String settingKey) {
        tenantSettingRepository.deleteByTenantIdAndSettingKey(tenantId, settingKey);
    }
}
