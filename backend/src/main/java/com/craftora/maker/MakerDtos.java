package com.craftora.maker;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class MakerDtos {

    private MakerDtos() {}

    /** Social/website links must be http(s) to rule out javascript: URLs. */
    public static final String URL = "^$|^https?://[^\\s<>\"]{3,250}$";

    public record MakerProfileRequest(
            @NotBlank @Pattern(regexp = "^[a-z0-9](?:[a-z0-9-]{1,58}[a-z0-9])$",
                    message = "3-60 lowercase letters, digits or hyphens") String slug,
            @NotBlank @Size(max = 120) String businessName,
            @Size(max = 160) String tagline,
            @Size(max = 5000) String bio,
            @Size(max = 80) String city,
            @Size(max = 80) String region,
            @Pattern(regexp = "^[A-Z]{2}$") String countryCode,
            boolean showLocation,
            @Email @Size(max = 254) String contactEmail,
            @Size(max = 40) String phone,
            @Pattern(regexp = URL) String websiteUrl,
            @Pattern(regexp = URL) String instagramUrl,
            @Pattern(regexp = URL) String facebookUrl,
            @Pattern(regexp = URL) String tiktokUrl,
            @Pattern(regexp = URL) String pinterestUrl,
            boolean acceptsCustomOrders,
            UUID primaryCategoryId,
            @Size(max = 160) String legalName,
            @Size(max = 40) String taxId,
            @Size(max = 255) String businessAddress) {}

    /** Owner view: everything including moderation status. */
    public record MakerProfileResponse(
            UUID id, String slug, String businessName, String tagline, String bio, String city, String region,
            String countryCode, boolean showLocation, String logoUrl, String coverUrl, String contactEmail,
            String phone, String websiteUrl, String instagramUrl, String facebookUrl, String tiktokUrl,
            String pinterestUrl, boolean acceptsCustomOrders, UUID primaryCategoryId, String legalName, String taxId,
            String businessAddress, MakerStatus status, Instant createdAt) {}

    public record SocialLinks(String website, String instagram, String facebook, String tiktok, String pinterest) {}

    /** Trader identification shown publicly where the maker provided it. */
    public record SellerInfo(String legalName, String taxId, String businessAddress) {}

    /** Public view: location only when the maker chose to disclose it; no moderation fields. */
    public record PublicMakerResponse(
            UUID id, String slug, String businessName, String tagline, String bio, String city, String region,
            String countryCode, String logoUrl, String coverUrl, String contactEmail, String phone,
            SocialLinks social, boolean acceptsCustomOrders, String primaryCategorySlug, SellerInfo seller,
            boolean hasStorefront, Instant memberSince) {}

    public record MakerCard(UUID id, String slug, String businessName, String tagline, String city, String logoUrl,
                            String coverUrl, long productCount) {}

    public record SlugAvailability(String slug, boolean available) {}

    public record OverviewResponse(String storefrontStatus, boolean storefrontHasUnpublishedChanges,
                                   MakerStatus makerStatus, boolean emailVerified, long totalProducts,
                                   long publishedProducts, long newInquiries, long openInquiries, String planCode,
                                   String planName, int maxPublishedProducts, int maxImagesPerProduct,
                                   java.util.List<ActivityItem> recentActivity) {}

    public record ActivityItem(String type, String label, UUID refId, Instant at) {}
}
