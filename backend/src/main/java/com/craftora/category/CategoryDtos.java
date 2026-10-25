package com.craftora.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CategoryDtos {

    private CategoryDtos() {}

    public record CategoryResponse(UUID id, String slug, String name, long productCount) {}

    public record AdminCategoryResponse(UUID id, String slug, Map<String, String> names, int position, boolean active) {}

    public record CategoryRequest(
            @NotBlank @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$") @Size(max = 80) String slug,
            @NotEmpty Map<@Pattern(regexp = "^[a-z]{2}$") String, @NotBlank @Size(max = 100) String> names,
            boolean active) {}

    public record ReorderRequest(@NotEmpty List<UUID> ids) {}
}
