package com.craftora.subscription;

import jakarta.validation.constraints.NotBlank;

public final class PlanDtos {

    private PlanDtos() {}

    public record PlanResponse(String code, String name, long priceMinor, String currency, String billingInterval,
                               int maxPublishedProducts, int maxImagesPerProduct, boolean customDomain,
                               boolean analytics, boolean reducedBranding) {

        static PlanResponse of(SubscriptionPlan p) {
            return new PlanResponse(p.getCode(), p.getName(), p.getPriceMinor(), p.getCurrency(),
                    p.getBillingInterval(), p.getMaxPublishedProducts(), p.getMaxImagesPerProduct(),
                    p.isCustomDomain(), p.isAnalytics(), p.isReducedBranding());
        }
    }

    public record MySubscriptionResponse(PlanResponse plan, String status, long publishedProducts) {}

    public record ChangePlanRequest(@NotBlank String planCode) {}
}
