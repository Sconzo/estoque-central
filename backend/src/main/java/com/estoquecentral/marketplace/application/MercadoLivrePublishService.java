package com.estoquecentral.marketplace.application;

import com.estoquecentral.catalog.adapter.out.ProductRepository;
import com.estoquecentral.catalog.adapter.out.variant.ProductVariantRepository;
import com.estoquecentral.catalog.domain.Product;
import com.estoquecentral.catalog.domain.ProductType;
import com.estoquecentral.catalog.domain.variant.ProductVariant;
import com.estoquecentral.marketplace.adapter.out.MarketplaceListingRepository;
import com.estoquecentral.marketplace.application.dto.CategorySuggestionResponse;
import com.estoquecentral.marketplace.application.dto.PublishProductRequest;
import com.estoquecentral.marketplace.application.dto.PublishProductResponse;
import com.estoquecentral.marketplace.application.dto.ml.*;
import com.estoquecentral.marketplace.domain.ListingStatus;
import com.estoquecentral.marketplace.domain.Marketplace;
import com.estoquecentral.marketplace.domain.MarketplaceListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for publishing products to Mercado Livre
 * Story 5.3: Publish Products to Mercado Livre
 */
@Service
public class MercadoLivrePublishService {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivrePublishService.class);
    private static final String DEFAULT_CATEGORY_ID = "MLB1648"; // Electronics - default category

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final MarketplaceListingRepository listingRepository;
    private final MercadoLivreApiClient apiClient;

    public MercadoLivrePublishService(
        ProductRepository productRepository,
        ProductVariantRepository variantRepository,
        MarketplaceListingRepository listingRepository,
        MercadoLivreApiClient apiClient
    ) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.listingRepository = listingRepository;
        this.apiClient = apiClient;
    }

    /**
     * AC2: Get category suggestion using ML category predictor
     * GET /sites/MLB/category_predictor/predict?title={title}
     */
    public CategorySuggestionResponse suggestCategory(UUID tenantId, String productTitle) {
        log.info("Suggesting category for product: {}", productTitle);

        try {
            String endpoint = UriComponentsBuilder.fromPath("/sites/MLB/category_predictor/predict")
                .queryParam("title", productTitle)
                .toUriString();

            MLCategoryPredictorResponse response = apiClient.get(
                endpoint,
                MLCategoryPredictorResponse.class,
                tenantId
            );

            // Build category path from hierarchy
            String categoryPath = buildCategoryPath(response.getPathFromRoot());

            return new CategorySuggestionResponse(
                response.getId(),
                response.getName(),
                categoryPath
            );

        } catch (Exception e) {
            log.error("Error suggesting category for product: {}", productTitle, e);
            // Return default category on error
            return new CategorySuggestionResponse(
                DEFAULT_CATEGORY_ID,
                "Eletrônicos, Áudio e Vídeo",
                "Eletrônicos, Áudio e Vídeo"
            );
        }
    }

    /**
     * AC1: Publish products to Mercado Livre
     * POST /api/integrations/mercadolivre/publish
     */
    @Transactional
    public PublishProductResponse publishProducts(UUID tenantId, PublishProductRequest request) {
        log.info("Publishing {} products to Mercado Livre for tenant: {}",
            request.getProductIds().size(), tenantId);

        PublishProductResponse response = new PublishProductResponse();

        for (UUID productId : request.getProductIds()) {
            try {
                publishSingleProduct(tenantId, productId);
                response.incrementPublished();

            } catch (Exception e) {
                log.error("Error publishing product: {}", productId, e);

                // Get product name for error message
                String productName = productRepository.findById(productId)
                    .map(Product::getName)
                    .orElse("Unknown");

                response.addError(productId, productName, e.getMessage());
            }
        }

        log.info("Published {} products successfully, {} errors",
            response.getPublished(), response.getErrors().size());

        return response;
    }

    /**
     * Publish a single product to Mercado Livre
     */
    private void publishSingleProduct(UUID tenantId, UUID productId) {
        // 1. Get product from database
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        // Check if already published
        boolean alreadyPublished = listingRepository
            .existsByTenantIdAndProductIdAndMarketplace(
                tenantId,
                productId,
                Marketplace.MERCADO_LIVRE.name()
            );

        if (alreadyPublished) {
            throw new IllegalStateException("Product already published: " + product.getName());
        }

        // 2. Build ML item request based on product type
        MLCreateItemRequest itemRequest;

        if (product.getType() == ProductType.VARIANT_PARENT) {
            // Product with variants
            itemRequest = buildVariantItemRequest(tenantId, product);
        } else {
            // Simple product
            itemRequest = buildSimpleItemRequest(tenantId, product);
        }

        // 3. POST /items to Mercado Livre
        log.info("Creating item in Mercado Livre for product: {}", product.getName());

        MLCreateItemResponse mlResponse = apiClient.post(
            "/items",
            itemRequest,
            MLCreateItemResponse.class,
            tenantId
        );

        log.info("Item created successfully in ML with ID: {}", mlResponse.getId());

        // 4. Save listing(s) in marketplace_listings
        if (itemRequest.hasVariations()) {
            // Save listing for each variant
            saveVariantListings(tenantId, product, mlResponse);
        } else {
            // Save single listing
            saveSimpleListing(tenantId, product, mlResponse);
        }
    }

    /**
     * Build ML item request for simple product (no variants)
     */
    private MLCreateItemRequest buildSimpleItemRequest(UUID tenantId, Product product) {
        MLCreateItemRequest request = new MLCreateItemRequest();

        request.setTitle(product.getName());
        request.setPrice(product.getPrice());
        request.setAvailableQuantity(0); // Stock sync will be done separately (Story 5.4)
        request.setDescription(product.getDescription() != null ? product.getDescription() : product.getName());

        // Get category suggestion
        CategorySuggestionResponse category = suggestCategory(tenantId, product.getName());
        request.setCategoryId(category.getCategoryId());

        // AC4: Add pictures if available
        List<MLPicture> pictures = uploadProductImages(tenantId, product);
        if (!pictures.isEmpty()) {
            request.setPictures(pictures);
        }

        return request;
    }

    /**
     * Upload product images to Mercado Livre
     * AC4: Upload de Imagens - Story 5.3
     */
    private List<MLPicture> uploadProductImages(UUID tenantId, Product product) {
        List<MLPicture> pictures = new ArrayList<>();

        // For now, we'll use a placeholder image generator based on product name
        // In a real scenario, you would fetch actual product images from a storage service
        // or from a product_images table

        // Example: Generate a placeholder image URL (replace with actual image URLs in production)
        String placeholderImageUrl = generatePlaceholderImageUrl(product.getName());

        if (placeholderImageUrl != null) {
            String pictureId = apiClient.uploadPicture(placeholderImageUrl, tenantId);
            if (pictureId != null) {
                MLPicture picture = new MLPicture();
                picture.setId(pictureId);
                picture.setSource(placeholderImageUrl);
                pictures.add(picture);
            }
        }

        return pictures;
    }

    /**
     * Generate placeholder image URL for a product
     * In production, replace this with actual product image URLs from database
     */
    private String generatePlaceholderImageUrl(String productName) {
        // Using a placeholder image service (ui-avatars.com) as an example
        // In production, you would fetch real product images from your storage
        try {
            String encoded = java.net.URLEncoder.encode(productName, "UTF-8");
            return "https://ui-avatars.com/api/?name=" + encoded + "&size=500&background=random";
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Build ML item request for product with variants
     */
    private MLCreateItemRequest buildVariantItemRequest(UUID tenantId, Product product) {
        MLCreateItemRequest request = new MLCreateItemRequest();

        request.setTitle(product.getName());
        request.setPrice(product.getPrice()); // Base price
        request.setDescription(product.getDescription() != null ? product.getDescription() : product.getName());

        // Get category suggestion
        CategorySuggestionResponse category = suggestCategory(tenantId, product.getName());
        request.setCategoryId(category.getCategoryId());

        // AC4: Upload main product images
        List<MLPicture> pictures = uploadProductImages(tenantId, product);
        if (!pictures.isEmpty()) {
            request.setPictures(pictures);
        }

        // Get variants and build ML variations
        List<ProductVariant> variants = variantRepository.findByParentProductId(product.getId());

        List<MLVariation> mlVariations = new ArrayList<>();

        for (ProductVariant variant : variants) {
            MLVariation mlVariation = new MLVariation();
            mlVariation.setPrice(variant.getPrice());
            mlVariation.setAvailableQuantity(0); // Stock sync done separately

            // Build attribute combinations (e.g., Color=Red, Size=M)
            List<MLAttribute> attributeCombinations = buildAttributesFromVariantName(variant.getName());
            mlVariation.setAttributeCombinations(attributeCombinations);

            // AC4: Upload variant-specific images if available
            List<String> variantPictureIds = uploadVariantImages(tenantId, variant);
            if (!variantPictureIds.isEmpty()) {
                mlVariation.setPictureIds(variantPictureIds);
            }

            mlVariations.add(mlVariation);
        }

        request.setVariations(mlVariations);

        return request;
    }

    /**
     * Upload variant-specific images to Mercado Livre
     * AC4: Upload de Imagens - Story 5.3
     */
    private List<String> uploadVariantImages(UUID tenantId, ProductVariant variant) {
        List<String> pictureIds = new ArrayList<>();

        // For variants, we could use variant.getImageUrl() if it exists
        // For now, using placeholder
        String imageUrl = generatePlaceholderImageUrl(variant.getName());

        if (imageUrl != null) {
            String pictureId = apiClient.uploadPicture(imageUrl, tenantId);
            if (pictureId != null) {
                pictureIds.add(pictureId);
            }
        }

        return pictureIds;
    }

    /**
     * Parse variant name to build ML attributes
     * Example: "T-shirt - Red - M" -> [Color=Red, Size=M]
     *
     * This is a simplified implementation. In production, you'd want to use
     * proper product_attributes table mapping.
     */
    private List<MLAttribute> buildAttributesFromVariantName(String variantName) {
        List<MLAttribute> attributes = new ArrayList<>();

        // Simple parsing - assumes format "Product - Attr1 - Attr2"
        String[] parts = variantName.split(" - ");

        if (parts.length > 1) {
            // Assume second part is color
            attributes.add(new MLAttribute("COLOR", parts[1]));
        }

        if (parts.length > 2) {
            // Assume third part is size
            attributes.add(new MLAttribute("SIZE", parts[2]));
        }

        return attributes;
    }

    /**
     * Save simple product listing to database
     */
    private void saveSimpleListing(UUID tenantId, Product product, MLCreateItemResponse mlResponse) {
        MarketplaceListing listing = new MarketplaceListing();
        listing.setTenantId(tenantId);
        listing.setProductId(product.getId());
        listing.setMarketplace(Marketplace.MERCADO_LIVRE);
        listing.setListingIdMarketplace(mlResponse.getId());
        listing.setTitle(mlResponse.getTitle());
        listing.setPrice(mlResponse.getPrice());
        listing.setQuantity(mlResponse.getAvailableQuantity());
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setLastSyncAt(LocalDateTime.now());

        listingRepository.save(listing);

        log.info("Saved listing for product {} with ML ID: {}", product.getName(), mlResponse.getId());
    }

    /**
     * Save variant listings to database
     */
    private void saveVariantListings(UUID tenantId, Product product, MLCreateItemResponse mlResponse) {
        List<ProductVariant> variants = variantRepository.findByParentProductId(product.getId());
        List<MLVariation> mlVariations = mlResponse.getVariations();

        // Match variants with ML variations (by index for now)
        for (int i = 0; i < variants.size() && i < mlVariations.size(); i++) {
            ProductVariant variant = variants.get(i);
            MLVariation mlVariation = mlVariations.get(i);

            MarketplaceListing listing = new MarketplaceListing();
            listing.setTenantId(tenantId);
            listing.setProductId(product.getId());
            listing.setVariantId(variant.getId());
            listing.setMarketplace(Marketplace.MERCADO_LIVRE);
            listing.setListingIdMarketplace(mlResponse.getId()); // Parent item ID
            listing.setTitle(variant.getName());
            listing.setPrice(mlVariation.getPrice());
            listing.setQuantity(mlVariation.getAvailableQuantity());
            listing.setStatus(ListingStatus.ACTIVE);
            listing.setLastSyncAt(LocalDateTime.now());

            listingRepository.save(listing);
        }

        log.info("Saved {} variant listings for product {}", variants.size(), product.getName());
    }

    /**
     * Build category path string from ML hierarchy
     */
    private String buildCategoryPath(List<MLCategoryPredictorResponse.PathFromRoot> pathFromRoot) {
        if (pathFromRoot == null || pathFromRoot.isEmpty()) {
            return "";
        }

        StringBuilder path = new StringBuilder();
        for (int i = 0; i < pathFromRoot.size(); i++) {
            if (i > 0) {
                path.append(" > ");
            }
            path.append(pathFromRoot.get(i).getName());
        }

        return path.toString();
    }
}
