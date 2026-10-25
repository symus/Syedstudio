package com.craftora.maker;

import com.craftora.category.Category;
import com.craftora.common.BaseEntity;
import com.craftora.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** A maker's business. Owns products, storefront, inquiries and subscription (tenant boundary). */
@Entity
@Table(name = "maker_profile")
public class MakerProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_category_id")
    private Category primaryCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MakerStatus status = MakerStatus.PENDING;

    private Instant approvedAt;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false)
    private String businessName;

    private String tagline;

    private String bio;

    private String city;

    private String region;

    @Column(nullable = false)
    private String countryCode = "PL";

    private boolean showLocation = true;

    private String logoKey;

    private String coverKey;

    private String contactEmail;

    private String phone;

    private String websiteUrl;

    private String instagramUrl;

    private String facebookUrl;

    private String tiktokUrl;

    private String pinterestUrl;

    private boolean acceptsCustomOrders = true;

    private String legalName;

    private String taxId;

    private String businessAddress;

    private boolean featured;

    protected MakerProfile() {}

    public MakerProfile(User user, String slug, String businessName) {
        this.user = user;
        this.slug = slug;
        this.businessName = businessName;
    }

    public boolean isPubliclyVisible() {
        return status == MakerStatus.APPROVED;
    }

    public void approve() {
        status = MakerStatus.APPROVED;
        if (approvedAt == null) {
            approvedAt = Instant.now();
        }
    }

    public User getUser() {
        return user;
    }

    public Category getPrimaryCategory() {
        return primaryCategory;
    }

    public void setPrimaryCategory(Category primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public MakerStatus getStatus() {
        return status;
    }

    public void setStatus(MakerStatus status) {
        this.status = status;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public boolean isShowLocation() {
        return showLocation;
    }

    public void setShowLocation(boolean showLocation) {
        this.showLocation = showLocation;
    }

    public String getLogoKey() {
        return logoKey;
    }

    public void setLogoKey(String logoKey) {
        this.logoKey = logoKey;
    }

    public String getCoverKey() {
        return coverKey;
    }

    public void setCoverKey(String coverKey) {
        this.coverKey = coverKey;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getTiktokUrl() {
        return tiktokUrl;
    }

    public void setTiktokUrl(String tiktokUrl) {
        this.tiktokUrl = tiktokUrl;
    }

    public String getPinterestUrl() {
        return pinterestUrl;
    }

    public void setPinterestUrl(String pinterestUrl) {
        this.pinterestUrl = pinterestUrl;
    }

    public boolean isAcceptsCustomOrders() {
        return acceptsCustomOrders;
    }

    public void setAcceptsCustomOrders(boolean acceptsCustomOrders) {
        this.acceptsCustomOrders = acceptsCustomOrders;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
}
