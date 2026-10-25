package com.craftora.subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Plan definitions and limits live in the database so prices/limits change without a deploy. */
@Entity
@Table(name = "subscription_plan")
public class SubscriptionPlan {

    public static final String FREE = "FREE";

    @Id
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long priceMinor;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String billingInterval;

    @Column(nullable = false)
    private int maxPublishedProducts;

    @Column(nullable = false)
    private int maxImagesPerProduct;

    private boolean customDomain;
    private boolean analytics;
    private boolean reducedBranding;
    private int position;
    private boolean active;

    protected SubscriptionPlan() {}

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public long getPriceMinor() {
        return priceMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBillingInterval() {
        return billingInterval;
    }

    public int getMaxPublishedProducts() {
        return maxPublishedProducts;
    }

    public int getMaxImagesPerProduct() {
        return maxImagesPerProduct;
    }

    public boolean isCustomDomain() {
        return customDomain;
    }

    public boolean isAnalytics() {
        return analytics;
    }

    public boolean isReducedBranding() {
        return reducedBranding;
    }

    public int getPosition() {
        return position;
    }

    public boolean isActive() {
        return active;
    }
}
