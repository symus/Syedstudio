package com.craftora.maker;

import com.craftora.inquiry.InquiryRepository;
import com.craftora.inquiry.InquiryStatus;
import com.craftora.maker.MakerDtos.ActivityItem;
import com.craftora.maker.MakerDtos.MakerProfileRequest;
import com.craftora.maker.MakerDtos.MakerProfileResponse;
import com.craftora.maker.MakerDtos.OverviewResponse;
import com.craftora.maker.MakerDtos.SlugAvailability;
import com.craftora.product.ProductEnums;
import com.craftora.product.ProductRepository;
import com.craftora.security.AuthUser;
import com.craftora.storefront.Storefront;
import com.craftora.storefront.StorefrontRepository;
import com.craftora.subscription.PlanDtos.MySubscriptionResponse;
import com.craftora.subscription.PlanDtos.PlanResponse;
import com.craftora.subscription.PlanService;
import com.craftora.subscription.SubscriptionRepository;
import com.craftora.user.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Maker profile & dashboard")
@RestController
@RequestMapping("/api/maker")
public class MakerController {

    private final MakerService makers;
    private final ProductRepository products;
    private final InquiryRepository inquiries;
    private final StorefrontRepository storefronts;
    private final SubscriptionRepository subscriptions;
    private final PlanService plans;
    private final UserRepository users;

    public MakerController(MakerService makers, ProductRepository products, InquiryRepository inquiries,
                           StorefrontRepository storefronts, SubscriptionRepository subscriptions, PlanService plans,
                           UserRepository users) {
        this.makers = makers;
        this.products = products;
        this.inquiries = inquiries;
        this.storefronts = storefronts;
        this.subscriptions = subscriptions;
        this.plans = plans;
        this.users = users;
    }

    @GetMapping("/profile")
    public MakerProfileResponse profile(@AuthenticationPrincipal AuthUser user) {
        return makers.getOwn(user.id());
    }

    @PutMapping("/profile")
    public MakerProfileResponse save(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody MakerProfileRequest req) {
        return makers.save(user.id(), req);
    }

    @PostMapping(path = "/profile/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MakerProfileResponse logo(@AuthenticationPrincipal AuthUser user, @RequestPart("file") MultipartFile file) {
        return makers.uploadImage(user.id(), file, true);
    }

    @PostMapping(path = "/profile/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MakerProfileResponse cover(@AuthenticationPrincipal AuthUser user, @RequestPart("file") MultipartFile file) {
        return makers.uploadImage(user.id(), file, false);
    }

    @GetMapping("/slug-available")
    public SlugAvailability slugAvailable(@AuthenticationPrincipal AuthUser user, @RequestParam String slug) {
        return new SlugAvailability(slug, makers.isSlugAvailable(slug, user.id()));
    }

    @GetMapping("/overview")
    @Transactional(readOnly = true)
    public OverviewResponse overview(@AuthenticationPrincipal AuthUser user) {
        MakerProfile m = makers.requireOwn(user.id());
        Storefront s = storefronts.findByMakerId(m.getId()).orElseThrow();
        var plan = plans.effectivePlan(m.getId());
        List<ActivityItem> activity = new ArrayList<>();
        inquiries.findTop5ByMakerIdOrderByCreatedAtDesc(m.getId()).forEach(i ->
                activity.add(new ActivityItem("INQUIRY", i.getCustomerName(), i.getId(), i.getCreatedAt())));
        products.findByMakerIdOrderByUpdatedAtDesc(m.getId(), PageRequest.of(0, 5)).forEach(p ->
                activity.add(new ActivityItem("PRODUCT_" + p.getStatus().name(), p.getName(), p.getId(),
                        p.getUpdatedAt())));
        activity.sort(Comparator.comparing(ActivityItem::at).reversed());
        return new OverviewResponse(s.getStatus().name(), s.hasUnpublishedChanges(), m.getStatus(),
                users.findById(user.id()).orElseThrow().isEmailVerified(), products.countByMakerId(m.getId()),
                products.countByMakerIdAndStatus(m.getId(), ProductEnums.Status.PUBLISHED),
                inquiries.countByMakerIdAndStatus(m.getId(), InquiryStatus.NEW),
                inquiries.countByMakerIdAndStatusIn(m.getId(),
                        List.of(InquiryStatus.NEW, InquiryStatus.IN_PROGRESS, InquiryStatus.QUOTED, InquiryStatus.ACCEPTED)),
                plan.getCode(), plan.getName(), plan.getMaxPublishedProducts(), plan.getMaxImagesPerProduct(),
                activity.stream().limit(8).toList());
    }

    @GetMapping("/subscription")
    @Transactional(readOnly = true)
    public MySubscriptionResponse subscription(@AuthenticationPrincipal AuthUser user) {
        MakerProfile m = makers.requireOwn(user.id());
        var sub = subscriptions.findByMakerId(m.getId()).orElseThrow();
        var effective = plans.effectivePlan(m.getId());
        return new MySubscriptionResponse(plans.listActive().stream()
                .filter(p -> p.code().equals(effective.getCode())).findFirst().orElseThrow(),
                sub.getStatus().name(), products.countByMakerIdAndStatus(m.getId(), ProductEnums.Status.PUBLISHED));
    }

    @GetMapping("/plans")
    public List<PlanResponse> plans() {
        return plans.listActive();
    }
}
