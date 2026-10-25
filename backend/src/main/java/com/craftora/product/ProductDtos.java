package com.craftora.product;

import com.craftora.maker.MakerDtos.SellerInfo;
import com.craftora.product.ProductEnums.AvailabilityType;
import com.craftora.product.ProductEnums.Moderation;
import com.craftora.product.ProductEnums.Occasion;
import com.craftora.product.ProductEnums.OptionType;
import com.craftora.product.ProductEnums.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ProductDtos {

    private ProductDtos() {}

    public record OptionRequest(
            @NotBlank @Size(max = 80) String name,
            @NotNull OptionType type,
            @Size(max = 20) List<@NotBlank @Size(max = 60) String> choices,
            boolean required) {}

    public record ProductRequest(
            @NotBlank @Size(max = 140) String name,
            @Size(max = 10000) String description,
            UUID categoryId,
            @Min(0) @Max(100_000_000) Long priceMinor,
            @Pattern(regexp = "^(PLN|EUR)$") String currency,
            @Size(max = 10) List<@NotBlank @Size(max = 30) String> tags,
            Set<Occasion> occasions,
            @NotNull AvailabilityType availabilityType,
            @Min(0) @Max(365) Integer productionDays,
            boolean available,
            boolean customizable,
            @Size(max = 255) String materials,
            @Size(max = 255) String dimensions,
            @Size(max = 2000) String shippingInfo,
            boolean showOnStorefront,
            @Size(max = 10) List<@Valid OptionRequest> options) {}

    public record StatusRequest(@NotNull Status status) {}

    public record ImageOrderRequest(@NotEmpty List<UUID> imageIds) {}

    public record ImageResponse(UUID id, String url, String altText, int position) {}

    public record OptionResponse(UUID id, String name, OptionType type, List<String> choices, boolean required) {}

    public record MakerProductResponse(
            UUID id, String slug, String name, String description, UUID categoryId, Long priceMinor,
            String currency, List<String> tags, Set<Occasion> occasions, AvailabilityType availabilityType,
            Integer productionDays, boolean available, boolean customizable, String materials, String dimensions,
            String shippingInfo, boolean showOnStorefront, Status status, Moderation moderationStatus,
            String moderationReason, List<ImageResponse> images, List<OptionResponse> options, Instant publishedAt,
            Instant updatedAt) {}

    public record ProductCard(
            UUID id, String slug, String name, Long priceMinor, String currency, String imageUrl, String imageAlt,
            String makerSlug, String makerName, String makerCity, boolean available,
            AvailabilityType availabilityType, boolean customizable) {}

    public record ProductMaker(String slug, String businessName, String logoUrl, String city,
                               boolean acceptsCustomOrders, SellerInfo seller, boolean hasStorefront) {}

    public record PublicProductResponse(
            UUID id, String slug, String name, String description, String categorySlug, String categoryName,
            Long priceMinor, String currency, List<String> tags, Set<Occasion> occasions,
            AvailabilityType availabilityType, Integer productionDays, boolean available, boolean customizable,
            String materials, String dimensions, String shippingInfo, List<ImageResponse> images,
            List<OptionResponse> options, ProductMaker maker, List<ProductCard> moreFromMaker,
            List<ProductCard> related, Instant publishedAt) {}
}
