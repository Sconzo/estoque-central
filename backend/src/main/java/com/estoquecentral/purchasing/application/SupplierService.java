package com.estoquecentral.purchasing.application;

import com.estoquecentral.purchasing.adapter.in.dto.CreateSupplierRequest;
import com.estoquecentral.purchasing.adapter.in.dto.SupplierResponse;
import com.estoquecentral.purchasing.adapter.in.dto.UpdateSupplierRequest;
import com.estoquecentral.purchasing.adapter.out.SupplierRepository;
import com.estoquecentral.purchasing.application.validation.CnpjValidator;
import com.estoquecentral.purchasing.application.validation.CpfValidator;
import com.estoquecentral.purchasing.domain.Supplier;
import com.estoquecentral.purchasing.domain.SupplierStatus;
import com.estoquecentral.purchasing.domain.SupplierType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SupplierService - Business logic for supplier management
 * Story 3.1: Supplier Management
 */
@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    /**
     * Create a new supplier
     */
    @Transactional
    public SupplierResponse createSupplier(UUID tenantId, CreateSupplierRequest request, UUID userId) {
        // Validate supplier type and document
        validateSupplierTypeAndDocument(request);

        // Validate unique CNPJ/CPF per tenant
        validateUniqueCnpjCpf(tenantId, request.getCnpj(), request.getCpf(), null);

        // Check if supplier code already exists
        Optional<Supplier> existing = supplierRepository.findByTenantIdAndSupplierCode(
                tenantId, request.getSupplierCode());
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Supplier code already exists: " + request.getSupplierCode());
        }

        // Create supplier entity
        Supplier supplier = new Supplier();
        supplier.setTenantId(tenantId);
        supplier.setSupplierCode(request.getSupplierCode());
        supplier.setSupplierType(request.getSupplierType());

        // Business details
        supplier.setCompanyName(request.getCompanyName());
        supplier.setTradeName(request.getTradeName());
        supplier.setCnpj(request.getCnpj());

        // Individual details
        supplier.setFirstName(request.getFirstName());
        supplier.setLastName(request.getLastName());
        supplier.setCpf(request.getCpf());

        // Contact
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setMobile(request.getMobile());
        supplier.setWebsite(request.getWebsite());

        // Address
        supplier.setStreet(request.getStreet());
        supplier.setNumber(request.getNumber());
        supplier.setComplement(request.getComplement());
        supplier.setNeighborhood(request.getNeighborhood());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setPostalCode(request.getPostalCode());
        supplier.setCountry(request.getCountry());

        // Fiscal
        supplier.setStateRegistration(request.getStateRegistration());
        supplier.setMunicipalRegistration(request.getMunicipalRegistration());
        supplier.setTaxRegime(request.getTaxRegime());
        supplier.setIcmsTaxpayer(request.getIcmsTaxpayer());

        // Bank
        supplier.setBankName(request.getBankName());
        supplier.setBankCode(request.getBankCode());
        supplier.setBankBranch(request.getBankBranch());
        supplier.setBankAccount(request.getBankAccount());
        supplier.setBankAccountType(request.getBankAccountType());
        supplier.setPixKey(request.getPixKey());

        // Business
        supplier.setPaymentTerms(request.getPaymentTerms());
        supplier.setDefaultPaymentMethod(request.getDefaultPaymentMethod());
        supplier.setCreditLimit(request.getCreditLimit());
        supplier.setAverageDeliveryDays(request.getAverageDeliveryDays());
        supplier.setMinimumOrderValue(request.getMinimumOrderValue());

        // Classification
        supplier.setStatus(SupplierStatus.ACTIVE);
        supplier.setSupplierCategory(request.getSupplierCategory());
        supplier.setRating(request.getRating());
        supplier.setIsPreferred(request.getIsPreferred());

        // Notes
        supplier.setNotes(request.getNotes());
        supplier.setInternalNotes(request.getInternalNotes());

        // Audit
        supplier.setCreatedBy(userId);
        supplier.setUpdatedBy(userId);
        supplier.setCreatedAt(LocalDateTime.now());
        supplier.setUpdatedAt(LocalDateTime.now());
        supplier.setAtivo(true);

        Supplier saved = supplierRepository.save(supplier);
        return SupplierResponse.fromEntity(saved);
    }

    /**
     * Update existing supplier
     */
    @Transactional
    public SupplierResponse updateSupplier(UUID tenantId, UUID supplierId,
                                           UpdateSupplierRequest request, UUID userId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierId));

        // Verify tenant ownership
        if (!supplier.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Supplier does not belong to this tenant");
        }

        // Validate supplier type and document
        validateSupplierTypeAndDocument(request);

        // Validate unique CNPJ/CPF per tenant (excluding current supplier)
        validateUniqueCnpjCpf(tenantId, request.getCnpj(), request.getCpf(), supplierId);

        // Update all fields
        supplier.setSupplierType(request.getSupplierType());
        supplier.setCompanyName(request.getCompanyName());
        supplier.setTradeName(request.getTradeName());
        supplier.setCnpj(request.getCnpj());

        supplier.setFirstName(request.getFirstName());
        supplier.setLastName(request.getLastName());
        supplier.setCpf(request.getCpf());

        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setMobile(request.getMobile());
        supplier.setWebsite(request.getWebsite());

        supplier.setStreet(request.getStreet());
        supplier.setNumber(request.getNumber());
        supplier.setComplement(request.getComplement());
        supplier.setNeighborhood(request.getNeighborhood());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setPostalCode(request.getPostalCode());
        supplier.setCountry(request.getCountry());

        supplier.setStateRegistration(request.getStateRegistration());
        supplier.setMunicipalRegistration(request.getMunicipalRegistration());
        supplier.setTaxRegime(request.getTaxRegime());
        supplier.setIcmsTaxpayer(request.getIcmsTaxpayer());

        supplier.setBankName(request.getBankName());
        supplier.setBankCode(request.getBankCode());
        supplier.setBankBranch(request.getBankBranch());
        supplier.setBankAccount(request.getBankAccount());
        supplier.setBankAccountType(request.getBankAccountType());
        supplier.setPixKey(request.getPixKey());

        supplier.setPaymentTerms(request.getPaymentTerms());
        supplier.setDefaultPaymentMethod(request.getDefaultPaymentMethod());
        supplier.setCreditLimit(request.getCreditLimit());
        supplier.setAverageDeliveryDays(request.getAverageDeliveryDays());
        supplier.setMinimumOrderValue(request.getMinimumOrderValue());

        supplier.setSupplierCategory(request.getSupplierCategory());
        supplier.setRating(request.getRating());
        supplier.setIsPreferred(request.getIsPreferred());

        supplier.setNotes(request.getNotes());
        supplier.setInternalNotes(request.getInternalNotes());

        supplier.setUpdatedBy(userId);
        supplier.setUpdatedAt(LocalDateTime.now());

        Supplier updated = supplierRepository.save(supplier);
        return SupplierResponse.fromEntity(updated);
    }

    /**
     * Get supplier by ID
     */
    public SupplierResponse getSupplierById(UUID tenantId, UUID supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierId));

        if (!supplier.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Supplier does not belong to this tenant");
        }

        return SupplierResponse.fromEntity(supplier);
    }

    /**
     * Get all suppliers for tenant (active only)
     */
    public List<SupplierResponse> getAllSuppliers(UUID tenantId) {
        return supplierRepository.findByTenantId(tenantId).stream()
                .map(SupplierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get suppliers with pagination
     */
    public Page<SupplierResponse> getSuppliers(UUID tenantId, Pageable pageable) {
        return supplierRepository.findByTenantId(tenantId, pageable)
                .map(SupplierResponse::fromEntity);
    }

    /**
     * Search suppliers with filters
     */
    public Page<SupplierResponse> searchSuppliers(UUID tenantId, String searchTerm,
                                                   String status, Boolean ativo, Pageable pageable) {
        return supplierRepository.search(tenantId, searchTerm, status, ativo, pageable)
                .map(SupplierResponse::fromEntity);
    }

    /**
     * Soft delete supplier (mark as inactive)
     */
    @Transactional
    public void deleteSupplier(UUID tenantId, UUID supplierId, UUID userId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierId));

        if (!supplier.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Supplier does not belong to this tenant");
        }

        supplier.setStatus(SupplierStatus.INACTIVE);
        supplier.setAtivo(false);
        supplier.setUpdatedBy(userId);
        supplier.setUpdatedAt(LocalDateTime.now());

        supplierRepository.save(supplier);
    }

    /**
     * Activate supplier
     */
    @Transactional
    public SupplierResponse activateSupplier(UUID tenantId, UUID supplierId, UUID userId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierId));

        if (!supplier.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Supplier does not belong to this tenant");
        }

        supplier.setStatus(SupplierStatus.ACTIVE);
        supplier.setAtivo(true);
        supplier.setUpdatedBy(userId);
        supplier.setUpdatedAt(LocalDateTime.now());

        Supplier updated = supplierRepository.save(supplier);
        return SupplierResponse.fromEntity(updated);
    }

    /**
     * Get preferred suppliers
     */
    public List<SupplierResponse> getPreferredSuppliers(UUID tenantId) {
        return supplierRepository.findPreferredSuppliers(tenantId).stream()
                .map(SupplierResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ============================================================
    // Private Validation Methods
    // ============================================================

    private void validateSupplierTypeAndDocument(CreateSupplierRequest request) {
        if (request.getSupplierType() == SupplierType.BUSINESS) {
            // PJ must have CNPJ
            if (request.getCnpj() == null || request.getCnpj().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "CNPJ is required for BUSINESS supplier type");
            }

            // Validate CNPJ format and check digits
            if (!CnpjValidator.isValid(request.getCnpj())) {
                throw new IllegalArgumentException(
                        "Invalid CNPJ: " + request.getCnpj());
            }

        } else if (request.getSupplierType() == SupplierType.INDIVIDUAL) {
            // PF must have CPF
            if (request.getCpf() == null || request.getCpf().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "CPF is required for INDIVIDUAL supplier type");
            }

            // Validate CPF format and check digits
            if (!CpfValidator.isValid(request.getCpf())) {
                throw new IllegalArgumentException(
                        "Invalid CPF: " + request.getCpf());
            }
        }
    }

    private void validateUniqueCnpjCpf(UUID tenantId, String cnpj, String cpf, UUID excludeSupplierId) {
        // Check CNPJ uniqueness
        if (cnpj != null && !cnpj.trim().isEmpty()) {
            Optional<Supplier> existingCnpj = supplierRepository.findByTenantIdAndCnpj(tenantId, cnpj);
            if (existingCnpj.isPresent() &&
                    (excludeSupplierId == null || !existingCnpj.get().getId().equals(excludeSupplierId))) {
                throw new IllegalArgumentException(
                        "CNPJ already exists for another supplier: " + cnpj);
            }
        }

        // Check CPF uniqueness
        if (cpf != null && !cpf.trim().isEmpty()) {
            Optional<Supplier> existingCpf = supplierRepository.findByTenantIdAndCpf(tenantId, cpf);
            if (existingCpf.isPresent() &&
                    (excludeSupplierId == null || !existingCpf.get().getId().equals(excludeSupplierId))) {
                throw new IllegalArgumentException(
                        "CPF already exists for another supplier: " + cpf);
            }
        }
    }
}
