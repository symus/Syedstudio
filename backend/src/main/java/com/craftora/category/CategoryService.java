package com.craftora.category;

import com.craftora.admin.AuditService;
import com.craftora.category.CategoryDtos.AdminCategoryResponse;
import com.craftora.category.CategoryDtos.CategoryRequest;
import com.craftora.category.CategoryDtos.CategoryResponse;
import com.craftora.common.ApiException;
import com.craftora.common.AppProperties;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final JdbcClient jdbc;
    private final AuditService audit;
    private final AppProperties props;

    public CategoryService(CategoryRepository repository, JdbcClient jdbc, AuditService audit, AppProperties props) {
        this.repository = repository;
        this.jdbc = jdbc;
        this.audit = audit;
        this.props = props;
    }

    /** Active categories with the number of publicly visible products in each. */
    @Transactional(readOnly = true)
    public List<CategoryResponse> listPublic(String locale) {
        Map<UUID, Long> counts = jdbc.sql("""
                        select p.category_id, count(*) from product p join maker_profile m on m.id = p.maker_id
                        where p.status = 'PUBLISHED' and p.moderation_status = 'VISIBLE' and m.status = 'APPROVED'
                          and p.category_id is not null
                        group by p.category_id
                        """)
                .query((rs, n) -> Map.entry(rs.getObject(1, UUID.class), rs.getLong(2)))
                .list().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return repository.findByActiveTrueOrderByPositionAsc().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getSlug(), c.name(locale, props.defaultLocale()),
                        counts.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminCategoryResponse> listAll() {
        return repository.findAllByOrderByPositionAsc().stream().map(CategoryService::toAdmin).toList();
    }

    @Transactional
    public AdminCategoryResponse create(CategoryRequest req, UUID actorId) {
        if (repository.existsBySlug(req.slug())) {
            throw ApiException.conflict("slug_taken", "A category with this slug already exists");
        }
        int position = repository.findAllByOrderByPositionAsc().size();
        Category c = new Category(req.slug(), position);
        c.setNames(req.names());
        c.setActive(req.active());
        repository.save(c);
        audit.record(actorId, "CATEGORY_CREATED", "CATEGORY", c.getId(), req.slug());
        return toAdmin(c);
    }

    @Transactional
    public AdminCategoryResponse update(UUID id, CategoryRequest req, UUID actorId) {
        Category c = repository.findById(id).orElseThrow(() -> ApiException.notFound("Category"));
        repository.findBySlug(req.slug()).filter(other -> !other.getId().equals(id)).ifPresent(other -> {
            throw ApiException.conflict("slug_taken", "A category with this slug already exists");
        });
        c.setSlug(req.slug());
        c.setNames(req.names());
        c.setActive(req.active());
        audit.record(actorId, "CATEGORY_UPDATED", "CATEGORY", id, req.slug() + (req.active() ? "" : " (inactive)"));
        return toAdmin(c);
    }

    @Transactional
    public List<AdminCategoryResponse> reorder(List<UUID> ids, UUID actorId) {
        List<Category> all = repository.findAllById(ids);
        if (all.size() != ids.size()) {
            throw ApiException.badRequest("unknown_category", "Unknown category in ordering");
        }
        Map<UUID, Category> byId = all.stream().collect(java.util.stream.Collectors.toMap(Category::getId, c -> c));
        for (int i = 0; i < ids.size(); i++) {
            byId.get(ids.get(i)).setPosition(i);
        }
        audit.record(actorId, "CATEGORIES_REORDERED", "CATEGORY", null, null);
        return listAll();
    }

    private static AdminCategoryResponse toAdmin(Category c) {
        return new AdminCategoryResponse(c.getId(), c.getSlug(), Map.copyOf(c.getNames()), c.getPosition(), c.isActive());
    }
}
