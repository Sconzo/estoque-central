package com.estoquecentral.inventory.adapter.out;

import com.estoquecentral.inventory.domain.InventoryMovement;
import com.estoquecentral.inventory.domain.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * InventoryMovementRepository - Data access for InventoryMovement entity
 *
 * <p>Provides queries for movement history and audit trail.
 * All movements are immutable once created.
 *
 * @see InventoryMovement
 */
@Repository
public interface InventoryMovementRepository extends CrudRepository<InventoryMovement, UUID>,
        PagingAndSortingRepository<InventoryMovement, UUID> {

    /**
     * Finds all movements for a product (paginated)
     *
     * @param productId product ID
     * @param pageable pagination parameters
     * @return page of movements
     */
    @Query("SELECT * FROM inventory_movements WHERE product_id = :productId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    java.util.List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(
        @Param("productId") UUID productId,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Query("SELECT COUNT(*) FROM inventory_movements WHERE product_id = :productId")
    long countByProductId(@Param("productId") UUID productId);

    /**
     * Finds all movements for a product and location (paginated)
     *
     * @param productId product ID
     * @param location location
     * @param pageable pagination parameters
     * @return page of movements
     */
    @Query("SELECT * FROM inventory_movements WHERE product_id = :productId AND location = :location ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    java.util.List<InventoryMovement> findByProductIdAndLocationOrderByCreatedAtDesc(
        @Param("productId") UUID productId,
        @Param("location") String location,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Query("SELECT COUNT(*) FROM inventory_movements WHERE product_id = :productId AND location = :location")
    long countByProductIdAndLocation(@Param("productId") UUID productId, @Param("location") String location);

    /**
     * Finds movements by type (paginated)
     *
     * @param type movement type
     * @param pageable pagination parameters
     * @return page of movements
     */
    @Query("SELECT * FROM inventory_movements WHERE type = CAST(:type AS VARCHAR) ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    java.util.List<InventoryMovement> findByTypeOrderByCreatedAtDesc(
        @Param("type") String type,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Query("SELECT COUNT(*) FROM inventory_movements WHERE type = CAST(:type AS VARCHAR)")
    long countByType(@Param("type") String type);

    /**
     * Finds movements by date range (paginated)
     *
     * @param startDate start date
     * @param endDate end date
     * @param pageable pagination parameters
     * @return page of movements
     */
    @Query("SELECT * FROM inventory_movements WHERE created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    java.util.List<InventoryMovement> findByCreatedAtBetweenOrderByCreatedAtDesc(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Query("SELECT COUNT(*) FROM inventory_movements WHERE created_at BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Finds movements by reference (e.g., purchase order, sale)
     *
     * @param referenceType reference type
     * @param referenceId reference ID
     * @return list of movements
     */
    @Query("""
        SELECT * FROM inventory_movements
        WHERE reference_type = :referenceType AND reference_id = :referenceId
        ORDER BY created_at DESC
        """)
    List<InventoryMovement> findByReference(@Param("referenceType") String referenceType,
                                             @Param("referenceId") UUID referenceId);

    /**
     * Finds movements by user (paginated)
     *
     * @param userId user ID
     * @param pageable pagination parameters
     * @return page of movements
     */
    @Query("SELECT * FROM inventory_movements WHERE created_by = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    java.util.List<InventoryMovement> findByCreatedByOrderByCreatedAtDesc(
        @Param("userId") UUID userId,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Query("SELECT COUNT(*) FROM inventory_movements WHERE created_by = :userId")
    long countByCreatedBy(@Param("userId") UUID userId);

    /**
     * Finds recent movements (paginated)
     *
     * @param pageable pagination parameters
     * @return page of recent movements
     */
    @Query("SELECT * FROM inventory_movements ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    java.util.List<InventoryMovement> findAllByOrderByCreatedAtDesc(
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Query("SELECT COUNT(*) FROM inventory_movements")
    long countAll();

    /**
     * Counts movements in date range
     *
     * @param startDate start date
     * @param endDate end date
     * @return count of movements
     */
    @Query("""
        SELECT COUNT(*) FROM inventory_movements
        WHERE created_at >= :startDate AND created_at <= :endDate
        """)
    long countByDateRange(@Param("startDate") LocalDateTime startDate,
                          @Param("endDate") LocalDateTime endDate);

    /**
     * Gets total quantity IN by product and date range
     *
     * @param productId product ID
     * @param startDate start date
     * @param endDate end date
     * @return total IN quantity
     */
    @Query("""
        SELECT COALESCE(SUM(quantity), 0)
        FROM inventory_movements
        WHERE product_id = :productId
          AND type = 'IN'
          AND created_at >= :startDate
          AND created_at <= :endDate
        """)
    Double getTotalInQuantity(@Param("productId") UUID productId,
                               @Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    /**
     * Gets total quantity OUT by product and date range
     *
     * @param productId product ID
     * @param startDate start date
     * @param endDate end date
     * @return total OUT quantity
     */
    @Query("""
        SELECT COALESCE(SUM(quantity), 0)
        FROM inventory_movements
        WHERE product_id = :productId
          AND type = 'OUT'
          AND created_at >= :startDate
          AND created_at <= :endDate
        """)
    Double getTotalOutQuantity(@Param("productId") UUID productId,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);
}
