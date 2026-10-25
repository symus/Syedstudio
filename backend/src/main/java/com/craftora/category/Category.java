package com.craftora.category;

import com.craftora.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/** Admin-managed product category with per-locale names (add a locale without schema changes). */
@Entity
@Table(name = "category")
public class Category extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "category_translation", joinColumns = @JoinColumn(name = "category_id"))
    @MapKeyColumn(name = "locale")
    @Column(name = "name")
    private Map<String, String> names = new HashMap<>();

    protected Category() {}

    public Category(String slug, int position) {
        this.slug = slug;
        this.position = position;
    }

    public String name(String locale, String fallbackLocale) {
        return names.getOrDefault(locale, names.getOrDefault(fallbackLocale, slug));
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(Map<String, String> names) {
        this.names.clear();
        this.names.putAll(names);
    }
}
