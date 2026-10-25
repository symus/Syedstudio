package com.craftora.subscription;

import com.craftora.common.BaseEntity;
import com.craftora.maker.MakerProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "subscription")
public class Subscription extends BaseEntity {

    public enum Status { ACTIVE, TRIALING, PAST_DUE, CANCELED }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maker_id")
    private MakerProfile maker;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "plan_code")
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    private Instant currentPeriodEnd;

    protected Subscription() {}

    public Subscription(MakerProfile maker, SubscriptionPlan plan) {
        this.maker = maker;
        this.plan = plan;
    }

    /** Lapsed paid plans fall back to Free limits so restrictions cannot be bypassed by non-payment. */
    public boolean isEntitled() {
        return status == Status.ACTIVE || status == Status.TRIALING;
    }

    public MakerProfile getMaker() {
        return maker;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }
}
