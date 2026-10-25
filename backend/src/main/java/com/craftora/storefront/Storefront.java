package com.craftora.storefront;

import com.craftora.common.BaseEntity;
import com.craftora.maker.MakerProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "storefront")
public class Storefront extends BaseEntity {

    public enum Status { DRAFT, PUBLISHED, UNPUBLISHED }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maker_id")
    private MakerProfile maker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;

    /** JSON-serialised {@link StorefrontConfig}. */
    @Column(nullable = false)
    private String draftConfig;

    private String publishedConfig;

    private Instant publishedAt;

    protected Storefront() {}

    public Storefront(MakerProfile maker, String draftConfig) {
        this.maker = maker;
        this.draftConfig = draftConfig;
    }

    public void publish() {
        publishedConfig = draftConfig;
        status = Status.PUBLISHED;
        publishedAt = Instant.now();
    }

    public void unpublish() {
        status = Status.UNPUBLISHED;
    }

    public boolean hasUnpublishedChanges() {
        return status != Status.PUBLISHED || !draftConfig.equals(publishedConfig);
    }

    public MakerProfile getMaker() {
        return maker;
    }

    public Status getStatus() {
        return status;
    }

    public String getDraftConfig() {
        return draftConfig;
    }

    public void setDraftConfig(String draftConfig) {
        this.draftConfig = draftConfig;
    }

    public String getPublishedConfig() {
        return publishedConfig;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
