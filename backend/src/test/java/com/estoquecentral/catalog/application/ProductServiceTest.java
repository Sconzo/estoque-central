package com.estoquecentral.catalog.application;

import com.estoquecentral.catalog.adapter.out.CategoryRepository;
import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.domain.Category;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductStatus;
import com.estoquecentral.catalog.domain.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private UUID tenantId;
    private UUID userId;
    private UUID categoryId;
    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        category = new Category(
                categoryId,
                "Electronics",
                "Electronic products",
                null
        );

        product = new Product(
                tenantId,
                ProductType.SIMPLE,
                "Notebook Dell",
                "NOTE-DELL-001",
                "7891234567890",
                "Notebook Dell Inspiron 15",
                categoryId,
                new BigDecimal("4500.00"),
                new BigDecimal("3200.00"),
                "UN",
                true,
                ProductStatus.ACTIVE
        );
        product.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should list all products with pagination")
    void shouldListAllProductsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findAllActive(pageable)).thenReturn(page);

        // When
        Page<Product> result = productService.listAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Notebook Dell");
        verify(productRepository, times(1)).findAllActive(pageable);
    }

    @Test
    @DisplayName("Should get product by ID")
    void shouldGetProductById() {
        // Given
        when(productRepository.findByIdAndActive(product.getId())).thenReturn(Optional.of(product));

        // When
        Product result = productService.getById(product.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Notebook Dell");
        verify(productRepository, times(1)).findByIdAndActive(product.getId());
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(productRepository.findByIdAndActive(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.getById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository, times(1)).findByIdAndActive(nonExistentId);
    }

    @Test
    @DisplayName("Should get product by SKU")
    void shouldGetProductBySku() {
        // Given
        String sku = "NOTE-DELL-001";
        when(productRepository.findByTenantIdAndSku(tenantId, sku)).thenReturn(Optional.of(product));

        // When
        Optional<Product> result = productService.getBySku(tenantId, sku);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSku()).isEqualTo(sku);
        verify(productRepository, times(1)).findByTenantIdAndSku(tenantId, sku);
    }

    @Test
    @DisplayName("Should get product by barcode")
    void shouldGetProductByBarcode() {
        // Given
        String barcode = "7891234567890";
        when(productRepository.findByTenantIdAndBarcode(tenantId, barcode)).thenReturn(Optional.of(product));

        // When
        Optional<Product> result = productService.getByBarcode(tenantId, barcode);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getBarcode()).isEqualTo(barcode);
        verify(productRepository, times(1)).findByTenantIdAndBarcode(tenantId, barcode);
    }

    @Test
    @DisplayName("Should search products by query")
    void shouldSearchProducts() {
        // Given
        String query = "Dell";
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.search(query, pageable)).thenReturn(page);

        // When
        Page<Product> result = productService.search(query, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository, times(1)).search(query, pageable);
    }

    @Test
    @DisplayName("Should find products by category")
    void shouldFindProductsByCategory() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findByCategoryId(categoryId, pageable)).thenReturn(page);

        // When
        Page<Product> result = productService.findByCategory(categoryId, false, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository, times(1)).findByCategoryId(categoryId, pageable);
    }

    @Test
    @DisplayName("Should find products by category including subcategories")
    void shouldFindProductsByCategoryIncludingSubcategories() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findByCategoryIdIncludingDescendants(categoryId, pageable)).thenReturn(page);

        // When
        Page<Product> result = productService.findByCategory(categoryId, true, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository, times(1)).findByCategoryIdIncludingDescendants(categoryId, pageable);
    }

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {
        // Given
        when(productRepository.findByTenantIdAndSku(tenantId, "NOTE-NEW-001")).thenReturn(Optional.empty());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Product result = productService.create(
                tenantId,
                "New Notebook",
                "NOTE-NEW-001",
                "7891234567891",
                "New notebook",
                categoryId,
                new BigDecimal("5000.00"),
                new BigDecimal("3500.00"),
                "UN",
                true,
                userId
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Notebook");
        assertThat(result.getSku()).isEqualTo("NOTE-NEW-001");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when creating product with duplicate SKU")
    void shouldThrowExceptionWhenCreatingDuplicateSku() {
        // Given
        when(productRepository.findByTenantIdAndSku(tenantId, "NOTE-DELL-001"))
                .thenReturn(Optional.of(product));

        // When/Then
        assertThatThrownBy(() -> productService.create(
                tenantId,
                "Another Notebook",
                "NOTE-DELL-001",
                null,
                "Description",
                categoryId,
                new BigDecimal("4000.00"),
                null,
                "UN",
                true,
                userId
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating product with duplicate barcode")
    void shouldThrowExceptionWhenCreatingDuplicateBarcode() {
        // Given
        when(productRepository.findByTenantIdAndSku(tenantId, "NOTE-NEW-001")).thenReturn(Optional.empty());
        when(productRepository.findByTenantIdAndBarcode(tenantId, "7891234567890"))
                .thenReturn(Optional.of(product));

        // When/Then
        assertThatThrownBy(() -> productService.create(
                tenantId,
                "Another Notebook",
                "NOTE-NEW-001",
                "7891234567890",
                "Description",
                categoryId,
                new BigDecimal("4000.00"),
                null,
                "UN",
                true,
                userId
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Barcode already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Given
        UUID nonExistentCategoryId = UUID.randomUUID();
        when(productRepository.findByTenantIdAndSku(tenantId, "NOTE-NEW-001")).thenReturn(Optional.empty());
        when(categoryRepository.findById(nonExistentCategoryId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.create(
                tenantId,
                "New Notebook",
                "NOTE-NEW-001",
                null,
                "Description",
                nonExistentCategoryId,
                new BigDecimal("4000.00"),
                null,
                "UN",
                true,
                userId
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when category is inactive")
    void shouldThrowExceptionWhenCategoryInactive() {
        // Given
        category.deactivate();
        when(productRepository.findByTenantIdAndSku(tenantId, "NOTE-NEW-001")).thenReturn(Optional.empty());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // When/Then
        assertThatThrownBy(() -> productService.create(
                tenantId,
                "New Notebook",
                "NOTE-NEW-001",
                null,
                "Description",
                categoryId,
                new BigDecimal("4000.00"),
                null,
                "UN",
                true,
                userId
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category is not active");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when price is negative")
    void shouldThrowExceptionWhenPriceNegative() {
        // Given
        when(productRepository.findByTenantIdAndSku(tenantId, "NOTE-NEW-001")).thenReturn(Optional.empty());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // When/Then
        assertThatThrownBy(() -> productService.create(
                tenantId,
                "New Notebook",
                "NOTE-NEW-001",
                null,
                "Description",
                categoryId,
                new BigDecimal("-100.00"),
                null,
                "UN",
                true,
                userId
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price must be greater than or equal to 0");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update product successfully")
    void shouldUpdateProductSuccessfully() {
        // Given
        when(productRepository.findByIdAndActive(product.getId())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Product result = productService.update(
                product.getId(),
                "Updated Notebook",
                "Updated description",
                categoryId,
                new BigDecimal("4800.00"),
                new BigDecimal("3300.00"),
                "UN",
                true,
                userId
        );

        // Then
        assertThat(result).isNotNull();
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product status")
    void shouldUpdateProductStatus() {
        // Given
        when(productRepository.findByIdAndActive(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Product result = productService.updateStatus(product.getId(), ProductStatus.INACTIVE, userId);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product (soft delete)")
    void shouldDeleteProduct() {
        // Given
        when(productRepository.findByIdAndActive(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        productService.delete(product.getId());

        // Then
        verify(productRepository, times(1)).save(argThat(p -> !p.getAtivo()));
    }

    @Test
    @DisplayName("Should activate previously deactivated product")
    void shouldActivateProduct() {
        // Given
        product.deactivate();
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Product result = productService.activate(product.getId());

        // Then
        assertThat(result).isNotNull();
        verify(productRepository, times(1)).save(argThat(Product::isActive));
    }

    @Test
    @DisplayName("Should count active products")
    void shouldCountActiveProducts() {
        // Given
        when(productRepository.countActive()).thenReturn(10L);

        // When
        long count = productService.countActive();

        // Then
        assertThat(count).isEqualTo(10L);
        verify(productRepository, times(1)).countActive();
    }

    @Test
    @DisplayName("Should count products by category")
    void shouldCountProductsByCategory() {
        // Given
        when(productRepository.countByCategoryId(categoryId)).thenReturn(5L);

        // When
        long count = productService.countByCategory(categoryId);

        // Then
        assertThat(count).isEqualTo(5L);
        verify(productRepository, times(1)).countByCategoryId(categoryId);
    }

    @Test
    @DisplayName("Should count products by status")
    void shouldCountProductsByStatus() {
        // Given
        when(productRepository.countByStatus("ACTIVE")).thenReturn(8L);

        // When
        long count = productService.countByStatus(ProductStatus.ACTIVE);

        // Then
        assertThat(count).isEqualTo(8L);
        verify(productRepository, times(1)).countByStatus("ACTIVE");
    }
}
