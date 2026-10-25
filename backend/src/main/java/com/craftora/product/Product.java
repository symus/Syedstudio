package com.craftora.product;

import com.craftora.category.Category;
import com.craftora.common.BaseEntity;
import com.craftora.maker.MakerProfile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** A listing owned by one maker; the same record powers both the marketplace and the maker's storefront. */
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maker_id")
    private MakerProfile maker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductEnums.Status status = ProductEnums.Status.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductEnums.AvailabilityType availabilityType = ProductEnums.AvailabilityType.READY_MADE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductEnums.Moderation moderationStatus = ProductEnums.Moderation.VISIBLE;

    private Instant publishedAt;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false)
    private String name;

    private String description;

    private Long priceMinor;

    @Column(nullable = false)
    private String currency = "PLN";

    private Integer productionDays;

    private boolean available = true;

    private boolean customizable;

    private String materials;

    private String dimensions;

    private String shippingInfo;

    private boolean showOnStorefront = true;

    private String moderationReason;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position asc")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position asc")
    private List<ProductOption> options = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "product_tag", joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "product_occasion", joinColumns = @JoinColumn(name = "product_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "occasion")
    private Set<ProductEnums.Occasion> occasions = new HashSet<>();

    protected Product() {}

    public Product(MakerProfile maker, String slug, String name) {
        this.maker = maker;
        this.slug = slug;
        this.name = name;
    }

    /** Visible on the marketplace and storefront: published by the maker, not hidden by moderation, maker approved. */
    public boolean isPubliclyVisible() {
        return status == ProductEnums.Status.PUBLISHED && moderationStatus == ProductEnums.Moderation.VISIBLE
                && maker.isPubliclyVisible();
    }

    public void changeStatus(ProductEnums.Status next) {
        if (next == ProductEnums.Status.PUBLISHED && publishedAt == null) {
            publishedAt = Instant.now();
        }
        status = next;
    }

    public ProductImage firstImage() {
        return images.isEmpty() ? null : images.getFirst();
    }

    public MakerProfile getMaker() {
        return maker;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public ProductEnums.Status getStatus() {
        return status;
    }

    public ProductEnums.AvailabilityType getAvailabilityType() {
        return availabilityType;
    }

    public void setAvailabilityType(ProductEnums.AvailabilityType availabilityType) {
        this.availabilityType = availabilityType;
    }

    public ProductEnums.Moderation getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(ProductEnums.Moderation moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public List<ProductOption> getOptions() {
        return options;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public Set<ProductEnums.Occasion> getOccasions() {
        return occasions.isEmpty() ? EnumSet.noneOf(ProductEnums.Occasion.class) : EnumSet.copyOf(occasions);
    }

    public void setOccasions(Set<ProductEnums.Occasion> occasions) {
        this.occasions.clear();
        this.occasions.addAll(occasions);
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
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

    public Long getPriceMinor() {
        return priceMinor;
    }

    public void setPriceMinor(Long priceMinor) {
        this.priceMinor = priceMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getProductionDays() {
        return productionDays;
    }

    public void setProductionDays(Integer productionDays) {
        this.productionDays = productionDays;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isCustomizable() {
        return customizable;
    }

    public void setCustomizable(boolean customizable) {
        this.customizable = customizable;
    }

    public String getMaterials() {
        return materials;
    }

    public void setMaterials(String materials) {
        this.materials = materials;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getShippingInfo() {
        return shippingInfo;
    }

    public void setShippingInfo(String shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public boolean isShowOnStorefront() {
        return showOnStorefront;
    }

    public void setShowOnStorefront(boolean showOnStorefront) {
        this.showOnStorefront = showOnStorefront;
    }

    public String getModerationReason() {
        return moderationReason;
    }

    public void setModerationReason(String moderationReason) {
        this.moderationReason = moderationReason;
    }
}
