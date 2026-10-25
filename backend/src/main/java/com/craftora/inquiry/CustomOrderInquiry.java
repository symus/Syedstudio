package com.craftora.inquiry;

import com.craftora.common.BaseEntity;
import com.craftora.maker.MakerProfile;
import com.craftora.product.Product;
import com.craftora.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "custom_order_inquiry")
public class CustomOrderInquiry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maker_id")
    private MakerProfile maker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_user_id")
    private User customer;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    private String customerPhone;

    @Column(nullable = false)
    private String message;

    private Long budgetMinor;

    @Column(nullable = false)
    private String currency = "PLN";

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status = InquiryStatus.NEW;

    /** Maker-only notes. Never included in any customer-facing response or e-mail. */
    private String internalNotes;

    @Column(nullable = false)
    private Instant consentAt;

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt asc")
    private List<InquiryAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt asc")
    private List<InquiryStatusHistory> history = new ArrayList<>();

    protected CustomOrderInquiry() {}

    public CustomOrderInquiry(MakerProfile maker, Product product, User customer, String customerName,
                              String customerEmail, String customerPhone, String message, Long budgetMinor,
                              String currency, LocalDate deadline) {
        this.maker = maker;
        this.product = product;
        this.customer = customer;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.message = message;
        this.budgetMinor = budgetMinor;
        this.currency = currency;
        this.deadline = deadline;
        this.consentAt = Instant.now();
        this.history.add(new InquiryStatusHistory(this, null, InquiryStatus.NEW, null, null));
    }

    public void changeStatus(InquiryStatus next, String customerMessage, User actor) {
        history.add(new InquiryStatusHistory(this, status, next, customerMessage, actor));
        status = next;
    }

    public MakerProfile getMaker() {
        return maker;
    }

    public Product getProduct() {
        return product;
    }

    public User getCustomer() {
        return customer;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getMessage() {
        return message;
    }

    public Long getBudgetMinor() {
        return budgetMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public InquiryStatus getStatus() {
        return status;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public Instant getConsentAt() {
        return consentAt;
    }

    public List<InquiryAttachment> getAttachments() {
        return attachments;
    }

    public List<InquiryStatusHistory> getHistory() {
        return history;
    }
}
