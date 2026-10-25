package com.craftora.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Metadata only; the binary lives in object storage under {@link #storageKey}. */
@Entity
@Table(name = "product_image")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private int position;

    private String altText;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected ProductImage() {}

    public ProductImage(Product product, String storageKey, int position, String altText, String contentType,
                        long sizeBytes) {
        this.product = product;
        this.storageKey = storageKey;
        this.position = position;
        this.altText = altText;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    public UUID getId() {
        return id;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }
}
