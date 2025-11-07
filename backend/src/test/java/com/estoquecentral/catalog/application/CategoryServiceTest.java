package com.estoquecentral.catalog.application;

import com.estoquecentral.catalog.adapter.out.CategoryRepository;
import com.estoquecentral.catalog.application.CategoryService.CategoryTreeNode;
import com.estoquecentral.catalog.domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private UUID userId;
    private Category rootCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        rootCategory = new Category(
                UUID.randomUUID(),
                "Electronics",
                "Electronic products",
                null
        );

        childCategory = new Category(
                UUID.randomUUID(),
                "Computers",
                "Computer products",
                rootCategory.getId()
        );
    }

    @Test
    @DisplayName("Should list all active categories")
    void shouldListAllActiveCategories() {
        // Given
        when(categoryRepository.findAllActive()).thenReturn(List.of(rootCategory, childCategory));

        // When
        List<Category> categories = categoryService.listAll();

        // Then
        assertThat(categories).hasSize(2);
        verify(categoryRepository, times(1)).findAllActive();
    }

    @Test
    @DisplayName("Should get category by ID")
    void shouldGetCategoryById() {
        // Given
        when(categoryRepository.findById(rootCategory.getId())).thenReturn(Optional.of(rootCategory));

        // When
        Category category = categoryService.getById(rootCategory.getId());

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo("Electronics");
        verify(categoryRepository, times(1)).findById(rootCategory.getId());
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.getById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found");

        verify(categoryRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should create category successfully")
    void shouldCreateCategorySuccessfully() {
        // Given
        when(categoryRepository.existsByIdAndActive(any())).thenReturn(true);
        when(categoryRepository.findByNameAndParentId(anyString(), any())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Category category = categoryService.create("New Category", "Description", rootCategory.getId(), userId);

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo("New Category");
        assertThat(category.getParentId()).isEqualTo(rootCategory.getId());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when creating category with duplicate name")
    void shouldThrowExceptionWhenCreatingDuplicateCategory() {
        // Given
        when(categoryRepository.findByNameAndParentId("Electronics", null))
                .thenReturn(Optional.of(rootCategory));

        // When/Then
        assertThatThrownBy(() -> categoryService.create("Electronics", "Desc", null, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when parent not found")
    void shouldThrowExceptionWhenParentNotFound() {
        // Given
        UUID nonExistentParentId = UUID.randomUUID();
        when(categoryRepository.existsByIdAndActive(nonExistentParentId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> categoryService.create("New Category", "Desc", nonExistentParentId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Parent category not found");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update category successfully")
    void shouldUpdateCategorySuccessfully() {
        // Given
        when(categoryRepository.findById(rootCategory.getId())).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Category updated = categoryService.update(
                rootCategory.getId(),
                "Updated Name",
                "Updated Description",
                null,
                userId
        );

        // Then
        assertThat(updated).isNotNull();
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should prevent circular reference when updating parent")
    void shouldPreventCircularReferenceWhenUpdatingParent() {
        // Given
        when(categoryRepository.findById(rootCategory.getId())).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.existsByIdAndActive(childCategory.getId())).thenReturn(true);
        when(categoryRepository.wouldCreateCycle(rootCategory.getId(), childCategory.getId())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> categoryService.update(
                rootCategory.getId(),
                "Updated",
                "Desc",
                childCategory.getId(),
                userId
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("circular reference");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete category when no children")
    void shouldDeleteCategoryWhenNoChildren() {
        // Given
        when(categoryRepository.findById(childCategory.getId())).thenReturn(Optional.of(childCategory));
        when(categoryRepository.hasChildren(childCategory.getId())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        categoryService.delete(childCategory.getId());

        // Then
        verify(categoryRepository, times(1)).save(argThat(cat -> !cat.getAtivo()));
    }

    @Test
    @DisplayName("Should throw exception when deleting category with children")
    void shouldThrowExceptionWhenDeletingCategoryWithChildren() {
        // Given
        when(categoryRepository.findById(rootCategory.getId())).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.hasChildren(rootCategory.getId())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> categoryService.delete(rootCategory.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("has subcategories");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should build category tree")
    void shouldBuildCategoryTree() {
        // Given
        when(categoryRepository.findAllActive()).thenReturn(List.of(rootCategory, childCategory));

        // When
        List<CategoryTreeNode> tree = categoryService.getTree();

        // Then
        assertThat(tree).hasSize(1); // One root
        assertThat(tree.get(0).getCategory().getName()).isEqualTo("Electronics");
        assertThat(tree.get(0).getChildren()).hasSize(1); // One child
        assertThat(tree.get(0).getChildren().get(0).getCategory().getName()).isEqualTo("Computers");
    }

    @Test
    @DisplayName("Should get root categories")
    void shouldGetRootCategories() {
        // Given
        when(categoryRepository.findRootCategories()).thenReturn(List.of(rootCategory));

        // When
        List<Category> roots = categoryService.getRootCategories();

        // Then
        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getName()).isEqualTo("Electronics");
        verify(categoryRepository, times(1)).findRootCategories();
    }

    @Test
    @DisplayName("Should get children of category")
    void shouldGetChildrenOfCategory() {
        // Given
        when(categoryRepository.findByParentId(rootCategory.getId())).thenReturn(List.of(childCategory));

        // When
        List<Category> children = categoryService.getChildren(rootCategory.getId());

        // Then
        assertThat(children).hasSize(1);
        assertThat(children.get(0).getName()).isEqualTo("Computers");
        verify(categoryRepository, times(1)).findByParentId(rootCategory.getId());
    }
}
