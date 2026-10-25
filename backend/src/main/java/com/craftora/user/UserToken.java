package com.craftora.user;

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

/** Single-use email verification / password reset token (hash only). */
@Entity
@Table(name = "user_token")
public class UserToken {

    public enum Type {
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected UserToken() {}

    public UserToken(User user, Type type, String tokenHash, Instant expiresAt) {
        this.user = user;
        this.type = type;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public boolean isUsable(Type expected, Instant now) {
        return type == expected && usedAt == null && expiresAt.isAfter(now);
    }

    public void markUsed() {
        usedAt = Instant.now();
    }

    public User getUser() {
        return user;
    }
}
