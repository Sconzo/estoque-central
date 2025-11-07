package com.estoquecentral.catalog.adapter.out.variant;

import com.estoquecentral.catalog.domain.variant.ProductVariant;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends CrudRepository<ProductVariant, UUID> {

    @Query("SELECT * FROM product_variants WHERE parent_product_id = :parentId AND ativo = true ORDER BY name")
    List<ProductVariant> findByParentProductId(@Param("parentId") UUID parentId);

    @Query("SELECT * FROM product_variants WHERE tenant_id = :tenantId AND sku = :sku AND ativo = true")
    Optional<ProductVariant> findByTenantIdAndSku(@Param("tenantId") UUID tenantId, @Param("sku") String sku);

    @Query("SELECT * FROM product_variants WHERE id = :id AND ativo = true")
    Optional<ProductVariant> findByIdAndActive(@Param("id") UUID id);

    @Query("SELECT COUNT(*) FROM product_variants WHERE parent_product_id = :parentId AND ativo = true")
    long countByParentProductId(@Param("parentId") UUID parentId);
}
