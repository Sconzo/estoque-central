package com.estoquecentral.inventory.application;

import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.application.composite.CompositeProductService;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.inventory.adapter.in.dto.BelowMinimumStockResponse;
import com.estoquecentral.inventory.adapter.in.dto.BomVirtualStockResponse;
import com.estoquecentral.inventory.adapter.in.dto.SetMinimumQuantityRequest;
import com.estoquecentral.inventory.adapter.in.dto.StockByLocationResponse;
import com.estoquecentral.inventory.adapter.in.dto.StockResponse;
import com.estoquecentral.inventory.adapter.out.InventoryRepository;
import com.estoquecentral.inventory.domain.Inventory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * StockService - Business logic for stock queries and management
 * Story 2.7: Multi-Warehouse Stock Control
 */
@Service
@Transactional(readOnly = true)
public class StockService {

    private final InventoryRepository inventoryRepository;
    private final JdbcTemplate jdbcTemplate;
    private final CompositeProductService compositeProductService;
    private final ProductRepository productRepository;

    public StockService(InventoryRepository inventoryRepository,
                       JdbcTemplate jdbcTemplate,
                       CompositeProductService compositeProductService,
                       ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.compositeProductService = compositeProductService;
        this.productRepository = productRepository;
    }

    /**
     * AC2: Get stock by product (aggregated across all locations)
     */
    public StockByLocationResponse getStockByProduct(UUID tenantId, UUID productId) {
        List<Inventory> inventories = inventoryRepository.findAllByTenantIdAndProductId(tenantId, productId);

        if (inventories.isEmpty()) {
            throw new IllegalArgumentException("No stock found for product: " + productId);
        }

        StockByLocationResponse response = new StockByLocationResponse();
        response.setProductId(productId);

        // Aggregate totals
        BigDecimal totalAvailable = BigDecimal.ZERO;
        BigDecimal totalReserved = BigDecimal.ZERO;
        BigDecimal totalForSale = BigDecimal.ZERO;

        List<StockByLocationResponse.LocationStock> locationStocks = new ArrayList<>();

        for (Inventory inv : inventories) {
            totalAvailable = totalAvailable.add(inv.getQuantityAvailable());
            totalReserved = totalReserved.add(inv.getReservedQuantity());
            totalForSale = totalForSale.add(inv.getComputedQuantityForSale());

            StockByLocationResponse.LocationStock locStock = new StockByLocationResponse.LocationStock();
            locStock.setStockLocationId(inv.getLocationId());
            locStock.setQuantityAvailable(inv.getQuantityAvailable());
            locStock.setReservedQuantity(inv.getReservedQuantity());
            locStock.setQuantityForSale(inv.getComputedQuantityForSale());
            locStock.setMinimumQuantity(inv.getMinimumQuantity());
            locStock.setStatus(calculateStatus(inv));

            locationStocks.add(locStock);
        }

        response.setTotalLocations(locationStocks.size());
        response.setTotalQuantityAvailable(totalAvailable);
        response.setTotalReservedQuantity(totalReserved);
        response.setTotalQuantityForSale(totalForSale);
        response.setByLocation(locationStocks);

        // Enrich with product and location names
        enrichWithNames(response, tenantId);

        return response;
    }

    /**
     * AC2: Get stock by variant (aggregated across all locations)
     */
    public StockByLocationResponse getStockByVariant(UUID tenantId, UUID variantId) {
        List<Inventory> inventories = inventoryRepository.findAllByTenantIdAndVariantId(tenantId, variantId);

        if (inventories.isEmpty()) {
            throw new IllegalArgumentException("No stock found for variant: " + variantId);
        }

        StockByLocationResponse response = new StockByLocationResponse();
        response.setVariantId(variantId);

        // Aggregate totals
        BigDecimal totalAvailable = BigDecimal.ZERO;
        BigDecimal totalReserved = BigDecimal.ZERO;
        BigDecimal totalForSale = BigDecimal.ZERO;

        List<StockByLocationResponse.LocationStock> locationStocks = new ArrayList<>();

        for (Inventory inv : inventories) {
            totalAvailable = totalAvailable.add(inv.getQuantityAvailable());
            totalReserved = totalReserved.add(inv.getReservedQuantity());
            totalForSale = totalForSale.add(inv.getComputedQuantityForSale());

            StockByLocationResponse.LocationStock locStock = new StockByLocationResponse.LocationStock();
            locStock.setStockLocationId(inv.getLocationId());
            locStock.setQuantityAvailable(inv.getQuantityAvailable());
            locStock.setReservedQuantity(inv.getReservedQuantity());
            locStock.setQuantityForSale(inv.getComputedQuantityForSale());
            locStock.setMinimumQuantity(inv.getMinimumQuantity());
            locStock.setStatus(calculateStatus(inv));

            locationStocks.add(locStock);
        }

        response.setTotalLocations(locationStocks.size());
        response.setTotalQuantityAvailable(totalAvailable);
        response.setTotalReservedQuantity(totalReserved);
        response.setTotalQuantityForSale(totalForSale);
        response.setByLocation(locationStocks);

        // Enrich with product and location names
        enrichWithNames(response, tenantId);

        return response;
    }

    /**
     * AC2: Get all stock for tenant (with optional filters)
     */
    public List<StockResponse> getAllStock(UUID tenantId, UUID productId, UUID variantId, UUID locationId, Boolean belowMinimum) {
        String sql = "SELECT * FROM v_stock_summary WHERE tenant_id = ?";
        List<Object> params = new ArrayList<>();
        params.add(tenantId);

        if (productId != null) {
            sql += " AND product_id = ?";
            params.add(productId);
        }

        if (variantId != null) {
            sql += " AND variant_id = ?";
            params.add(variantId);
        }

        if (locationId != null) {
            sql += " AND location_id = ?";
            params.add(locationId);
        }

        if (belowMinimum != null && belowMinimum) {
            sql += " AND stock_status IN ('LOW', 'CRITICAL')";
        }

        sql += " ORDER BY product_name, location_name";

        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            StockResponse response = new StockResponse();
            response.setId(UUID.fromString(rs.getString("id")));
            response.setProductId(rs.getString("product_id") != null ? UUID.fromString(rs.getString("product_id")) : null);
            response.setVariantId(rs.getString("variant_id") != null ? UUID.fromString(rs.getString("variant_id")) : null);
            response.setLocationId(UUID.fromString(rs.getString("location_id")));
            response.setLocationName(rs.getString("location_name"));
            response.setLocationCode(rs.getString("location_code"));
            response.setProductName(rs.getString("product_name"));
            response.setProductSku(rs.getString("sku"));
            response.setQuantityAvailable(rs.getBigDecimal("quantity_available"));
            response.setReservedQuantity(rs.getBigDecimal("reserved_quantity"));
            response.setQuantityForSale(rs.getBigDecimal("quantity_for_sale"));
            response.setMinimumQuantity(rs.getBigDecimal("minimum_quantity"));
            response.setMaximumQuantity(rs.getBigDecimal("maximum_quantity"));
            response.setStockStatus(rs.getString("stock_status"));
            response.setPercentageOfMinimum(rs.getDouble("percentage_of_minimum"));
            return response;
        });
    }

    /**
     * AC5: Set minimum quantity for product/variant at location
     */
    @Transactional
    public void setMinimumQuantity(UUID tenantId, UUID productId, UUID variantId, SetMinimumQuantityRequest request) {
        Inventory inventory;

        if (productId != null) {
            inventory = inventoryRepository.findByTenantIdAndProductIdAndLocationId(
                    tenantId, productId, request.getStockLocationId()
            ).orElseGet(() -> {
                // Create new inventory record if doesn't exist
                Inventory newInv = new Inventory(tenantId, productId, request.getStockLocationId(), BigDecimal.ZERO);
                return inventoryRepository.save(newInv);
            });
        } else if (variantId != null) {
            inventory = inventoryRepository.findByTenantIdAndVariantIdAndLocationId(
                    tenantId, variantId, request.getStockLocationId()
            ).orElseGet(() -> {
                // Create new inventory record if doesn't exist
                Inventory newInv = new Inventory(tenantId, variantId, request.getStockLocationId());
                return inventoryRepository.save(newInv);
            });
        } else {
            throw new IllegalArgumentException("Either productId or variantId must be provided");
        }

        inventory.setLevels(request.getMinimumQuantity(), inventory.getMaximumQuantity());
        inventoryRepository.save(inventory);
    }

    /**
     * AC6: Get products below minimum stock
     */
    public BelowMinimumStockResponse getProductsBelowMinimum(UUID tenantId, UUID locationId) {
        List<Inventory> inventories;

        if (locationId != null) {
            inventories = inventoryRepository.findBelowMinimumByLocation(tenantId, locationId);
        } else {
            inventories = inventoryRepository.findBelowMinimum(tenantId);
        }

        BelowMinimumStockResponse response = new BelowMinimumStockResponse();
        List<BelowMinimumStockResponse.ProductBelowMinimum> products = new ArrayList<>();

        for (Inventory inv : inventories) {
            BelowMinimumStockResponse.ProductBelowMinimum product = new BelowMinimumStockResponse.ProductBelowMinimum();
            product.setProductId(inv.getProductId());
            product.setVariantId(inv.getVariantId());
            product.setStockLocationId(inv.getLocationId());
            product.setQuantityForSale(inv.getComputedQuantityForSale());
            product.setMinimumQuantity(inv.getMinimumQuantity());

            // Calculate percentage
            double percentage = inv.getComputedQuantityForSale()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(inv.getMinimumQuantity(), 2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            product.setPercentageOfMinimum(percentage);

            // Determine severity
            product.setSeverity(percentage < 50 ? "CRITICAL" : "LOW");

            products.add(product);
        }

        // Enrich with names from view
        enrichBelowMinimumWithNames(products, tenantId);

        response.setProducts(products);
        response.setTotalCount(products.size());

        return response;
    }

    /**
     * AC4: Calculate stock for composite products with virtual BOM
     */
    public BomVirtualStockResponse calculateBomVirtualStock(UUID tenantId, UUID productId, UUID locationId) {
        // Get product info
        Product product = productRepository.findByIdAndActive(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        // Delegate to CompositeProductService for calculation
        CompositeProductService.AvailableStockResponse response =
                compositeProductService.calculateAvailableStock(productId, tenantId, locationId);

        // Convert to BomVirtualStockResponse
        return BomVirtualStockResponse.fromCompositeResponse(
                response,
                product.getName(),
                product.getSku()
        );
    }

    // Helper methods

    private String calculateStatus(Inventory inventory) {
        if (inventory.getMinimumQuantity() == null || inventory.getMinimumQuantity().compareTo(BigDecimal.ZERO) == 0) {
            return "NOT_SET";
        }

        BigDecimal forSale = inventory.getComputedQuantityForSale();
        BigDecimal minimum = inventory.getMinimumQuantity();

        if (forSale.compareTo(minimum) >= 0) {
            return "OK";
        } else if (forSale.compareTo(minimum.multiply(BigDecimal.valueOf(0.2))) >= 0) {
            return "LOW";
        } else {
            return "CRITICAL";
        }
    }

    private void enrichWithNames(StockByLocationResponse response, UUID tenantId) {
        // Query to get product/variant and location names
        String sql = """
            SELECT p.name as product_name, p.sku as product_sku, l.name as location_name, l.code as location_code, l.id as location_id
            FROM inventory i
            LEFT JOIN products p ON i.product_id = p.id
            LEFT JOIN product_variants pv ON i.variant_id = pv.id
            LEFT JOIN products parent_p ON pv.parent_product_id = parent_p.id
            INNER JOIN locations l ON i.location_id = l.id
            WHERE i.tenant_id = ?
              AND (i.product_id = ? OR i.variant_id = ?)
            """;

        jdbcTemplate.query(sql, new Object[]{tenantId, response.getProductId(), response.getVariantId()}, rs -> {
            if (response.getProductName() == null) {
                response.setProductName(rs.getString("product_name"));
                response.setProductSku(rs.getString("product_sku"));
            }

            UUID locId = UUID.fromString(rs.getString("location_id"));
            for (StockByLocationResponse.LocationStock loc : response.getByLocation()) {
                if (loc.getStockLocationId().equals(locId)) {
                    loc.setLocationName(rs.getString("location_name"));
                    loc.setLocationCode(rs.getString("location_code"));
                }
            }
        });
    }

    private void enrichBelowMinimumWithNames(List<BelowMinimumStockResponse.ProductBelowMinimum> products, UUID tenantId) {
        String sql = """
            SELECT
                i.id,
                COALESCE(p.name, parent_p.name || ' - ' || pv.sku) as product_name,
                COALESCE(p.sku, pv.sku) as sku,
                l.name as location_name
            FROM inventory i
            LEFT JOIN products p ON i.product_id = p.id
            LEFT JOIN product_variants pv ON i.variant_id = pv.id
            LEFT JOIN products parent_p ON pv.parent_product_id = parent_p.id
            INNER JOIN locations l ON i.location_id = l.id
            WHERE i.tenant_id = ?
              AND i.minimum_quantity IS NOT NULL
              AND i.quantity_for_sale < i.minimum_quantity
            """;

        Map<UUID, Map<String, String>> namesMap = new java.util.HashMap<>();
        jdbcTemplate.query(sql, new Object[]{tenantId}, rs -> {
            UUID id = UUID.fromString(rs.getString("id"));
            Map<String, String> names = new java.util.HashMap<>();
            names.put("product_name", rs.getString("product_name"));
            names.put("sku", rs.getString("sku"));
            names.put("location_name", rs.getString("location_name"));
            namesMap.put(id, names);
        });

        // Match and enrich (simplified approach - in real scenario would use inventory ID)
        for (BelowMinimumStockResponse.ProductBelowMinimum product : products) {
            // Since we don't have inventory ID in the product, we'll query individually
            // This is not optimal but works for the MVP
            String individualSql = """
                SELECT
                    COALESCE(p.name, parent_p.name || ' - ' || pv.sku) as product_name,
                    COALESCE(p.sku, pv.sku) as sku,
                    l.name as location_name
                FROM inventory i
                LEFT JOIN products p ON i.product_id = p.id
                LEFT JOIN product_variants pv ON i.variant_id = pv.id
                LEFT JOIN products parent_p ON pv.parent_product_id = parent_p.id
                INNER JOIN locations l ON i.location_id = l.id
                WHERE i.tenant_id = ?
                  AND i.location_id = ?
                  AND (i.product_id = ? OR i.variant_id = ?)
                """;

            jdbcTemplate.query(individualSql,
                    new Object[]{tenantId, product.getStockLocationId(), product.getProductId(), product.getVariantId()},
                    rs -> {
                        product.setProductName(rs.getString("product_name"));
                        product.setSku(rs.getString("sku"));
                        product.setLocationName(rs.getString("location_name"));
                    });
        }
    }
}
