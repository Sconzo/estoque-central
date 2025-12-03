package com.estoquecentral.inventory.adapter.out;

import com.estoquecentral.inventory.domain.Location;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends CrudRepository<Location, UUID> {

    @Query("SELECT * FROM locations WHERE tenant_id = :tenantId AND ativo = true ORDER BY name")
    List<Location> findAllByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT * FROM locations WHERE tenant_id = :tenantId ORDER BY name")
    List<Location> findAllByTenantIdIncludingInactive(@Param("tenantId") UUID tenantId);

    @Query("SELECT * FROM locations WHERE tenant_id = :tenantId AND code = :code AND ativo = true")
    Optional<Location> findByTenantIdAndCode(@Param("tenantId") UUID tenantId, @Param("code") String code);

    @Query("SELECT * FROM locations WHERE tenant_id = :tenantId AND is_default = true AND ativo = true LIMIT 1")
    Optional<Location> findDefaultLocation(@Param("tenantId") UUID tenantId);

    @Query("SELECT * FROM locations WHERE id = :id AND ativo = true")
    Optional<Location> findByIdAndActive(@Param("id") UUID id);
}
