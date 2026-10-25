package com.craftora.category;

import com.craftora.category.CategoryDtos.AdminCategoryResponse;
import com.craftora.category.CategoryDtos.CategoryRequest;
import com.craftora.category.CategoryDtos.CategoryResponse;
import com.craftora.category.CategoryDtos.ReorderRequest;
import com.craftora.security.AuthUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Categories")
@RestController
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping("/api/public/categories")
    public List<CategoryResponse> list(@RequestParam(defaultValue = "pl") String locale) {
        return service.listPublic(locale);
    }

    @GetMapping("/api/admin/categories")
    public List<AdminCategoryResponse> adminList() {
        return service.listAll();
    }

    @PostMapping("/api/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCategoryResponse create(@Valid @RequestBody CategoryRequest req, @AuthenticationPrincipal AuthUser user) {
        return service.create(req, user.id());
    }

    @PutMapping("/api/admin/categories/{id}")
    public AdminCategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest req,
                                        @AuthenticationPrincipal AuthUser user) {
        return service.update(id, req, user.id());
    }

    @PutMapping("/api/admin/categories/order")
    public List<AdminCategoryResponse> reorder(@Valid @RequestBody ReorderRequest req,
                                               @AuthenticationPrincipal AuthUser user) {
        return service.reorder(req.ids(), user.id());
    }
}
