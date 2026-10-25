package com.craftora.common;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** Stable pagination envelope so clients never depend on Spring's {@code Page} JSON shape. */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    public static <T> PageResponse<T> of(List<T> items, int page, int size, long total) {
        return new PageResponse<>(items, page, size, total, size == 0 ? 0 : (int) Math.ceil((double) total / size));
    }

    /** Clamps client-supplied paging values to safe bounds. */
    public static int clampSize(Integer size, int defaultSize, int max) {
        if (size == null || size < 1) {
            return defaultSize;
        }
        return Math.min(size, max);
    }

    public static int clampPage(Integer page) {
        return page == null || page < 0 ? 0 : Math.min(page, 10_000);
    }
}
