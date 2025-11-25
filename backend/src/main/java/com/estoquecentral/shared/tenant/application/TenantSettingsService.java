package com.estoquecentral.shared.tenant.application;

import com.estoquecentral.shared.tenant.adapter.out.TenantSettingRepository;
import com.estoquecentral.shared.tenant.domain.TenantSetting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * TenantSettingsService - Manages tenant-specific configuration settings
 * Story 4.6: Stock Reservation and Automatic Release - AC5
 */
@Service
public class TenantSettingsService {

    private final TenantSettingRepository tenantSettingRepository;

    private static final String SALES_ORDER_AUTO_RELEASE_DAYS = "sales_order_auto_release_days";
    private static final int DEFAULT_AUTO_RELEASE_DAYS = 7;

    public TenantSettingsService(TenantSettingRepository tenantSettingRepository) {
        this.tenantSettingRepository = tenantSettingRepository;
    }

    /**
     * Gets the auto-release days setting for sales orders
     *
     * @param tenantId tenant ID
     * @return number of days before auto-releasing reservations (default: 7)
     */
    @Transactional(readOnly = true)
    public int getAutoReleaseDays(UUID tenantId) {
        return tenantSettingRepository.findByTenantIdAndSettingKey(tenantId, SALES_ORDER_AUTO_RELEASE_DAYS)
            .map(setting -> {
                try {
                    return Integer.parseInt(setting.getSettingValue());
                } catch (NumberFormatException e) {
                    return DEFAULT_AUTO_RELEASE_DAYS;
                }
            })
            .orElse(DEFAULT_AUTO_RELEASE_DAYS);
    }

    /**
     * Updates the auto-release days setting for sales orders
     *
     * @param tenantId tenant ID
     * @param days     number of days (must be between 1 and 90)
     */
    @Transactional
    public void updateAutoReleaseDays(UUID tenantId, int days) {
        if (days < 1 || days > 90) {
            throw new IllegalArgumentException("Auto-release days must be between 1 and 90");
        }

        TenantSetting setting = tenantSettingRepository.findByTenantIdAndSettingKey(tenantId, SALES_ORDER_AUTO_RELEASE_DAYS)
            .orElseGet(() -> new TenantSetting(tenantId, SALES_ORDER_AUTO_RELEASE_DAYS, String.valueOf(days)));

        setting.setSettingValue(String.valueOf(days));
        tenantSettingRepository.save(setting);
    }
}
