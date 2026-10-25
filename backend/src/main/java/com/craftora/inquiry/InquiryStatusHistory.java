package com.craftora.inquiry;

import com.craftora.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inquiry_status_history")
public class InquiryStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inquiry_id")
    private CustomOrderInquiry inquiry;

    @Enumerated(EnumType.STRING)
    private InquiryStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus toStatus;

    private String customerMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected InquiryStatusHistory() {}

    InquiryStatusHistory(CustomOrderInquiry inquiry, InquiryStatus fromStatus, InquiryStatus toStatus,
                         String customerMessage, User changedBy) {
        this.inquiry = inquiry;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.customerMessage = customerMessage;
        this.changedBy = changedBy;
    }

    public InquiryStatus getFromStatus() {
        return fromStatus;
    }

    public InquiryStatus getToStatus() {
        return toStatus;
    }

    public String getCustomerMessage() {
        return customerMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
