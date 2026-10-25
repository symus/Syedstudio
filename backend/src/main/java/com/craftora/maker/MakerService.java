package com.craftora.maker;

import com.craftora.category.CategoryRepository;
import com.craftora.common.ApiException;
import com.craftora.common.AppProperties;
import com.craftora.common.Slugs;
import com.craftora.maker.MakerDtos.MakerCard;
import com.craftora.maker.MakerDtos.MakerProfileRequest;
import com.craftora.maker.MakerDtos.MakerProfileResponse;
import com.craftora.maker.MakerDtos.PublicMakerResponse;
import com.craftora.maker.MakerDtos.SellerInfo;
import com.craftora.maker.MakerDtos.SocialLinks;
import com.craftora.media.ImageUploads;
import com.craftora.media.MediaController;
import com.craftora.storefront.Storefront;
import com.craftora.storefront.StorefrontConfig;
import com.craftora.storefront.StorefrontConfigCodec;
import com.craftora.storefront.StorefrontRepository;
import com.craftora.storefront.StorefrontTemplateRepository;
import com.craftora.subscription.PlanService;
import com.craftora.user.Role;
import com.craftora.user.User;
import com.craftora.user.UserRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MakerService {

    private final MakerProfileRepository makers;
    private final UserRepository users;
    private final CategoryRepository categories;
    private final StorefrontRepository storefronts;
    private final StorefrontTemplateRepository templates;
    private final StorefrontConfigCodec codec;
    private final PlanService plans;
    private final ImageUploads uploads;
    private final AppProperties props;

    public MakerService(MakerProfileRepository makers, UserRepository users, CategoryRepository categories,
                        StorefrontRepository storefronts, StorefrontTemplateRepository templates,
                        StorefrontConfigCodec codec, PlanService plans, ImageUploads uploads, AppProperties props) {
        this.makers = makers;
        this.users = users;
        this.categories = categories;
        this.storefronts = storefronts;
        this.templates = templates;
        this.codec = codec;
        this.plans = plans;
        this.uploads = uploads;
        this.props = props;
    }

    /** Resolves the caller's own maker profile: the tenant boundary for every /api/maker operation. */
    @Transactional(readOnly = true)
    public MakerProfile requireOwn(UUID userId) {
        return makers.findByUserId(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                "maker_profile_missing", "Create your maker profile first"));
    }

    @Transactional(readOnly = true)
    public MakerProfileResponse getOwn(UUID userId) {
        return toOwner(requireOwn(userId));
    }

    @Transactional(readOnly = true)
    public boolean isSlugAvailable(String slug, UUID userId) {
        if (!Slugs.isValidStoreSlug(slug)) {
            return false;
        }
        return makers.findBySlug(slug).map(m -> m.getUser().getId().equals(userId)).orElse(true);
    }

    @Transactional
    public MakerProfileResponse save(UUID userId, MakerProfileRequest req) {
        if (!Slugs.isValidStoreSlug(req.slug())) {
            throw ApiException.badRequest("slug_reserved", "This web address is not available");
        }
        MakerProfile maker = makers.findByUserId(userId).orElse(null);
        makers.findBySlug(req.slug())
                .filter(other -> maker == null || !other.getId().equals(maker.getId()))
                .ifPresent(other -> {
                    throw ApiException.conflict("slug_taken", "This web address is already taken");
                });
        MakerProfile target = maker != null ? maker : create(userId, req);
        apply(target, req);
        return toOwner(target);
    }

    private MakerProfile create(UUID userId, MakerProfileRequest req) {
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        if (!user.hasRole(Role.MAKER)) {
            throw ApiException.forbidden("Maker role required");
        }
        MakerProfile maker = new MakerProfile(user, req.slug(), req.businessName().trim());
        if (props.makers().autoApprove()) {
            maker.approve();
        }
        makers.save(maker);
        var template = templates.findByActiveTrueOrderByPositionAsc().getFirst();
        var config = StorefrontConfig.defaults(template, req.businessName().trim(), req.tagline(),
                user.getPreferredLocale());
        storefronts.save(new Storefront(maker, codec.write(config)));
        plans.startFree(maker);
        return maker;
    }

    private void apply(MakerProfile m, MakerProfileRequest r) {
        m.setSlug(r.slug());
        m.setBusinessName(r.businessName().trim());
        m.setTagline(blankToNull(r.tagline()));
        m.setBio(blankToNull(r.bio()));
        m.setCity(blankToNull(r.city()));
        m.setRegion(blankToNull(r.region()));
        m.setCountryCode(r.countryCode() == null ? "PL" : r.countryCode());
        m.setShowLocation(r.showLocation());
        m.setContactEmail(blankToNull(r.contactEmail()));
        m.setPhone(blankToNull(r.phone()));
        m.setWebsiteUrl(blankToNull(r.websiteUrl()));
        m.setInstagramUrl(blankToNull(r.instagramUrl()));
        m.setFacebookUrl(blankToNull(r.facebookUrl()));
        m.setTiktokUrl(blankToNull(r.tiktokUrl()));
        m.setPinterestUrl(blankToNull(r.pinterestUrl()));
        m.setAcceptsCustomOrders(r.acceptsCustomOrders());
        m.setLegalName(blankToNull(r.legalName()));
        m.setTaxId(blankToNull(r.taxId()));
        m.setBusinessAddress(blankToNull(r.businessAddress()));
        m.setPrimaryCategory(r.primaryCategoryId() == null ? null : categories.findById(r.primaryCategoryId())
                .orElseThrow(() -> ApiException.badRequest("unknown_category", "Unknown category")));
    }

    @Transactional
    public MakerProfileResponse uploadImage(UUID userId, MultipartFile file, boolean logo) {
        MakerProfile m = requireOwn(userId);
        var stored = uploads.store(file, "public/makers/" + m.getId());
        String previous = logo ? m.getLogoKey() : m.getCoverKey();
        if (logo) {
            m.setLogoKey(stored.key());
        } else {
            m.setCoverKey(stored.key());
        }
        uploads.deleteQuietly(previous);
        return toOwner(m);
    }

    @Transactional(readOnly = true)
    public PublicMakerResponse getPublic(String slug) {
        MakerProfile m = makers.findBySlug(slug).filter(MakerProfile::isPubliclyVisible)
                .orElseThrow(() -> ApiException.notFound("Maker"));
        boolean hasStorefront = storefronts.findByMakerId(m.getId())
                .map(s -> s.getStatus() == Storefront.Status.PUBLISHED).orElse(false);
        return toPublic(m, hasStorefront);
    }

    public static PublicMakerResponse toPublic(MakerProfile m, boolean hasStorefront) {
        SellerInfo seller = m.getLegalName() == null && m.getTaxId() == null && m.getBusinessAddress() == null
                ? null : new SellerInfo(m.getLegalName(), m.getTaxId(), m.getBusinessAddress());
        return new PublicMakerResponse(m.getId(), m.getSlug(), m.getBusinessName(), m.getTagline(), m.getBio(),
                m.isShowLocation() ? m.getCity() : null, m.isShowLocation() ? m.getRegion() : null,
                m.getCountryCode(), MediaController.url(m.getLogoKey()), MediaController.url(m.getCoverKey()),
                m.getContactEmail(), m.getPhone(),
                new SocialLinks(m.getWebsiteUrl(), m.getInstagramUrl(), m.getFacebookUrl(), m.getTiktokUrl(),
                        m.getPinterestUrl()),
                m.isAcceptsCustomOrders(),
                m.getPrimaryCategory() == null ? null : m.getPrimaryCategory().getSlug(),
                seller, hasStorefront, m.getApprovedAt());
    }

    public static MakerCard toCard(MakerProfile m, long productCount) {
        return new MakerCard(m.getId(), m.getSlug(), m.getBusinessName(), m.getTagline(),
                m.isShowLocation() ? m.getCity() : null, MediaController.url(m.getLogoKey()),
                MediaController.url(m.getCoverKey()), productCount);
    }

    static MakerProfileResponse toOwner(MakerProfile m) {
        return new MakerProfileResponse(m.getId(), m.getSlug(), m.getBusinessName(), m.getTagline(), m.getBio(),
                m.getCity(), m.getRegion(), m.getCountryCode(), m.isShowLocation(),
                MediaController.url(m.getLogoKey()), MediaController.url(m.getCoverKey()), m.getContactEmail(),
                m.getPhone(), m.getWebsiteUrl(), m.getInstagramUrl(), m.getFacebookUrl(), m.getTiktokUrl(),
                m.getPinterestUrl(), m.isAcceptsCustomOrders(),
                m.getPrimaryCategory() == null ? null : m.getPrimaryCategory().getId(), m.getLegalName(),
                m.getTaxId(), m.getBusinessAddress(), m.getStatus(), m.getCreatedAt());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
