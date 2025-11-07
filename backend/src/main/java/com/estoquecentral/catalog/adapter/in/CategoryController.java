package com.estoquecentral.catalog.adapter.in;

import com.estoquecentral.catalog.adapter.in.dto.CategoryCreateRequest;
import com.estoquecentral.catalog.adapter.in.dto.CategoryDTO;
import com.estoquecentral.catalog.adapter.in.dto.CategoryTreeDTO;
import com.estoquecentral.catalog.application.CategoryService;
import com.estoquecentral.catalog.application.CategoryService.CategoryTreeNode;
import com.estoquecentral.catalog.domain.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CategoryController - REST API for hierarchical category management
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>GET /api/categories - List all categories</li>
 *   <li>GET /api/categories/tree - Get hierarchical tree</li>
 *   <li>GET /api/categories/{id} - Get category by ID</li>
 *   <li>GET /api/categories/{id}/path - Get category breadcrumb path</li>
 *   <li>POST /api/categories - Create category</li>
 *   <li>PUT /api/categories/{id} - Update category</li>
 *   <li>DELETE /api/categories/{id} - Delete category (soft delete)</li>
 * </ul>
 *
 * <p><strong>Security:</strong> Requires authentication. ADMIN or GERENTE
 * roles required for write operations.
 *
 * @see CategoryService
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Hierarchical product category management")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Lists all active categories (flat list)
     *
     * @return list of categories
     */
    @GetMapping
    @Operation(summary = "List all categories", description = "Returns flat list of all active categories")
    public ResponseEntity<List<CategoryDTO>> listAll() {
        List<Category> categories = categoryService.listAll();

        List<CategoryDTO> dtos = categories.stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Gets category tree (hierarchical structure)
     *
     * @return hierarchical category tree
     */
    @GetMapping("/tree")
    @Operation(summary = "Get category tree", description = "Returns hierarchical tree structure of categories")
    public ResponseEntity<List<CategoryTreeDTO>> getTree() {
        List<CategoryTreeNode> tree = categoryService.getTree();

        List<CategoryTreeDTO> dtos = tree.stream()
                .map(CategoryTreeDTO::fromTreeNode)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Gets category by ID
     *
     * @param id category ID
     * @return category
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Returns category details")
    public ResponseEntity<CategoryDTO> getById(@PathVariable UUID id) {
        Category category = categoryService.getById(id);
        return ResponseEntity.ok(CategoryDTO.fromEntity(category));
    }

    /**
     * Gets category path (breadcrumb)
     *
     * @param id category ID
     * @return list of categories from root to current
     */
    @GetMapping("/{id}/path")
    @Operation(summary = "Get category path", description = "Returns breadcrumb path from root to category")
    public ResponseEntity<List<CategoryDTO>> getPath(@PathVariable UUID id) {
        List<Category> path = categoryService.getPath(id);

        List<CategoryDTO> dtos = path.stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Searches categories by name
     *
     * @param q search query
     * @return list of matching categories
     */
    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Searches categories by name (case-insensitive)")
    public ResponseEntity<List<CategoryDTO>> search(@RequestParam String q) {
        List<Category> categories = categoryService.search(q);

        List<CategoryDTO> dtos = categories.stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Gets root categories (no parent)
     *
     * @return list of root categories
     */
    @GetMapping("/roots")
    @Operation(summary = "Get root categories", description = "Returns all root-level categories")
    public ResponseEntity<List<CategoryDTO>> getRoots() {
        List<Category> roots = categoryService.getRootCategories();

        List<CategoryDTO> dtos = roots.stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Gets children of a category
     *
     * @param id parent category ID
     * @return list of child categories
     */
    @GetMapping("/{id}/children")
    @Operation(summary = "Get category children", description = "Returns direct children of a category")
    public ResponseEntity<List<CategoryDTO>> getChildren(@PathVariable UUID id) {
        List<Category> children = categoryService.getChildren(id);

        List<CategoryDTO> dtos = children.stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Creates new category
     *
     * @param request category creation request
     * @param authentication current user
     * @return created category
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Create category", description = "Creates new category (requires ADMIN or GERENTE role)")
    public ResponseEntity<CategoryDTO> create(
            @Valid @RequestBody CategoryCreateRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        Category category = categoryService.create(
                request.getName(),
                request.getDescription(),
                request.getParentId(),
                userId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryDTO.fromEntity(category));
    }

    /**
     * Updates category
     *
     * @param id category ID
     * @param request update request
     * @param authentication current user
     * @return updated category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Update category", description = "Updates category (requires ADMIN or GERENTE role)")
    public ResponseEntity<CategoryDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryCreateRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        Category category = categoryService.update(
                id,
                request.getName(),
                request.getDescription(),
                request.getParentId(),
                userId
        );

        return ResponseEntity.ok(CategoryDTO.fromEntity(category));
    }

    /**
     * Deletes category (soft delete)
     *
     * @param id category ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Delete category", description = "Soft deletes category (requires ADMIN or GERENTE role)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activates previously deactivated category
     *
     * @param id category ID
     * @return activated category
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate category", description = "Activates deactivated category (requires ADMIN role)")
    public ResponseEntity<CategoryDTO> activate(@PathVariable UUID id) {
        Category category = categoryService.activate(id);
        return ResponseEntity.ok(CategoryDTO.fromEntity(category));
    }
}
