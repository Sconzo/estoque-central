package com.estoquecentral.sales.application;

import com.estoquecentral.sales.adapter.out.CustomerRepository;
import com.estoquecentral.sales.domain.Customer;
import com.estoquecentral.sales.domain.CustomerType;
import com.estoquecentral.shared.security.CryptoService;
import com.estoquecentral.shared.tenant.TenantContext;
import com.estoquecentral.shared.validator.CnpjValidator;
import com.estoquecentral.shared.validator.CpfValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CustomerService - Business logic for Customer management.
 *
 * Story 4.1: Customer Management
 *
 * Handles:
 * - CRUD operations for customers (PF and PJ)
 * - CPF/CNPJ validation with check digits
 * - AES-256 encryption/decryption of sensitive data
 * - Soft delete (marks as inactive)
 * - Quick search for autocomplete (< 500ms NFR3)
 * - Default "Consumidor Final" customer for PDV
 */
@Service
@Transactional
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final CryptoService cryptoService;

    public CustomerService(CustomerRepository customerRepository, CryptoService cryptoService) {
        this.customerRepository = customerRepository;
        this.cryptoService = cryptoService;
    }

    /**
     * Creates a new customer with validation and encryption.
     *
     * @param customer the customer to create
     * @return the created customer with decrypted fields
     * @throws IllegalArgumentException if validation fails
     */
    public Customer create(Customer customer) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        customer.setTenantId(tenantId);
        customer.setId(UUID.randomUUID());
        customer.markAsNew();
        customer.setAtivo(true);
        customer.setIsDefaultConsumer(false);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        // Validate and encrypt based on customer type
        if (customer.getCustomerType() == CustomerType.INDIVIDUAL) {
            validateIndividualCustomer(customer);
            encryptCustomer(customer);
        } else if (customer.getCustomerType() == CustomerType.BUSINESS) {
            validateBusinessCustomer(customer);
            encryptCustomer(customer);
        } else {
            throw new IllegalArgumentException("Customer type must be INDIVIDUAL or BUSINESS");
        }

        Customer saved = customerRepository.save(customer);
        return decryptCustomer(saved);
    }

    /**
     * Updates an existing customer.
     *
     * @param id the customer ID
     * @param customer the customer data to update
     * @return the updated customer with decrypted fields
     * @throws IllegalArgumentException if customer not found or validation fails
     */
    public Customer update(UUID id, Customer customer) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        Customer existing = customerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));

        // Cannot edit default consumer
        if (Boolean.TRUE.equals(existing.getIsDefaultConsumer())) {
            throw new IllegalArgumentException("Cannot edit default 'Consumidor Final' customer");
        }

        // Validate before updating
        if (customer.getCustomerType() == CustomerType.INDIVIDUAL) {
            validateIndividualCustomer(customer);
        } else if (customer.getCustomerType() == CustomerType.BUSINESS) {
            validateBusinessCustomer(customer);
        }

        // Update fields
        existing.setCustomerType(customer.getCustomerType());
        existing.setFirstName(customer.getFirstName());
        existing.setLastName(customer.getLastName());
        existing.setCpf(customer.getCpf());
        existing.setCompanyName(customer.getCompanyName());
        existing.setCnpj(customer.getCnpj());
        existing.setTradeName(customer.getTradeName());
        existing.setEmail(customer.getEmail());
        existing.setPhone(customer.getPhone());
        existing.setMobile(customer.getMobile());
        existing.setBirthDate(customer.getBirthDate());
        existing.setStateRegistration(customer.getStateRegistration());
        existing.setCustomerSegment(customer.getCustomerSegment());
        existing.setLoyaltyTier(customer.getLoyaltyTier());
        existing.setCreditLimit(customer.getCreditLimit());
        existing.setAcceptsMarketing(customer.getAcceptsMarketing());
        existing.setPreferredLanguage(customer.getPreferredLanguage());
        existing.setNotes(customer.getNotes());
        existing.setUpdatedAt(LocalDateTime.now());

        encryptCustomer(existing);
        Customer saved = customerRepository.save(existing);
        return decryptCustomer(saved);
    }

    /**
     * Finds a customer by ID.
     *
     * @param id the customer ID
     * @return the customer with decrypted fields
     * @throws IllegalArgumentException if customer not found
     */
    @Transactional(readOnly = true)
    public Customer findById(UUID id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        return decryptCustomer(customer);
    }

    /**
     * Finds all active customers with pagination.
     *
     * @param pageable pagination parameters
     * @return Page of customers with decrypted fields
     */
    @Transactional(readOnly = true)
    public Page<Customer> findAll(Pageable pageable) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        List<Customer> content = customerRepository.findByTenantIdPaginated(
            tenantId,
            pageable.getPageSize(),
            pageable.getOffset()
        );
        long total = customerRepository.countByTenantIdAndAtivo(tenantId, true);
        Page<Customer> page = new PageImpl<>(content, pageable, total);
        page.forEach(this::decryptCustomer);
        return page;
    }

    /**
     * Finds customers with filters.
     *
     * @param customerType the customer type filter (optional)
     * @param ativo active status filter (optional, default true)
     * @param pageable pagination parameters
     * @return Page of customers with decrypted fields
     */
    @Transactional(readOnly = true)
    public Page<Customer> findWithFilters(CustomerType customerType, Boolean ativo, Pageable pageable) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());

        List<Customer> content;
        long total;

        if (customerType != null) {
            content = customerRepository.findByTenantIdAndCustomerType(
                tenantId,
                customerType.name(),
                pageable.getPageSize(),
                pageable.getOffset()
            );
            total = customerRepository.countByTenantIdAndCustomerType(tenantId, customerType.name());
        } else if (ativo != null) {
            content = customerRepository.findByTenantIdAndAtivo(
                tenantId,
                ativo,
                pageable.getPageSize(),
                pageable.getOffset()
            );
            total = customerRepository.countByTenantIdAndAtivo(tenantId, ativo);
        } else {
            content = customerRepository.findByTenantIdPaginated(
                tenantId,
                pageable.getPageSize(),
                pageable.getOffset()
            );
            total = customerRepository.countByTenantId(tenantId);
        }

        Page<Customer> page = new PageImpl<>(content, pageable, total);
        page.forEach(this::decryptCustomer);
        return page;
    }

    /**
     * Quick search for customers (used in autocomplete).
     * Performance target: < 500ms (NFR3)
     *
     * @param query the search query
     * @return List of matching customers (max 10)
     */
    @Transactional(readOnly = true)
    public List<Customer> quickSearch(String query) {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        Pageable limit10 = PageRequest.of(0, 10);
        List<Customer> customers = customerRepository.quickSearch(tenantId, query, limit10);
        customers.forEach(this::decryptCustomer);
        return customers;
    }

    /**
     * Soft delete: marks customer as inactive.
     *
     * @param id the customer ID
     * @throws IllegalArgumentException if customer not found or is default consumer
     */
    public void softDelete(UUID id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));

        // Cannot delete default consumer
        if (Boolean.TRUE.equals(customer.getIsDefaultConsumer())) {
            throw new IllegalArgumentException("Cannot delete default 'Consumidor Final' customer");
        }

        customer.setAtivo(false);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    /**
     * Gets the default "Consumidor Final" customer for PDV sales without customer identification.
     *
     * @return the default consumer customer
     * @throws IllegalStateException if default consumer not found
     */
    @Transactional(readOnly = true)
    public Customer getDefaultConsumer() {
        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        return customerRepository.findDefaultConsumer(tenantId)
            .map(this::decryptCustomer)
            .orElseThrow(() -> new IllegalStateException("Default consumer customer not found for tenant"));
    }

    // ============================================================
    // Private validation methods
    // ============================================================

    private void validateIndividualCustomer(Customer customer) {
        if (customer.getFirstName() == null || customer.getFirstName().isEmpty()) {
            throw new IllegalArgumentException("First name is required for individual customers");
        }
        if (customer.getLastName() == null || customer.getLastName().isEmpty()) {
            throw new IllegalArgumentException("Last name is required for individual customers");
        }
        if (customer.getCpf() != null && !customer.getCpf().isEmpty()) {
            validateCpfUnique(customer.getCpf(), customer.getId());
        }
    }

    private void validateBusinessCustomer(Customer customer) {
        if (customer.getCompanyName() == null || customer.getCompanyName().isEmpty()) {
            throw new IllegalArgumentException("Company name is required for business customers");
        }
        if (customer.getCnpj() == null || customer.getCnpj().isEmpty()) {
            throw new IllegalArgumentException("CNPJ is required for business customers");
        }
        validateCnpjUnique(customer.getCnpj(), customer.getId());
    }

    private void validateCpfUnique(String cpf, UUID customerId) {
        String cleanCpf = CpfValidator.cleanFormat(cpf);

        if (!CpfValidator.isValid(cleanCpf)) {
            throw new IllegalArgumentException("Invalid CPF format or check digits");
        }

        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        String encryptedCpf = cryptoService.encrypt(CpfValidator.format(cleanCpf));

        Optional<Customer> existing = customerRepository.findByTenantIdAndCpf(tenantId, encryptedCpf);
        if (existing.isPresent() && !existing.get().getId().equals(customerId)) {
            throw new IllegalArgumentException("CPF already exists for this tenant");
        }
    }

    private void validateCnpjUnique(String cnpj, UUID customerId) {
        String cleanCnpj = CnpjValidator.cleanFormat(cnpj);

        if (!CnpjValidator.isValid(cleanCnpj)) {
            throw new IllegalArgumentException("Invalid CNPJ format or check digits");
        }

        UUID tenantId = UUID.fromString(TenantContext.getTenantId());
        String encryptedCnpj = cryptoService.encrypt(CnpjValidator.format(cleanCnpj));

        Optional<Customer> existing = customerRepository.findByTenantIdAndCnpj(tenantId, encryptedCnpj);
        if (existing.isPresent() && !existing.get().getId().equals(customerId)) {
            throw new IllegalArgumentException("CNPJ already exists for this tenant");
        }
    }

    // ============================================================
    // Private encryption/decryption methods
    // ============================================================

    private void encryptCustomer(Customer customer) {
        if (customer.getCpf() != null && !customer.getCpf().isEmpty()) {
            customer.setCpf(cryptoService.encrypt(customer.getCpf()));
        }
        if (customer.getCnpj() != null && !customer.getCnpj().isEmpty()) {
            customer.setCnpj(cryptoService.encrypt(customer.getCnpj()));
        }
        if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
            customer.setEmail(cryptoService.encrypt(customer.getEmail()));
        }
    }

    private Customer decryptCustomer(Customer customer) {
        if (customer.getCpf() != null && !customer.getCpf().isEmpty()) {
            try {
                customer.setCpf(cryptoService.decrypt(customer.getCpf()));
            } catch (Exception e) {
                logger.warn("Failed to decrypt CPF for customer {}, setting to null. " +
                    "Run migration to fix plain-text encrypted fields.", customer.getId());
                customer.setCpf(null);
            }
        }
        if (customer.getCnpj() != null && !customer.getCnpj().isEmpty()) {
            try {
                customer.setCnpj(cryptoService.decrypt(customer.getCnpj()));
            } catch (Exception e) {
                logger.warn("Failed to decrypt CNPJ for customer {}, setting to null.", customer.getId());
                customer.setCnpj(null);
            }
        }
        if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
            try {
                customer.setEmail(cryptoService.decrypt(customer.getEmail()));
            } catch (Exception e) {
                logger.warn("Failed to decrypt email for customer {}, setting to null.", customer.getId());
                customer.setEmail(null);
            }
        }
        return customer;
    }
}
