package com.estoquecentral.catalog.adapter.in.dto;

import com.estoquecentral.catalog.application.CategoryService.CategoryTreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CategoryTreeDTO - Hierarchical category tree representation
 *
 * <p>Response DTO for category tree endpoint. Contains category
 * information with nested children for building tree UI.
 */
public class CategoryTreeDTO {

    private UUID id;
    private String name;
    private String description;
    private UUID parentId;
    private List<CategoryTreeDTO> children;

    public CategoryTreeDTO() {
        this.children = new ArrayList<>();
    }

    public CategoryTreeDTO(UUID id, String name, String description, UUID parentId) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
    }

    /**
     * Converts CategoryTreeNode to DTO
     *
     * @param node tree node
     * @return tree DTO
     */
    public static CategoryTreeDTO fromTreeNode(CategoryTreeNode node) {
        CategoryTreeDTO dto = new CategoryTreeDTO(
                node.getCategory().getId(),
                node.getCategory().getName(),
                node.getCategory().getDescription(),
                node.getCategory().getParentId()
        );

        // Recursively convert children
        dto.children = node.getChildren().stream()
                .map(CategoryTreeDTO::fromTreeNode)
                .collect(Collectors.toList());

        return dto;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public List<CategoryTreeDTO> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryTreeDTO> children) {
        this.children = children;
    }
}
