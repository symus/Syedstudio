package com.craftora.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "report")
public class Report {

    public enum TargetType { PRODUCT, MAKER }

    public enum Reason { PROHIBITED_ITEM, COUNTERFEIT, FRAUD, OFFENSIVE, INTELLECTUAL_PROPERTY, SPAM, OTHER }

    public enum Status { OPEN, RESOLVED, DISMISSED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    @Column(nullable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Reason reason;

    private String details;

    private UUID reporterUserId;

    private String reporterEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    private String resolutionNote;

    private UUID resolvedBy;

    private Instant resolvedAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Report() {}

    public Report(TargetType targetType, UUID targetId, Reason reason, String details, UUID reporterUserId,
                  String reporterEmail) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.details = details;
        this.reporterUserId = reporterUserId;
        this.reporterEmail = reporterEmail;
    }

    public void resolve(Status outcome, String note, UUID adminId) {
        status = outcome;
        resolutionNote = note;
        resolvedBy = adminId;
        resolvedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public Reason getReason() {
        return reason;
    }

    public String getDetails() {
        return details;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public Status getStatus() {
        return status;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
