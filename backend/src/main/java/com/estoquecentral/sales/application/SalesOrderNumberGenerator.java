package com.estoquecentral.sales.application;

import com.estoquecentral.sales.adapter.out.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * SalesOrderNumberGenerator - Generates sequential sales order numbers
 * Story 4.5: Sales Order B2B Interface
 *
 * Format: SO-YYYYMM-9999
 * Example: SO-202511-0001, SO-202511-0002, ...
 *
 * Sequence resets each month
 */
@Service
public class SalesOrderNumberGenerator {

    private final SalesOrderRepository salesOrderRepository;

    public SalesOrderNumberGenerator(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = salesOrderRepository;
    }

    /**
     * Generate next SO number for the current month
     *
     * @param tenantId tenant UUID
     * @return SO number in format SO-YYYYMM-9999
     */
    @Transactional
    public synchronized String generateOrderNumber(UUID tenantId) {
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String prefix = "SO-" + yearMonth + "-";

        // Find maximum SO number for current month
        String maxOrderNumber = salesOrderRepository
            .findMaxOrderNumberByTenantAndYearMonth(tenantId, yearMonth)
            .orElse(null);

        int nextSequence = 1;
        if (maxOrderNumber != null && maxOrderNumber.startsWith(prefix)) {
            // Extract sequence number from SO-YYYYMM-9999
            String sequencePart = maxOrderNumber.substring(prefix.length());
            nextSequence = Integer.parseInt(sequencePart) + 1;
        }

        // Format with 4-digit zero padding
        return String.format("%s%04d", prefix, nextSequence);
    }

    /**
     * Check if a SO number already exists
     *
     * @param tenantId tenant UUID
     * @param orderNumber SO number to check
     * @return true if exists, false otherwise
     */
    public boolean existsOrderNumber(UUID tenantId, String orderNumber) {
        return salesOrderRepository.findByTenantIdAndOrderNumber(tenantId, orderNumber).isPresent();
    }
}
