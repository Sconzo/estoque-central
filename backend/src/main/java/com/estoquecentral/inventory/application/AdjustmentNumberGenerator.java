package com.estoquecentral.inventory.application;

import com.estoquecentral.inventory.adapter.out.StockAdjustmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * AdjustmentNumberGenerator - Generates sequential adjustment numbers
 * Format: ADJ-YYYYMM-0001
 * Story 3.5: Stock Adjustment
 */
@Service
public class AdjustmentNumberGenerator {

    private static final String PREFIX = "ADJ-";
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final StockAdjustmentRepository adjustmentRepository;

    public AdjustmentNumberGenerator(StockAdjustmentRepository adjustmentRepository) {
        this.adjustmentRepository = adjustmentRepository;
    }

    /**
     * Generates next adjustment number for tenant
     * Format: ADJ-YYYYMM-0001
     *
     * @param tenantId Tenant ID
     * @return Generated adjustment number
     */
    public String generateAdjustmentNumber(UUID tenantId) {
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(YEAR_MONTH_FORMATTER);

        // Find max adjustment number for this tenant and month
        Optional<String> maxAdjustmentNumber = adjustmentRepository
                .findMaxAdjustmentNumberByTenantAndYearMonth(tenantId, yearMonth);

        int nextSequence = 1;

        if (maxAdjustmentNumber.isPresent()) {
            String lastNumber = maxAdjustmentNumber.get();
            // Extract sequence from ADJ-YYYYMM-0001
            String sequencePart = lastNumber.substring(lastNumber.lastIndexOf('-') + 1);
            nextSequence = Integer.parseInt(sequencePart) + 1;
        }

        // Format: ADJ-202511-0001
        return String.format("%s%s-%04d", PREFIX, yearMonth, nextSequence);
    }
}
