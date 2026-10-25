package com.craftora.product;

import com.craftora.product.ProductEnums.Occasion;

/** Marketplace filters. All optional; {@code sort} is one of relevance | newest | price_asc | price_desc. */
public record SearchCriteria(String q, String category, String city, Long minPrice, Long maxPrice, Occasion occasion,
                             String availability, Boolean customizable, String maker, String sort, int page,
                             int size) {}
