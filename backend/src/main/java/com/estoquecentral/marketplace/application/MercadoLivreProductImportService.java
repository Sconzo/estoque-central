package com.estoquecentral.marketplace.application;

import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductType;
import com.estoquecentral.marketplace.adapter.out.MarketplaceListingRepository;
import com.estoquecentral.marketplace.application.dto.ImportListingsRequest;
import com.estoquecentral.marketplace.application.dto.ImportListingsResponse;
import com.estoquecentral.marketplace.application.dto.ListingPreviewResponse;
import com.estoquecentral.marketplace.application.dto.ml.MLItemResponse;
import com.estoquecentral.marketplace.application.dto.ml.MLItemSearchResponse;
import com.estoquecentral.marketplace.domain.ListingStatus;
import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.MarketplaceListing;
import com.estoquecentral.shared.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for importing products from Mercado Livre
 * Story 5.2: Import Products from Mercado Livre - AC2, AC3, AC4
 */
@Service
public class MercadoLivreProductImportService {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreProductImportService.class);

    private final MercadoLivreApiClient mlApiClient;
    private final MarketplaceListingRepository listingRepository;
    private final ProductRepository productRepository;

    public MercadoLivreProductImportService(
        MercadoLivreApiClient mlApiClient,
        MarketplaceListingRepository listingRepository,
        ProductRepository productRepository
    ) {
        this.mlApiClient = mlApiClient;
        this.listingRepository = listingRepository;
        this.productRepository = productRepository;
    }

    /**
     * AC2: Get listings preview (for user selection)
     * Fetches all user's listings from Mercado Livre and marks which are already imported
     */
    public List<ListingPreviewResponse> getListingsPreview(UUID tenantId) {
        log.info("Fetching listings preview for tenant: {}", tenantId);

        try {
            // Get user's item IDs from ML
            MLItemSearchResponse searchResponse = mlApiClient.get(
                "/users/me/items/search?status=active&limit=50",
                MLItemSearchResponse.class,
                tenantId
            );

            List<ListingPreviewResponse> previews = new ArrayList<>();

            for (String itemId : searchResponse.getResults()) {
                try {
                    // Get basic item info (without full details for performance)
                    MLItemResponse item = mlApiClient.get(
                        "/items/" + itemId,
                        MLItemResponse.class,
                        tenantId
                    );

                    // Check if already imported
                    boolean alreadyImported = listingRepository.existsByTenantIdAndMarketplaceAndListingId(
                        tenantId,
                        Marketplace.MERCADO_LIVRE.name(),
                        itemId
                    );

                    ListingPreviewResponse preview = new ListingPreviewResponse(
                        item.getId(),
                        item.getTitle(),
                        item.getPrice(),
                        item.getAvailableQuantity(),
                        item.getThumbnail(),
                        alreadyImported,
                        item.hasVariations()
                    );

                    previews.add(preview);

                } catch (Exception e) {
                    log.error("Error fetching item {}: {}", itemId, e.getMessage());
                }
            }

            log.info("Found {} listings for tenant {}", previews.size(), tenantId);
            return previews;

        } catch (Exception e) {
            log.error("Error fetching listings preview for tenant {}", tenantId, e);
            throw new RuntimeException("Failed to fetch listings from Mercado Livre", e);
        }
    }

    /**
     * AC3: Import selected listings
     * Creates products and marketplace_listings records
     */
    @Transactional
    public ImportListingsResponse importListings(UUID tenantId, ImportListingsRequest request) {
        log.info("Importing {} listings for tenant: {}", request.getListingIds().size(), tenantId);

        ImportListingsResponse response = new ImportListingsResponse();

        for (String listingId : request.getListingIds()) {
            try {
                // Check if already imported
                if (listingRepository.existsByTenantIdAndMarketplaceAndListingId(
                    tenantId, Marketplace.MERCADO_LIVRE.name(), listingId)) {
                    log.debug("Listing {} already imported, skipping", listingId);
                    response.incrementSkipped();
                    continue;
                }

                // Get full item details from ML
                MLItemResponse mlItem = mlApiClient.get(
                    "/items/" + listingId,
                    MLItemResponse.class,
                    tenantId
                );

                // Import based on type (simple or with variations)
                if (mlItem.hasVariations()) {
                    importProductWithVariations(tenantId, mlItem);
                } else {
                    importSimpleProduct(tenantId, mlItem);
                }

                response.incrementImported();
                log.info("Successfully imported listing: {}", listingId);

            } catch (Exception e) {
                log.error("Error importing listing {}: {}", listingId, e.getMessage(), e);
                response.addError("Failed to import " + listingId + ": " + e.getMessage());
            }
        }

        log.info("Import completed. Imported: {}, Skipped: {}, Errors: {}",
            response.getImported(), response.getSkipped(), response.getErrors().size());

        return response;
    }

    /**
     * Import simple product (no variations)
     */
    private void importSimpleProduct(UUID tenantId, MLItemResponse mlItem) {
        log.debug("Importing simple product: {}", mlItem.getId());

        // Create product
        Product product = new Product();
        product.setTenantId(tenantId);
        product.setName(mlItem.getTitle());
        product.setSku("ML-" + mlItem.getId());
        product.setType(ProductType.SIMPLE);
        product.setAtivo(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        // Create marketplace listing
        MarketplaceListing listing = new MarketplaceListing();
        listing.setTenantId(tenantId);
        listing.setProductId(savedProduct.getId());
        listing.setVariantId(null); // Simple product has no variant
        listing.setMarketplace(Marketplace.MERCADO_LIVRE);
        listing.setListingIdMarketplace(mlItem.getId());
        listing.setTitle(mlItem.getTitle());
        listing.setPrice(mlItem.getPrice());
        listing.setQuantity(mlItem.getAvailableQuantity());
        listing.setStatus(mapMLStatusToListingStatus(mlItem.getStatus()));
        listing.setLastSyncAt(LocalDateTime.now());

        listingRepository.save(listing);

        log.debug("Simple product imported successfully: {}", savedProduct.getId());
    }

    /**
     * AC4: Import product with variations
     * Creates product with VARIANT type and creates variants for each ML variation
     */
    private void importProductWithVariations(UUID tenantId, MLItemResponse mlItem) {
        log.debug("Importing product with variations: {}", mlItem.getId());

        // Create main product (VARIANT type)
        Product product = new Product();
        product.setTenantId(tenantId);
        product.setName(mlItem.getTitle());
        product.setSku("ML-" + mlItem.getId());
        product.setType(ProductType.VARIANT);
        product.setAtivo(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        // Create listing for each variation
        for (MLItemResponse.Variation mlVariation : mlItem.getVariations()) {
            try {
                // Build variant name from attribute combinations
                String variantName = buildVariantName(mlVariation.getAttributeCombinations());

                // Create marketplace listing for this variation
                // Note: In a full implementation, we would create ProductVariant entities
                // For now, we create listings pointing to the main product
                MarketplaceListing listing = new MarketplaceListing();
                listing.setTenantId(tenantId);
                listing.setProductId(savedProduct.getId());
                listing.setVariantId(null); // TODO: Create ProductVariant and set ID
                listing.setMarketplace(Marketplace.MERCADO_LIVRE);
                listing.setListingIdMarketplace(mlItem.getId() + "-" + mlVariation.getId());
                listing.setTitle(mlItem.getTitle() + " - " + variantName);
                listing.setPrice(mlVariation.getPrice());
                listing.setQuantity(mlVariation.getAvailableQuantity());
                listing.setStatus(ListingStatus.ACTIVE);
                listing.setLastSyncAt(LocalDateTime.now());

                listingRepository.save(listing);

            } catch (Exception e) {
                log.error("Error importing variation {}: {}", mlVariation.getId(), e.getMessage());
            }
        }

        log.debug("Product with variations imported successfully: {}", savedProduct.getId());
    }

    /**
     * Build variant name from attribute combinations (e.g., "Azul - M")
     */
    private String buildVariantName(List<MLItemResponse.AttributeCombination> attributeCombinations) {
        if (attributeCombinations == null || attributeCombinations.isEmpty()) {
            return "Variante";
        }

        StringBuilder name = new StringBuilder();
        for (int i = 0; i < attributeCombinations.size(); i++) {
            if (i > 0) {
                name.append(" - ");
            }
            name.append(attributeCombinations.get(i).getValueName());
        }

        return name.toString();
    }

    /**
     * Map Mercado Livre status to our ListingStatus
     */
    private ListingStatus mapMLStatusToListingStatus(String mlStatus) {
        if (mlStatus == null) {
            return ListingStatus.ACTIVE;
        }

        switch (mlStatus.toUpperCase()) {
            case "ACTIVE":
                return ListingStatus.ACTIVE;
            case "PAUSED":
                return ListingStatus.PAUSED;
            case "CLOSED":
                return ListingStatus.CLOSED;
            default:
                return ListingStatus.ACTIVE;
        }
    }
}
