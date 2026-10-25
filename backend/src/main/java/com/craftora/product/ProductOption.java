package com.craftora.product;

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
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Personalisation field shown to customers (e.g. "Engraving text" or "Colour: red/blue"). */
@Entity
@Table(name = "product_option")
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductEnums.OptionType type;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> choices;

    private boolean required;

    private int position;

    protected ProductOption() {}

    public ProductOption(Product product, String name, ProductEnums.OptionType type, List<String> choices,
                         boolean required, int position) {
        this.product = product;
        this.name = name;
        this.type = type;
        this.choices = choices;
        this.required = required;
        this.position = position;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProductEnums.OptionType getType() {
        return type;
    }

    public List<String> getChoices() {
        return choices == null ? List.of() : choices;
    }

    public boolean isRequired() {
        return required;
    }

    public int getPosition() {
        return position;
    }
}
