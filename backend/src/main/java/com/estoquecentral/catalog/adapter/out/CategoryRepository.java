package com.estoquecentral.catalog.adapter.out;

import com.estoquecentral.catalog.domain.Category;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CategoryRepository - Data access for Category entities
 *
 * <p>Provides CRUD operations and specialized queries for hierarchical
 * category tree management. Categories are tenant-specific and stored
 * in tenant schemas.
 *
 * <p><strong>Key Queries:</strong>
 * <ul>
 *   <li>Find all active categories</li>
 *   <li>Find root categories (no parent)</li>
 *   <li>Find children of a category</li>
 *   <li>Find category path (breadcrumb)</li>
 *   <li>Check for circular references</li>
 * </ul>
 *
 * @see Category
 * @see com.estoquecentral.catalog.application.CategoryService
 */
@Repository
public interface CategoryRepository extends CrudRepository<Category, UUID> {

    /**
     * Finds all active categories
     *
     * @return list of active categories
     */
    @Query("SELECT * FROM categories WHERE ativo = true ORDER BY name")
    List<Category> findAllActive();

    /**
     * Finds all root categories (no parent)
     *
     * @return list of root categories
     */
    @Query("SELECT * FROM categories WHERE parent_id IS NULL AND ativo = true ORDER BY name")
    List<Category> findRootCategories();

    /**
     * Finds all direct children of a category
     *
     * @param parentId parent category ID
     * @return list of child categories
     */
    @Query("SELECT * FROM categories WHERE parent_id = :parentId AND ativo = true ORDER BY name")
    List<Category> findByParentId(@Param("parentId") UUID parentId);

    /**
     * Finds category by name and parent
     * Used to check for duplicate names within same parent
     *
     * @param name category name
     * @param parentId parent category ID (can be null)
     * @return category if found
     */
    @Query("""
            SELECT * FROM categories
            WHERE name = :name
            AND (parent_id = :parentId OR (parent_id IS NULL AND :parentId IS NULL))
            """)
    Optional<Category> findByNameAndParentId(@Param("name") String name, @Param("parentId") UUID parentId);

    /**
     * Searches categories by name (case-insensitive)
     *
     * @param name search term
     * @return list of matching categories
     */
    @Query("SELECT * FROM categories WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%')) AND ativo = true ORDER BY name")
    List<Category> searchByName(@Param("name") String name);

    /**
     * Finds active categories by exact name (case-insensitive)
     *
     * @param name exact category name
     * @return list of matching categories (may have duplicates across different parents)
     */
    @Query("SELECT * FROM categories WHERE LOWER(name) = LOWER(:name) AND ativo = true")
    List<Category> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Checks if category has children
     * Used before deletion to prevent deleting parent categories
     *
     * @param categoryId category ID
     * @return true if has children, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM categories WHERE parent_id = :categoryId)")
    boolean hasChildren(@Param("categoryId") UUID categoryId);

    /**
     * Checks if category exists and is active
     *
     * @param id category ID
     * @return true if exists and active
     */
    @Query("SELECT EXISTS(SELECT 1 FROM categories WHERE id = :id AND ativo = true)")
    boolean existsByIdAndActive(@Param("id") UUID id);

    /**
     * Counts all active categories
     *
     * @return count of active categories
     */
    @Query("SELECT COUNT(*) FROM categories WHERE ativo = true")
    long countActive();

    /**
     * Finds all descendants of a category (recursive)
     * Uses PostgreSQL recursive CTE to traverse tree
     *
     * @param categoryId root category ID
     * @return list of all descendant categories
     */
    @Query("""
            WITH RECURSIVE category_tree AS (
                -- Base case: start with given category
                SELECT id, name, parent_id, 0 as depth
                FROM categories
                WHERE id = :categoryId

                UNION ALL

                -- Recursive case: find children
                SELECT c.id, c.name, c.parent_id, ct.depth + 1
                FROM categories c
                INNER JOIN category_tree ct ON c.parent_id = ct.id
                WHERE c.ativo = true
            )
            SELECT c.* FROM categories c
            INNER JOIN category_tree ct ON c.id = ct.id
            WHERE ct.depth > 0
            ORDER BY ct.depth, c.name
            """)
    List<Category> findAllDescendants(@Param("categoryId") UUID categoryId);

    /**
     * Finds all ancestors of a category (path to root)
     * Uses PostgreSQL recursive CTE to traverse tree upwards
     *
     * @param categoryId leaf category ID
     * @return list of ancestor categories (from root to parent)
     */
    @Query("""
            WITH RECURSIVE category_path AS (
                -- Base case: start with given category
                SELECT id, name, parent_id, 0 as depth
                FROM categories
                WHERE id = :categoryId

                UNION ALL

                -- Recursive case: find parent
                SELECT c.id, c.name, c.parent_id, cp.depth + 1
                FROM categories c
                INNER JOIN category_path cp ON c.id = cp.parent_id
            )
            SELECT c.* FROM categories c
            INNER JOIN category_path cp ON c.id = cp.id
            WHERE cp.depth > 0
            ORDER BY cp.depth DESC
            """)
    List<Category> findAllAncestors(@Param("categoryId") UUID categoryId);

    /**
     * Checks if moving category would create a circular reference
     * Returns true if newParentId is a descendant of categoryId
     *
     * @param categoryId category being moved
     * @param newParentId new parent ID
     * @return true if would create cycle, false otherwise
     */
    @Query("""
            WITH RECURSIVE descendants AS (
                SELECT id FROM categories WHERE id = :categoryId
                UNION ALL
                SELECT c.id FROM categories c
                INNER JOIN descendants d ON c.parent_id = d.id
            )
            SELECT EXISTS(SELECT 1 FROM descendants WHERE id = :newParentId)
            """)
    boolean wouldCreateCycle(@Param("categoryId") UUID categoryId, @Param("newParentId") UUID newParentId);
}
