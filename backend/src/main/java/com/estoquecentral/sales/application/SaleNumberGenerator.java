package com.estoquecentral.sales.application;

import com.estoquecentral.sales.adapter.out.SaleRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Sale Number Generator
 * Generates sale numbers in format: SALE-YYYYMM-9999
 * Story 4.3: NFCe Emission and Stock Decrease
 */
@Component
public class SaleNumberGenerator {
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private final SaleRepository saleRepository;

    public SaleNumberGenerator(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    /**
     * Generate next sale number for the current month
     * @param tenantId Tenant ID
     * @return Sale number in format SALE-YYYYMM-9999
     */
    public String generate(UUID tenantId) {
        String yearMonth = LocalDateTime.now().format(YEAR_MONTH_FORMATTER);
        String pattern = "SALE-" + yearMonth + "-%";

        long count = saleRepository.countBySaleNumberPattern(tenantId, pattern);
        long nextSequence = count + 1;

        return String.format("SALE-%s-%04d", yearMonth, nextSequence);
    }
}
