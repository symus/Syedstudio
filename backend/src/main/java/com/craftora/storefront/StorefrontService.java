package com.craftora.storefront;

import com.craftora.common.ApiException;
import com.craftora.maker.MakerProfile;
import com.craftora.maker.MakerService;
import com.craftora.media.ImageUploads;
import com.craftora.media.MediaController;
import com.craftora.product.CatalogService;
import com.craftora.storefront.StorefrontDtos.StorefrontStateResponse;
import com.craftora.storefront.StorefrontDtos.StorefrontView;
import com.craftora.storefront.StorefrontDtos.TemplateResponse;
import com.craftora.subscription.PlanService;
import com.craftora.user.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorefrontService {

    private final StorefrontRepository storefronts;
    private final StorefrontTemplateRepository templates;
    private final StorefrontConfigCodec codec;
    private final MakerService makers;
    private final CatalogService catalog;
    private final PlanService plans;
    private final ImageUploads uploads;
    private final UserRepository users;

    public StorefrontService(StorefrontRepository storefronts, StorefrontTemplateRepository templates,
                             StorefrontConfigCodec codec, MakerService makers, CatalogService catalog,
                             PlanService plans, ImageUploads uploads, UserRepository users) {
        this.storefronts = storefronts;
        this.templates = templates;
        this.codec = codec;
        this.makers = makers;
        this.catalog = catalog;
        this.plans = plans;
        this.uploads = uploads;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public StorefrontStateResponse state(UUID userId) {
        return toState(own(userId));
    }

    @Transactional
    public StorefrontStateResponse saveDraft(UUID userId, StorefrontConfig config) {
        Storefront s = own(userId);
        templates.findById(config.templateKey()).filter(StorefrontTemplate::isActive)
                .orElseThrow(() -> ApiException.badRequest("unknown_template", "Unknown template"));
        StorefrontConfig current = codec.read(s.getDraftConfig());
        // The hero image key can only be set through the upload endpoint, never supplied by the client.
        if (config.heroImageKey() != null && !Objects.equals(config.heroImageKey(), current.heroImageKey())) {
            throw ApiException.badRequest("invalid_hero_image", "Upload the hero image through the upload endpoint");
        }
        List<StorefrontConfig.Section> distinct = config.sections().stream().distinct().toList();
        StorefrontConfig sanitized = new StorefrontConfig(config.templateKey(), config.primaryColor(),
                config.accentColor(), config.font(), config.heroTitle(), config.heroSubtitle(), config.heroImageKey(),
                config.aboutTitle(), config.aboutText(), config.featuredHeading(), distinct, config.language(),
                config.seoTitle(), config.seoDescription());
        s.setDraftConfig(codec.write(sanitized));
        return toState(s);
    }

    @Transactional
    public StorefrontStateResponse uploadHero(UUID userId, MultipartFile file) {
        Storefront s = own(userId);
        StorefrontConfig current = codec.read(s.getDraftConfig());
        var stored = uploads.store(file, "public/storefronts/" + s.getMaker().getId());
        s.setDraftConfig(codec.write(current.withHeroImageKey(stored.key())));
        // the previous image may still be referenced by the published config, so it is kept
        return toState(s);
    }

    @Transactional
    public StorefrontStateResponse publish(UUID userId) {
        Storefront s = own(userId);
        if (!users.findById(userId).orElseThrow().isEmailVerified()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "email_not_verified",
                    "Verify your email address before publishing your website");
        }
        s.publish();
        return toState(s);
    }

    @Transactional
    public StorefrontStateResponse unpublish(UUID userId) {
        Storefront s = own(userId);
        s.unpublish();
        return toState(s);
    }

    @Transactional(readOnly = true)
    public StorefrontView preview(UUID userId) {
        Storefront s = own(userId);
        return view(s, codec.read(s.getDraftConfig()), true);
    }

    @Transactional(readOnly = true)
    public StorefrontView published(String slug) {
        Storefront s = storefronts.findPublishedBySlug(slug).orElseThrow(() -> ApiException.notFound("Storefront"));
        return view(s, codec.read(s.getPublishedConfig()), false);
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> templates() {
        return templates.findByActiveTrueOrderByPositionAsc().stream()
                .map(t -> new TemplateResponse(t.getKey(), t.getName(), t.getDescription(),
                        t.getDefaultPrimaryColor(), t.getDefaultAccentColor(), t.getDefaultFont()))
                .toList();
    }

    private StorefrontView view(Storefront s, StorefrontConfig config, boolean preview) {
        MakerProfile m = s.getMaker();
        boolean branding = !plans.effectivePlan(m.getId()).isReducedBranding();
        return new StorefrontView(MakerService.toPublic(m, s.getStatus() == Storefront.Status.PUBLISHED), config,
                MediaController.url(config.heroImageKey()), catalog.storefrontProducts(m.getId()), branding, preview);
    }

    private Storefront own(UUID userId) {
        MakerProfile maker = makers.requireOwn(userId);
        return storefronts.findByMakerId(maker.getId()).orElseThrow(() -> ApiException.notFound("Storefront"));
    }

    private StorefrontStateResponse toState(Storefront s) {
        StorefrontConfig draft = codec.read(s.getDraftConfig());
        MakerProfile m = s.getMaker();
        return new StorefrontStateResponse(s.getStatus(), s.hasUnpublishedChanges(), s.getPublishedAt(), draft,
                MediaController.url(draft.heroImageKey()), "/" + m.getSlug(), m.isPubliclyVisible(),
                m.getUser().isEmailVerified(), templates());
    }
}
