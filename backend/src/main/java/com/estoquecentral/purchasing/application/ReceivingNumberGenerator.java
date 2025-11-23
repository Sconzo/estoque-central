package com.estoquecentral.purchasing.application;

import com.estoquecentral.purchasing.adapter.out.ReceivingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * ReceivingNumberGenerator - Generates sequential receiving numbers
 * Format: RCV-YYYYMM-9999
 * Story 3.4: Receiving Processing
 */
@Service
public class ReceivingNumberGenerator {

    private static final String PREFIX = "RCV-";
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final ReceivingRepository receivingRepository;

    public ReceivingNumberGenerator(ReceivingRepository receivingRepository) {
        this.receivingRepository = receivingRepository;
    }

    /**
     * Generates next receiving number for tenant
     * Format: RCV-YYYYMM-0001
     *
     * @param tenantId Tenant ID
     * @return Generated receiving number
     */
    public String generateReceivingNumber(UUID tenantId) {
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(YEAR_MONTH_FORMATTER);

        // Find max receiving number for this tenant and month
        Optional<String> maxReceivingNumber = receivingRepository
                .findMaxReceivingNumberByTenantAndYearMonth(tenantId, yearMonth);

        int nextSequence = 1;

        if (maxReceivingNumber.isPresent()) {
            String lastNumber = maxReceivingNumber.get();
            // Extract sequence from RCV-YYYYMM-0001
            String sequencePart = lastNumber.substring(lastNumber.lastIndexOf('-') + 1);
            nextSequence = Integer.parseInt(sequencePart) + 1;
        }

        // Format: RCV-202511-0001
        return String.format("%s%s-%04d", PREFIX, yearMonth, nextSequence);
    }
}
