package com.craftora.subscription;

import com.craftora.common.ApiException;
import com.craftora.maker.MakerProfile;
import com.craftora.subscription.PlanDtos.PlanResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Single place where plan limits are resolved and enforced (server-side, so the UI cannot bypass them). */
@Service
public class PlanService {

    private final SubscriptionPlanRepository plans;
    private final SubscriptionRepository subscriptions;

    public PlanService(SubscriptionPlanRepository plans, SubscriptionRepository subscriptions) {
        this.plans = plans;
        this.subscriptions = subscriptions;
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listActive() {
        return plans.findByActiveTrueOrderByPositionAsc().stream().map(PlanResponse::of).toList();
    }

    @Transactional
    public Subscription startFree(MakerProfile maker) {
        return subscriptions.save(new Subscription(maker, freePlan()));
    }

    /** Plan whose limits currently apply (Free when the paid subscription is not in good standing). */
    @Transactional(readOnly = true)
    public SubscriptionPlan effectivePlan(UUID makerId) {
        return subscriptions.findByMakerId(makerId)
                .filter(Subscription::isEntitled)
                .map(Subscription::getPlan)
                .orElseGet(this::freePlan);
    }

    public void assertCanPublish(UUID makerId, long currentlyPublished) {
        SubscriptionPlan plan = effectivePlan(makerId);
        if (currentlyPublished >= plan.getMaxPublishedProducts()) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "plan_limit_products",
                    "Your %s plan allows %d published products. Upgrade to publish more."
                            .formatted(plan.getName(), plan.getMaxPublishedProducts()));
        }
    }

    public void assertCanAddImage(UUID makerId, int currentImages) {
        SubscriptionPlan plan = effectivePlan(makerId);
        if (currentImages >= plan.getMaxImagesPerProduct()) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "plan_limit_images",
                    "Your %s plan allows %d images per product.".formatted(plan.getName(), plan.getMaxImagesPerProduct()));
        }
    }

    /** Manual plan assignment for the pilot (admin only) until payment integration lands in Phase 2. */
    @Transactional
    public void changePlan(UUID makerId, String planCode) {
        SubscriptionPlan plan = plans.findById(planCode).filter(SubscriptionPlan::isActive)
                .orElseThrow(() -> ApiException.badRequest("unknown_plan", "Unknown plan"));
        Subscription sub = subscriptions.findByMakerId(makerId).orElseThrow(() -> ApiException.notFound("Subscription"));
        sub.setPlan(plan);
        sub.setStatus(Subscription.Status.ACTIVE);
    }

    private SubscriptionPlan freePlan() {
        return plans.findById(SubscriptionPlan.FREE).orElseThrow();
    }
}
