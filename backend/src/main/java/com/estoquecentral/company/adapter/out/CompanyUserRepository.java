package com.estoquecentral.company.adapter.out;

import com.estoquecentral.company.domain.CompanyUser;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CompanyUser associations.
 *
 * @since 1.0
 */
@Repository
public interface CompanyUserRepository extends CrudRepository<CompanyUser, Long> {

    /**
     * Finds all active users (collaborators) for a specific company.
     */
    @Query("""
        SELECT cu.* FROM company_users cu
        WHERE cu.company_id = :companyId AND cu.active = true
        ORDER BY cu.accepted_at DESC
        """)
    List<CompanyUser> findAllActiveByCompanyId(@Param("companyId") Long companyId);

    /**
     * Finds the association between a user and a company.
     */
    @Query("""
        SELECT cu.* FROM company_users cu
        WHERE cu.company_id = :companyId AND cu.user_id = :userId
        """)
    Optional<CompanyUser> findByCompanyIdAndUserId(
        @Param("companyId") Long companyId,
        @Param("userId") Long userId
    );

    /**
     * Checks if a user is already associated with a company.
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM company_users
        WHERE company_id = :companyId AND user_id = :userId
        """)
    boolean existsByCompanyIdAndUserId(
        @Param("companyId") Long companyId,
        @Param("userId") Long userId
    );

    /**
     * Finds all companies for a specific user.
     */
    @Query("""
        SELECT cu.* FROM company_users cu
        WHERE cu.user_id = :userId AND cu.active = true
        ORDER BY cu.accepted_at DESC
        """)
    List<CompanyUser> findAllByUserId(@Param("userId") Long userId);
}
