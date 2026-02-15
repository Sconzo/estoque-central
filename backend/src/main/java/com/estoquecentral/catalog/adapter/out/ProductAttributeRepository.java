package com.estoquecentral.catalog.adapter.out;

import com.estoquecentral.catalog.domain.ProductAttribute;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ProductAttributeRepository - Data access for product descriptive attributes
 */
@Repository
public interface ProductAttributeRepository extends CrudRepository<ProductAttribute, UUID> {

    @Query("SELECT * FROM product_attributes WHERE product_id = :productId ORDER BY sort_order, attribute_key")
    List<ProductAttribute> findByProductId(@Param("productId") UUID productId);

    @Modifying
    @Query("DELETE FROM product_attributes WHERE product_id = :productId")
    void deleteByProductId(@Param("productId") UUID productId);
}
