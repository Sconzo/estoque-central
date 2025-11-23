package com.estoquecentral.sales.adapter.in;

import com.estoquecentral.sales.adapter.in.dto.CustomerQuickDTO;
import com.estoquecentral.sales.adapter.in.dto.CustomerRequestDTO;
import com.estoquecentral.sales.adapter.in.dto.CustomerResponseDTO;
import com.estoquecentral.sales.application.CustomerService;
import com.estoquecentral.sales.domain.Customer;
import com.estoquecentral.sales.domain.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CustomerController - REST API for Customer management.
 *
 * Story 4.1: Customer Management - AC3 (CRUD Endpoints)
 *
 * Endpoints:
 * - POST /api/customers - Create customer
 * - GET /api/customers - List with filters and pagination
 * - GET /api/customers/{id} - Get by ID
 * - GET /api/customers/search?q={query} - Quick search (autocomplete)
 * - GET /api/customers/default-consumer - Get default consumer
 * - PUT /api/customers/{id} - Update customer
 * - DELETE /api/customers/{id} - Soft delete (mark as inactive)
 */
@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Creates a new customer.
     *
     * @param request the customer data
     * @return the created customer
     */
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(@RequestBody CustomerRequestDTO request) {
        try {
            Customer customer = toEntity(request);
            Customer created = customerService.create(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(CustomerResponseDTO.from(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Lists customers with optional filters and pagination.
     *
     * @param customerType optional customer type filter (INDIVIDUAL or BUSINESS)
     * @param ativo optional active status filter (default: true)
     * @param page page number (default: 0)
     * @param size page size (default: 20, max: 100)
     * @return Page of customers
     */
    @GetMapping
    public ResponseEntity<Page<CustomerResponseDTO>> list(
        @RequestParam(required = false) CustomerType customerType,
        @RequestParam(required = false, defaultValue = "true") Boolean ativo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        // Enforce max page size
        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customers = customerService.findWithFilters(customerType, ativo, pageable);
        Page<CustomerResponseDTO> response = customers.map(CustomerResponseDTO::from);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets a customer by ID.
     *
     * @param id the customer ID
     * @return the customer
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getById(@PathVariable UUID id) {
        try {
            Customer customer = customerService.findById(id);
            return ResponseEntity.ok(CustomerResponseDTO.from(customer));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Quick search for customers (autocomplete).
     * Performance target: < 500ms (NFR3)
     *
     * @param q the search query
     * @return List of matching customers (max 10)
     */
    @GetMapping("/search")
    public ResponseEntity<List<CustomerQuickDTO>> quickSearch(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Customer> customers = customerService.quickSearch(q.trim());
        List<CustomerQuickDTO> response = customers.stream()
            .map(CustomerQuickDTO::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets the default "Consumidor Final" customer for PDV.
     *
     * @return the default consumer customer
     */
    @GetMapping("/default-consumer")
    public ResponseEntity<CustomerResponseDTO> getDefaultConsumer() {
        try {
            Customer customer = customerService.getDefaultConsumer();
            return ResponseEntity.ok(CustomerResponseDTO.from(customer));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Updates an existing customer.
     *
     * @param id the customer ID
     * @param request the updated customer data
     * @return the updated customer
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> update(
        @PathVariable UUID id,
        @RequestBody CustomerRequestDTO request
    ) {
        try {
            Customer customer = toEntity(request);
            Customer updated = customerService.update(id, customer);
            return ResponseEntity.ok(CustomerResponseDTO.from(updated));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Soft deletes a customer (marks as inactive).
     *
     * @param id the customer ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        try {
            customerService.softDelete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ============================================================
    // Private helper methods
    // ============================================================

    private Customer toEntity(CustomerRequestDTO request) {
        Customer customer = new Customer();
        customer.setCustomerType(request.customerType());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setCpf(request.cpf());
        customer.setCompanyName(request.companyName());
        customer.setCnpj(request.cnpj());
        customer.setTradeName(request.tradeName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setMobile(request.mobile());
        customer.setBirthDate(request.birthDate());
        customer.setStateRegistration(request.stateRegistration());
        customer.setCustomerSegment(request.customerSegment());
        customer.setLoyaltyTier(request.loyaltyTier());
        customer.setCreditLimit(request.creditLimit());
        customer.setAcceptsMarketing(request.acceptsMarketing());
        customer.setPreferredLanguage(request.preferredLanguage());
        customer.setNotes(request.notes());
        return customer;
    }
}
