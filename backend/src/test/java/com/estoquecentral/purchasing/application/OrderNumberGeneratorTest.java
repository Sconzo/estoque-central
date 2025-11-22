package com.estoquecentral.purchasing.application;

import com.estoquecentral.purchasing.adapter.out.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderNumberGenerator
 * Story 3.2: Purchase Order Creation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderNumberGenerator Unit Tests")
class OrderNumberGeneratorTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @InjectMocks
    private OrderNumberGenerator orderNumberGenerator;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should generate first PO number for a month as PO-YYYYMM-0001")
    void shouldGenerateFirstPONumber() {
        // Given
        String yearMonth = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));

        when(purchaseOrderRepository.findMaxPoNumberByTenantAndYearMonth(eq(tenantId), eq(yearMonth)))
            .thenReturn(Optional.empty());

        // When
        String poNumber = orderNumberGenerator.generateOrderNumber(tenantId);

        // Then
        assertThat(poNumber).isNotNull();
        assertThat(poNumber).startsWith("PO-" + yearMonth + "-");
        assertThat(poNumber).endsWith("0001");
        assertThat(poNumber).matches("PO-\\d{6}-\\d{4}");
    }

    @Test
    @DisplayName("Should generate sequential PO number")
    void shouldGenerateSequentialPONumber() {
        // Given
        String yearMonth = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        String existingPONumber = "PO-" + yearMonth + "-0042";

        when(purchaseOrderRepository.findMaxPoNumberByTenantAndYearMonth(eq(tenantId), eq(yearMonth)))
            .thenReturn(Optional.of(existingPONumber));

        // When
        String poNumber = orderNumberGenerator.generateOrderNumber(tenantId);

        // Then
        assertThat(poNumber).isEqualTo("PO-" + yearMonth + "-0043");
    }

    @Test
    @DisplayName("Should handle large sequence numbers with proper zero-padding")
    void shouldHandleLargeSequenceNumbers() {
        // Given
        String yearMonth = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        String existingPONumber = "PO-" + yearMonth + "-9999";

        when(purchaseOrderRepository.findMaxPoNumberByTenantAndYearMonth(eq(tenantId), eq(yearMonth)))
            .thenReturn(Optional.of(existingPONumber));

        // When
        String poNumber = orderNumberGenerator.generateOrderNumber(tenantId);

        // Then
        assertThat(poNumber).isEqualTo("PO-" + yearMonth + "-10000");
    }

    @Test
    @DisplayName("Should check if PO number exists")
    void shouldCheckIfPONumberExists() {
        // Given
        String poNumber = "PO-202511-0001";
        when(purchaseOrderRepository.findByTenantIdAndPoNumber(tenantId, poNumber))
            .thenReturn(Optional.of(null)); // Simplified - just checking exists

        // When
        boolean exists = orderNumberGenerator.existsPoNumber(tenantId, poNumber);

        // Then
        assertThat(exists).isTrue();
        verify(purchaseOrderRepository).findByTenantIdAndPoNumber(tenantId, poNumber);
    }

    @Test
    @DisplayName("Should return false when PO number does not exist")
    void shouldReturnFalseWhenPONumberDoesNotExist() {
        // Given
        String poNumber = "PO-202511-0001";
        when(purchaseOrderRepository.findByTenantIdAndPoNumber(tenantId, poNumber))
            .thenReturn(Optional.empty());

        // When
        boolean exists = orderNumberGenerator.existsPoNumber(tenantId, poNumber);

        // Then
        assertThat(exists).isFalse();
    }
}
