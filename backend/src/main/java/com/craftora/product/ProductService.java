package com.craftora.product;

import com.craftora.category.CategoryRepository;
import com.craftora.common.ApiException;
import com.craftora.common.PageResponse;
import com.craftora.common.Slugs;
import com.craftora.maker.MakerProfile;
import com.craftora.maker.MakerService;
import com.craftora.media.ImageUploads;
import com.craftora.product.ProductDtos.MakerProductResponse;
import com.craftora.product.ProductDtos.OptionRequest;
import com.craftora.product.ProductDtos.ProductRequest;
import com.craftora.product.ProductEnums.OptionType;
import com.craftora.product.ProductEnums.Status;
import com.craftora.subscription.PlanService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Maker-side product management. Every method is scoped to the caller's own maker profile. */
@Service
public class ProductService {

    private final ProductRepository products;
    private final TagRepository tags;
    private final CategoryRepository categories;
    private final MakerService makers;
    private final PlanService plans;
    private final ImageUploads uploads;

    public ProductService(ProductRepository products, TagRepository tags, CategoryRepository categories,
                          MakerService makers, PlanService plans, ImageUploads uploads) {
        this.products = products;
        this.tags = tags;
        this.categories = categories;
        this.makers = makers;
        this.plans = plans;
        this.uploads = uploads;
    }

    @Transactional(readOnly = true)
    public PageResponse<MakerProductResponse> list(UUID userId, Status status, Integer page, Integer size) {
        MakerProfile maker = makers.requireOwn(userId);
        var pageable = PageRequest.of(PageResponse.clampPage(page), PageResponse.clampSize(size, 20, 100));
        var result = status == null
                ? products.findByMakerIdOrderByUpdatedAtDesc(maker.getId(), pageable)
                : products.findByMakerIdAndStatusOrderByUpdatedAtDesc(maker.getId(), status, pageable);
        return PageResponse.of(result, ProductMapper::owner);
    }

    @Transactional(readOnly = true)
    public MakerProductResponse get(UUID userId, UUID productId) {
        return ProductMapper.owner(own(userId, productId));
    }

    @Transactional
    public MakerProductResponse create(UUID userId, ProductRequest req) {
        MakerProfile maker = makers.requireOwn(userId);
        Product p = new Product(maker, uniqueSlug(maker.getId(), req.name()), req.name().trim());
        apply(p, req);
        products.save(p);
        return ProductMapper.owner(p);
    }

    @Transactional
    public MakerProductResponse update(UUID userId, UUID productId, ProductRequest req) {
        Product p = own(userId, productId);
        if (p.getPublishedAt() == null && !p.getName().equals(req.name().trim())) {
            // Never-published listings can still change URL; published ones keep theirs for SEO stability.
            p.setSlug(uniqueSlug(p.getMaker().getId(), req.name()));
        }
        p.setName(req.name().trim());
        apply(p, req);
        if (p.getStatus() == Status.PUBLISHED) {
            assertPublishable(p);
        }
        return ProductMapper.owner(p);
    }

    @Transactional
    public MakerProductResponse changeStatus(UUID userId, UUID productId, Status next) {
        Product p = own(userId, productId);
        if (next == Status.PUBLISHED && p.getStatus() != Status.PUBLISHED) {
            assertPublishable(p);
            plans.assertCanPublish(p.getMaker().getId(),
                    products.countByMakerIdAndStatus(p.getMaker().getId(), Status.PUBLISHED));
        }
        p.changeStatus(next);
        return ProductMapper.owner(p);
    }

    @Transactional
    public void delete(UUID userId, UUID productId) {
        Product p = own(userId, productId);
        List<String> keys = p.getImages().stream().map(ProductImage::getStorageKey).toList();
        products.delete(p);
        keys.forEach(uploads::deleteQuietly);
    }

    @Transactional
    public MakerProductResponse addImage(UUID userId, UUID productId, MultipartFile file, String altText) {
        Product p = own(userId, productId);
        plans.assertCanAddImage(p.getMaker().getId(), p.getImages().size());
        var stored = uploads.store(file, "public/products/" + p.getId());
        p.getImages().add(new ProductImage(p, stored.key(), p.getImages().size(),
                altText == null || altText.isBlank() ? null : altText.trim(), stored.contentType(), stored.sizeBytes()));
        products.saveAndFlush(p);
        return ProductMapper.owner(p);
    }

    @Transactional
    public MakerProductResponse removeImage(UUID userId, UUID productId, UUID imageId) {
        Product p = own(userId, productId);
        ProductImage img = p.getImages().stream().filter(i -> i.getId().equals(imageId)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Image"));
        if (p.getStatus() == Status.PUBLISHED && p.getImages().size() == 1) {
            throw ApiException.badRequest("image_required", "Published products need at least one image");
        }
        p.getImages().remove(img);
        renumber(p.getImages());
        uploads.deleteQuietly(img.getStorageKey());
        return ProductMapper.owner(p);
    }

    @Transactional
    public MakerProductResponse reorderImages(UUID userId, UUID productId, List<UUID> imageIds) {
        Product p = own(userId, productId);
        Map<UUID, ProductImage> byId = new HashMap<>();
        p.getImages().forEach(i -> byId.put(i.getId(), i));
        if (imageIds.size() != byId.size() || !byId.keySet().containsAll(imageIds)) {
            throw ApiException.badRequest("invalid_order", "Order must list every image of the product exactly once");
        }
        for (int i = 0; i < imageIds.size(); i++) {
            byId.get(imageIds.get(i)).setPosition(i);
        }
        p.getImages().sort(java.util.Comparator.comparingInt(ProductImage::getPosition));
        return ProductMapper.owner(p);
    }

    private Product own(UUID userId, UUID productId) {
        MakerProfile maker = makers.requireOwn(userId);
        return products.findByIdAndMakerId(productId, maker.getId())
                .orElseThrow(() -> ApiException.notFound("Product"));
    }

    private void apply(Product p, ProductRequest r) {
        p.setDescription(blankToNull(r.description()));
        p.setCategory(r.categoryId() == null ? null : categories.findById(r.categoryId())
                .filter(c -> c.isActive())
                .orElseThrow(() -> ApiException.badRequest("unknown_category", "Unknown category")));
        p.setPriceMinor(r.priceMinor());
        p.setCurrency(r.currency() == null ? "PLN" : r.currency());
        p.setAvailabilityType(r.availabilityType());
        p.setProductionDays(r.productionDays());
        p.setAvailable(r.available());
        p.setCustomizable(r.customizable());
        p.setMaterials(blankToNull(r.materials()));
        p.setDimensions(blankToNull(r.dimensions()));
        p.setShippingInfo(blankToNull(r.shippingInfo()));
        p.setShowOnStorefront(r.showOnStorefront());
        p.setOccasions(r.occasions() == null ? Set.of() : r.occasions());
        applyTags(p, r.tags() == null ? List.of() : r.tags());
        applyOptions(p, r.options() == null ? List.of() : r.options());
    }

    private void applyTags(Product p, List<String> names) {
        Map<String, String> bySlug = new LinkedHashMap<>();
        for (String n : names) {
            String slug = Slugs.slugify(n, 30);
            if (!slug.isEmpty()) {
                bySlug.putIfAbsent(slug, n.trim());
            }
        }
        Set<Tag> resolved = new HashSet<>(tags.findBySlugIn(bySlug.keySet()));
        Set<String> existing = new HashSet<>();
        resolved.forEach(t -> existing.add(t.getSlug()));
        bySlug.forEach((slug, name) -> {
            if (!existing.contains(slug)) {
                resolved.add(tags.save(new Tag(slug, name)));
            }
        });
        p.getTags().clear();
        p.getTags().addAll(resolved);
    }

    private void applyOptions(Product p, List<OptionRequest> requested) {
        List<ProductOption> next = new ArrayList<>();
        for (int i = 0; i < requested.size(); i++) {
            OptionRequest o = requested.get(i);
            List<String> choices = o.choices() == null ? List.of()
                    : o.choices().stream().map(String::trim).filter(c -> !c.isEmpty()).distinct().toList();
            if (o.type() == OptionType.SELECT && choices.size() < 2) {
                throw ApiException.badRequest("option_choices", "Option \"" + o.name() + "\" needs at least two choices");
            }
            next.add(new ProductOption(p, o.name().trim(), o.type(),
                    o.type() == OptionType.SELECT ? choices : null, o.required(), i));
        }
        p.getOptions().clear();
        p.getOptions().addAll(next);
    }

    private static void assertPublishable(Product p) {
        if (p.getImages().isEmpty()) {
            throw ApiException.badRequest("image_required", "Add at least one photo before publishing");
        }
        if (p.getCategory() == null) {
            throw ApiException.badRequest("category_required", "Choose a category before publishing");
        }
    }

    private String uniqueSlug(UUID makerId, String name) {
        String base = Slugs.slugify(name, 120);
        if (base.isEmpty()) {
            base = "product";
        }
        String slug = base;
        for (int i = 2; products.existsByMakerIdAndSlug(makerId, slug); i++) {
            slug = base + "-" + i;
        }
        return slug;
    }

    private static void renumber(List<ProductImage> images) {
        for (int i = 0; i < images.size(); i++) {
            images.get(i).setPosition(i);
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
