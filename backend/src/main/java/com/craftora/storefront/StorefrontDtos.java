package com.craftora.storefront;

import com.craftora.maker.MakerDtos.PublicMakerResponse;
import com.craftora.product.ProductDtos.ProductCard;
import java.time.Instant;
import java.util.List;

public final class StorefrontDtos {

    private StorefrontDtos() {}

    public record TemplateResponse(String key, String name, String description, String defaultPrimaryColor,
                                   String defaultAccentColor, String defaultFont) {}

    public record StorefrontStateResponse(Storefront.Status status, boolean hasUnpublishedChanges,
                                          Instant publishedAt, StorefrontConfig draft, String heroImageUrl,
                                          String path, boolean makerApproved, boolean emailVerified,
                                          List<TemplateResponse> templates) {}

    /** Everything needed to render a storefront page (public or preview). */
    public record StorefrontView(PublicMakerResponse maker, StorefrontConfig config, String heroImageUrl,
                                 List<ProductCard> products, boolean showPlatformBranding, boolean preview) {}
}
