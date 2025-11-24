package com.estoquecentral.sales.application;

import com.estoquecentral.sales.adapter.out.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * SaleNumberGenerator - Generates unique sale numbers per tenant
 * Story 4.3: NFCe Emission and Stock Decrease
 * Format: SALE-YYYYMM-0001 (monthly sequence)
 */
@Service
public class SaleNumberGenerator {

    private final SaleRepository saleRepository;

    public SaleNumberGenerator(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    /**
     * Generate next sale number for tenant
     * Format: SALE-YYYYMM-0001
     * Sequence resets monthly
     */
    public synchronized String generateSaleNumber(UUID tenantId) {
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String prefix = "SALE-" + yearMonth + "-";

        Optional<String> maxNumberOpt = saleRepository.findMaxSaleNumberByTenantAndYearMonth(
                tenantId, "SALE-" + yearMonth
        );

        int nextSequence = 1;
        if (maxNumberOpt.isPresent()) {
            String maxNumber = maxNumberOpt.get();
            String sequencePart = maxNumber.substring(prefix.length());
            nextSequence = Integer.parseInt(sequencePart) + 1;
        }

        return String.format("%s%04d", prefix, nextSequence);
    }
}
