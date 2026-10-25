package com.craftora.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Immutable record of a sensitive administrative action. Never stores passwords, tokens or personal data. */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID actorUserId;

    @Column(nullable = false)
    private String action;

    private String targetType;

    private UUID targetId;

    private String details;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected AuditLog() {}

    public AuditLog(UUID actorUserId, String action, String targetType, UUID targetId, String details) {
        this.actorUserId = actorUserId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details == null || details.length() <= 500 ? details : details.substring(0, 500);
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public String getAction() {
        return action;
    }

    public String getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getDetails() {
        return details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
