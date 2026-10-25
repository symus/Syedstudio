package com.craftora.storefront;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "storefront_template")
public class StorefrontTemplate {

    @Id
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String defaultPrimaryColor;

    @Column(nullable = false)
    private String defaultAccentColor;

    @Column(nullable = false)
    private String defaultFont;

    private int position;

    private boolean active;

    protected StorefrontTemplate() {}

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultPrimaryColor() {
        return defaultPrimaryColor;
    }

    public String getDefaultAccentColor() {
        return defaultAccentColor;
    }

    public String getDefaultFont() {
        return defaultFont;
    }

    public int getPosition() {
        return position;
    }

    public boolean isActive() {
        return active;
    }
}
