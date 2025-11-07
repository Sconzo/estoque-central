package com.estoquecentral.catalog.application;

import com.estoquecentral.catalog.adapter.out.CategoryRepository;
import com.estoquecentral.catalog.domain.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CategoryService - Business logic for hierarchical category management
 *
 * <p>Handles category operations with tree structure validation:
 * <ul>
 *   <li>Create category with parent validation</li>
 *   <li>Update category (prevents circular references)</li>
 *   <li>Delete category (soft delete with children check)</li>
 *   <li>Build category tree (hierarchical structure)</li>
 *   <li>Get category path/breadcrumb</li>
 * </ul>
 *
 * <p><strong>Multi-tenancy:</strong> All operations are tenant-scoped via
 * TenantContext (set by JwtAuthenticationFilter).
 *
 * @see Category
 * @see CategoryRepository
 */
@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Lists all active categories (flat list)
     *
     * @return list of all active categories
     */
    public List<Category> listAll() {
        logger.debug("Listing all active categories");
        return categoryRepository.findAllActive();
    }

    /**
     * Builds complete category tree
     *
     * @return list of root categories with nested children
     */
    public List<CategoryTreeNode> getTree() {
        logger.debug("Building category tree");

        // Get all active categories
        List<Category> allCategories = categoryRepository.findAllActive();

        // Create map for quick lookup
        Map<UUID, CategoryTreeNode> nodeMap = allCategories.stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        CategoryTreeNode::new
                ));

        // Build tree structure
        List<CategoryTreeNode> roots = new ArrayList<>();

        for (CategoryTreeNode node : nodeMap.values()) {
            if (node.getCategory().isRoot()) {
                roots.add(node);
            } else {
                UUID parentId = node.getCategory().getParentId();
                CategoryTreeNode parent = nodeMap.get(parentId);
                if (parent != null) {
                    parent.addChild(node);
                }
            }
        }

        // Sort roots and children
        roots.sort(Comparator.comparing(n -> n.getCategory().getName()));
        roots.forEach(this::sortChildren);

        logger.debug("Category tree built with {} root categories", roots.size());
        return roots;
    }

    /**
     * Recursively sorts children in tree
     */
    private void sortChildren(CategoryTreeNode node) {
        node.getChildren().sort(Comparator.comparing(n -> n.getCategory().getName()));
        node.getChildren().forEach(this::sortChildren);
    }

    /**
     * Gets category by ID
     *
     * @param id category ID
     * @return category
     * @throws IllegalArgumentException if not found
     */
    public Category getById(UUID id) {
        logger.debug("Getting category by ID: {}", id);

        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Category not found: {}", id);
                    return new IllegalArgumentException("Category not found: " + id);
                });
    }

    /**
     * Gets category path (breadcrumb) from root to category
     *
     * @param categoryId category ID
     * @return list of categories from root to given category
     */
    public List<Category> getPath(UUID categoryId) {
        logger.debug("Getting path for category: {}", categoryId);

        Category category = getById(categoryId);
        List<Category> ancestors = categoryRepository.findAllAncestors(categoryId);

        // Build path: ancestors + current category
        List<Category> path = new ArrayList<>(ancestors);
        path.add(category);

        logger.debug("Category path has {} levels", path.size());
        return path;
    }

    /**
     * Creates new category
     *
     * @param name category name
     * @param description description (optional)
     * @param parentId parent category ID (null for root)
     * @param createdBy user creating the category
     * @return created category
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public Category create(String name, String description, UUID parentId, UUID createdBy) {
        logger.info("Creating category: name={}, parentId={}", name, parentId);

        // Validate name not blank
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        // Validate parent exists if provided
        if (parentId != null && !categoryRepository.existsByIdAndActive(parentId)) {
            throw new IllegalArgumentException("Parent category not found or inactive: " + parentId);
        }

        // Check for duplicate name within same parent
        Optional<Category> existing = categoryRepository.findByNameAndParentId(name, parentId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Category with name '" + name + "' already exists under this parent"
            );
        }

        // Create category
        Category category = new Category(
                UUID.randomUUID(),
                name.trim(),
                description != null ? description.trim() : null,
                parentId
        );
        category.setCreatedBy(createdBy);
        category.setUpdatedBy(createdBy);

        category = categoryRepository.save(category);
        logger.info("Category created successfully: id={}, name={}", category.getId(), category.getName());

        return category;
    }

    /**
     * Updates category
     *
     * @param id category ID
     * @param name new name (optional)
     * @param description new description (optional)
     * @param newParentId new parent ID (optional, null to keep current)
     * @param updatedBy user updating the category
     * @return updated category
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public Category update(UUID id, String name, String description, UUID newParentId, UUID updatedBy) {
        logger.info("Updating category: id={}, name={}, newParentId={}", id, name, newParentId);

        // Get existing category
        Category category = getById(id);

        // Validate name if provided
        if (name != null && !name.isBlank() && !name.equals(category.getName())) {
            // Check for duplicate name
            Optional<Category> duplicate = categoryRepository.findByNameAndParentId(
                    name,
                    newParentId != null ? newParentId : category.getParentId()
            );
            if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                throw new IllegalArgumentException(
                        "Category with name '" + name + "' already exists under this parent"
                );
            }
        }

        // Validate parent change
        if (newParentId != null && !newParentId.equals(category.getParentId())) {
            // Cannot set category as its own parent
            if (newParentId.equals(id)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }

            // Check if new parent exists
            if (!categoryRepository.existsByIdAndActive(newParentId)) {
                throw new IllegalArgumentException("Parent category not found or inactive: " + newParentId);
            }

            // Check for circular reference
            if (categoryRepository.wouldCreateCycle(id, newParentId)) {
                throw new IllegalArgumentException(
                        "Moving category would create a circular reference. " +
                        "New parent cannot be a descendant of this category."
                );
            }

            category.setParentId(newParentId);
        }

        // Update category
        category.update(name, description, updatedBy);
        category = categoryRepository.save(category);

        logger.info("Category updated successfully: id={}", id);
        return category;
    }

    /**
     * Deletes category (soft delete)
     *
     * @param id category ID
     * @throws IllegalArgumentException if category has children or products
     */
    @Transactional
    public void delete(UUID id) {
        logger.info("Deleting category: id={}", id);

        Category category = getById(id);

        // Check if category has children
        if (categoryRepository.hasChildren(id)) {
            throw new IllegalArgumentException(
                    "Cannot delete category that has subcategories. " +
                    "Delete or move subcategories first."
            );
        }

        // TODO: Check if category has products (when Product entity is implemented)
        // if (productRepository.countByCategory(id) > 0) {
        //     throw new IllegalArgumentException("Cannot delete category that has products");
        // }

        // Soft delete
        category.deactivate();
        categoryRepository.save(category);

        logger.info("Category deleted successfully: id={}", id);
    }

    /**
     * Activates previously deactivated category
     *
     * @param id category ID
     * @return activated category
     */
    @Transactional
    public Category activate(UUID id) {
        logger.info("Activating category: id={}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));

        category.activate();
        category = categoryRepository.save(category);

        logger.info("Category activated successfully: id={}", id);
        return category;
    }

    /**
     * Searches categories by name
     *
     * @param searchTerm search term
     * @return list of matching categories
     */
    public List<Category> search(String searchTerm) {
        logger.debug("Searching categories: searchTerm={}", searchTerm);

        if (searchTerm == null || searchTerm.isBlank()) {
            return listAll();
        }

        return categoryRepository.searchByName(searchTerm.trim());
    }

    /**
     * Gets all root categories
     *
     * @return list of root categories
     */
    public List<Category> getRootCategories() {
        logger.debug("Getting root categories");
        return categoryRepository.findRootCategories();
    }

    /**
     * Gets all direct children of a category
     *
     * @param parentId parent category ID
     * @return list of child categories
     */
    public List<Category> getChildren(UUID parentId) {
        logger.debug("Getting children of category: parentId={}", parentId);
        return categoryRepository.findByParentId(parentId);
    }

    /**
     * CategoryTreeNode - Helper class for building category tree
     */
    public static class CategoryTreeNode {
        private final Category category;
        private final List<CategoryTreeNode> children;

        public CategoryTreeNode(Category category) {
            this.category = category;
            this.children = new ArrayList<>();
        }

        public void addChild(CategoryTreeNode child) {
            this.children.add(child);
        }

        public Category getCategory() {
            return category;
        }

        public List<CategoryTreeNode> getChildren() {
            return children;
        }
    }
}
