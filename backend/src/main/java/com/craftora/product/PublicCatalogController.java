package com.craftora.product;

import com.craftora.category.CategoryDtos.CategoryResponse;
import com.craftora.category.CategoryService;
import com.craftora.common.PageResponse;
import com.craftora.maker.MakerDtos.MakerCard;
import com.craftora.maker.MakerDtos.PublicMakerResponse;
import com.craftora.maker.MakerProfile;
import com.craftora.maker.MakerProfileRepository;
import com.craftora.maker.MakerService;
import com.craftora.maker.MakerStatus;
import com.craftora.product.ProductDtos.ProductCard;
import com.craftora.product.ProductDtos.PublicProductResponse;
import com.craftora.storefront.StorefrontRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public catalogue")
@Validated
@RestController
@RequestMapping("/api/public")
public class PublicCatalogController {

    private final CatalogService catalog;
    private final MakerService makerService;
    private final MakerProfileRepository makers;
    private final StorefrontRepository storefronts;
    private final CategoryService categories;

    public PublicCatalogController(CatalogService catalog, MakerService makerService, MakerProfileRepository makers,
                                   StorefrontRepository storefronts, CategoryService categories) {
        this.catalog = catalog;
        this.makerService = makerService;
        this.makers = makers;
        this.storefronts = storefronts;
        this.categories = categories;
    }

    @GetMapping("/products")
    public PageResponse<ProductCard> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @Min(0) Long minPrice,
            @RequestParam(required = false) @Min(0) Long maxPrice,
            @RequestParam(required = false) ProductEnums.Occasion occasion,
            @RequestParam(required = false) String availability,
            @RequestParam(required = false) Boolean customizable,
            @RequestParam(required = false) String maker,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        String query = q != null && q.length() > 100 ? q.substring(0, 100) : q;
        return catalog.search(new SearchCriteria(query, category, city, minPrice, maxPrice, occasion, availability,
                customizable, maker, sort, PageResponse.clampPage(page), PageResponse.clampSize(size, 24, 60)));
    }

    @GetMapping("/products/{makerSlug}/{productSlug}")
    public PublicProductResponse product(@PathVariable String makerSlug, @PathVariable String productSlug,
                                         @RequestParam(defaultValue = "pl") String locale) {
        return catalog.product(makerSlug, productSlug, locale);
    }

    @GetMapping("/makers")
    public PageResponse<MakerCard> makers(@RequestParam(required = false) String q,
                                          @RequestParam(required = false) String city,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) @Max(60) Integer size) {
        return catalog.makers(q, city, category, PageResponse.clampPage(page), PageResponse.clampSize(size, 24, 60));
    }

    @GetMapping("/makers/{slug}")
    public PublicMakerResponse maker(@PathVariable String slug) {
        return makerService.getPublic(slug);
    }

    @GetMapping("/cities")
    public List<String> cities() {
        return catalog.cities();
    }

    public record HomeResponse(List<MakerCard> featuredMakers, List<ProductCard> trendingProducts,
                               List<ProductCard> recentProducts, List<CategoryResponse> popularCategories) {}

    @GetMapping("/home")
    public ResponseEntity<HomeResponse> home(@RequestParam(defaultValue = "pl") String locale) {
        var featured = makers.findTop8ByStatusAndFeaturedTrueOrderByApprovedAtDesc(MakerStatus.APPROVED);
        if (featured.isEmpty()) {
            featured = makers.findTop8ByStatusOrderByApprovedAtDesc(MakerStatus.APPROVED);
        }
        var recent = catalog.search(new SearchCriteria(null, null, null, null, null, null, null, null, null,
                "newest", 0, 8)).items();
        var popular = categories.listPublic(locale).stream()
                .sorted(Comparator.comparingLong(CategoryResponse::productCount).reversed()).limit(8).toList();
        var body = new HomeResponse(catalog.makerCards(featured.stream().map(MakerProfile::getId).toList()),
                catalog.trending(8), recent, popular);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofSeconds(60)).cachePublic()).body(body);
    }

    public record SitemapResponse(List<String> storefronts, List<String> makers, List<String> categories,
                                  List<String> products) {}

    /** Public URLs for sitemap.xml generation. Products are listed as "makerSlug/productSlug". */
    @GetMapping("/sitemap")
    public SitemapResponse sitemap() {
        return new SitemapResponse(storefronts.findPublishedSlugs(), makers.findSlugsByStatus(MakerStatus.APPROVED),
                categories.listPublic("pl").stream().map(CategoryResponse::slug).toList(),
                catalog.publicProductPaths(50_000));
    }
}
