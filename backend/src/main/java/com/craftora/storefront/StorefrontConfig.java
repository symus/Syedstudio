package com.craftora.storefront;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Structured, template-based website configuration (no free-form HTML, so no stored XSS).
 * A draft copy is edited; publishing snapshots it into the published copy.
 */
public record StorefrontConfig(
        @NotBlank @Size(max = 40) String templateKey,
        @NotBlank @Pattern(regexp = HEX) String primaryColor,
        @NotBlank @Pattern(regexp = HEX) String accentColor,
        @NotNull Font font,
        @Size(max = 120) String heroTitle,
        @Size(max = 240) String heroSubtitle,
        @Size(max = 255) String heroImageKey,
        @Size(max = 80) String aboutTitle,
        @Size(max = 5000) String aboutText,
        @Size(max = 80) String featuredHeading,
        @NotNull @Size(max = 6) List<@NotNull Section> sections,
        @NotBlank @Pattern(regexp = "^(pl|en)$") String language,
        @Size(max = 70) String seoTitle,
        @Size(max = 160) String seoDescription) {

    public static final String HEX = "^#[0-9a-fA-F]{6}$";

    public enum Font { SERIF, SANS, ROUNDED }

    public enum Section { HERO, FEATURED_PRODUCTS, ABOUT, GALLERY, CUSTOM_ORDER, CONTACT }

    public static StorefrontConfig defaults(StorefrontTemplate template, String businessName, String tagline,
                                            String language) {
        return new StorefrontConfig(template.getKey(), template.getDefaultPrimaryColor(),
                template.getDefaultAccentColor(), Font.valueOf(template.getDefaultFont()), businessName, tagline,
                null, null, null, null,
                List.of(Section.HERO, Section.FEATURED_PRODUCTS, Section.ABOUT, Section.GALLERY,
                        Section.CUSTOM_ORDER, Section.CONTACT),
                language, null, null);
    }

    public StorefrontConfig withHeroImageKey(String key) {
        return new StorefrontConfig(templateKey, primaryColor, accentColor, font, heroTitle, heroSubtitle, key,
                aboutTitle, aboutText, featuredHeading, sections, language, seoTitle, seoDescription);
    }
}
