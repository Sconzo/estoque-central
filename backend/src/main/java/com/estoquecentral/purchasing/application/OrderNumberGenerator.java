package com.estoquecentral.purchasing.application;

import com.estoquecentral.purchasing.adapter.out.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * OrderNumberGenerator - Generates sequential purchase order numbers
 * Story 3.2: Purchase Order Creation
 *
 * Format: PO-YYYYMM-9999
 * Example: PO-202511-0001, PO-202511-0002, ...
 *
 * Sequence resets each month
 */
@Service
public class OrderNumberGenerator {

    private final PurchaseOrderRepository purchaseOrderRepository;

    public OrderNumberGenerator(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    /**
     * Generate next PO number for the current month
     *
     * @param tenantId tenant UUID
     * @return PO number in format PO-YYYYMM-9999
     */
    @Transactional
    public synchronized String generateOrderNumber(UUID tenantId) {
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String prefix = "PO-" + yearMonth + "-";

        // Find maximum PO number for current month
        String maxOrderNumber = purchaseOrderRepository
            .findMaxPoNumberByTenantAndYearMonth(tenantId, yearMonth)
            .orElse(null);

        int nextSequence = 1;
        if (maxOrderNumber != null && maxOrderNumber.startsWith(prefix)) {
            // Extract sequence number from PO-YYYYMM-9999
            String sequencePart = maxOrderNumber.substring(prefix.length());
            nextSequence = Integer.parseInt(sequencePart) + 1;
        }

        // Format with 4-digit zero padding
        return String.format("%s%04d", prefix, nextSequence);
    }

    /**
     * Check if a PO number already exists
     *
     * @param tenantId tenant UUID
     * @param poNumber PO number to check
     * @return true if exists, false otherwise
     */
    public boolean existsPoNumber(UUID tenantId, String poNumber) {
        return purchaseOrderRepository.findByTenantIdAndPoNumber(tenantId, poNumber).isPresent();
    }
}
