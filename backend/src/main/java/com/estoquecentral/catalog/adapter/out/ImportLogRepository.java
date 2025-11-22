package com.estoquecentral.catalog.adapter.out;

import com.estoquecentral.catalog.domain.ImportLog;
import com.estoquecentral.catalog.domain.ImportStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ImportLogRepository - Data access for import logs
 */
@Repository
public interface ImportLogRepository extends CrudRepository<ImportLog, UUID> {

    /**
     * Finds all import logs for a specific user, ordered by creation date
     *
     * @param userId user ID
     * @return list of import logs
     */
    @Query("SELECT * FROM import_logs WHERE user_id = :userId ORDER BY created_at DESC")
    List<ImportLog> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    /**
     * Finds import logs by status
     *
     * @param status import status
     * @return list of import logs
     */
    @Query("SELECT * FROM import_logs WHERE status = :status ORDER BY created_at DESC")
    List<ImportLog> findByStatus(@Param("status") String status);

    /**
     * Finds recent import logs for a user (last N records)
     *
     * @param userId user ID
     * @param limit maximum number of records
     * @return list of import logs
     */
    @Query("SELECT * FROM import_logs WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    List<ImportLog> findRecentByUserId(@Param("userId") UUID userId, @Param("limit") int limit);
}
