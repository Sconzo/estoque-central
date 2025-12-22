package com.estoquecentral.company.adapter.out;

import com.estoquecentral.company.domain.Company;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Company aggregate.
 *
 * @since 1.0
 */
@Repository
public interface CompanyRepository extends CrudRepository<Company, Long> {

    /**
     * Finds all active companies for a specific user.
     */
    @Query("""
        SELECT c.* FROM companies c
        INNER JOIN company_users cu ON c.id = cu.company_id
        WHERE cu.user_id = :userId AND cu.active = true AND c.active = true
        ORDER BY c.name
        """)
    List<Company> findAllByUserId(@Param("userId") Long userId);

    /**
     * Finds a company by CNPJ.
     */
    Optional<Company> findByCnpj(String cnpj);

    /**
     * Checks if a company with the given CNPJ already exists.
     */
    @Query("SELECT COUNT(*) > 0 FROM companies WHERE cnpj = :cnpj")
    boolean existsByCnpj(@Param("cnpj") String cnpj);

    /**
     * Finds all active companies.
     */
    @Query("SELECT * FROM companies WHERE active = true ORDER BY name")
    List<Company> findAllActive();
}
