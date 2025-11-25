package com.estoquecentral.marketplace.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MarketplaceListing entity - Maps products/variants to marketplace listings
 * Story 5.2: Import Products from Mercado Livre - AC1
 *
 * Represents a product or variant published on a marketplace (Mercado Livre, Shopee, etc.)
 */
@Table("marketplace_listings")
public class MarketplaceListing {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("product_id")
    private UUID productId;

    @Column("variant_id")
    private UUID variantId;

    @Column("marketplace")
    private Marketplace marketplace;

    /**
     * Listing ID from marketplace (e.g., MLB123456789 for Mercado Livre)
     */
    @Column("listing_id_marketplace")
    private String listingIdMarketplace;

    @Column("title")
    private String title;

    @Column("price")
    private BigDecimal price;

    @Column("quantity")
    private Integer quantity;

    @Column("status")
    private ListingStatus status;

    @Column("last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column("data_criacao")
    private LocalDateTime dataCriacao;

    @Column("data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Constructors

    public MarketplaceListing() {
        this.status = ListingStatus.ACTIVE;
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.quantity = 0;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    public String getListingIdMarketplace() {
        return listingIdMarketplace;
    }

    public void setListingIdMarketplace(String listingIdMarketplace) {
        this.listingIdMarketplace = listingIdMarketplace;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    /**
     * Check if this listing is for a simple product (no variant)
     */
    public boolean isSimpleProduct() {
        return variantId == null;
    }

    /**
     * Check if this listing is active
     */
    public boolean isActive() {
        return status == ListingStatus.ACTIVE;
    }
}
