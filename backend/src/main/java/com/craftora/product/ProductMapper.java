package com.craftora.product;

import com.craftora.media.MediaController;
import com.craftora.product.ProductDtos.ImageResponse;
import com.craftora.product.ProductDtos.MakerProductResponse;
import com.craftora.product.ProductDtos.OptionResponse;
import com.craftora.product.ProductDtos.ProductCard;
import com.craftora.product.ProductEnums.Moderation;
import java.util.Comparator;
import java.util.List;

public final class ProductMapper {

    private ProductMapper() {}

    public static ProductCard card(Product p) {
        ProductImage img = p.firstImage();
        var m = p.getMaker();
        return new ProductCard(p.getId(), p.getSlug(), p.getName(), p.getPriceMinor(), p.getCurrency(),
                img == null ? null : MediaController.url(img.getStorageKey()),
                img == null ? p.getName() : altOrName(img, p), m.getSlug(), m.getBusinessName(),
                m.isShowLocation() ? m.getCity() : null, p.isAvailable(), p.getAvailabilityType(),
                p.isCustomizable());
    }

    public static MakerProductResponse owner(Product p) {
        return new MakerProductResponse(p.getId(), p.getSlug(), p.getName(), p.getDescription(),
                p.getCategory() == null ? null : p.getCategory().getId(), p.getPriceMinor(), p.getCurrency(),
                tags(p), p.getOccasions(), p.getAvailabilityType(), p.getProductionDays(), p.isAvailable(),
                p.isCustomizable(), p.getMaterials(), p.getDimensions(), p.getShippingInfo(),
                p.isShowOnStorefront(), p.getStatus(), p.getModerationStatus(),
                p.getModerationStatus() == Moderation.HIDDEN ? p.getModerationReason() : null,
                images(p), options(p), p.getPublishedAt(), p.getUpdatedAt());
    }

    static List<String> tags(Product p) {
        return p.getTags().stream().map(Tag::getName).sorted(Comparator.naturalOrder()).toList();
    }

    static List<ImageResponse> images(Product p) {
        return p.getImages().stream()
                .map(i -> new ImageResponse(i.getId(), MediaController.url(i.getStorageKey()), altOrName(i, p),
                        i.getPosition()))
                .toList();
    }

    static List<OptionResponse> options(Product p) {
        return p.getOptions().stream()
                .map(o -> new OptionResponse(o.getId(), o.getName(), o.getType(), o.getChoices(), o.isRequired()))
                .toList();
    }

    private static String altOrName(ProductImage i, Product p) {
        return i.getAltText() == null || i.getAltText().isBlank() ? p.getName() : i.getAltText();
    }
}
