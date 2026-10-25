package com.craftora.product;

import com.craftora.common.ApiException;
import com.craftora.common.AppProperties;
import com.craftora.common.PageResponse;
import com.craftora.maker.MakerDtos.MakerCard;
import com.craftora.maker.MakerProfile;
import com.craftora.maker.MakerProfileRepository;
import com.craftora.maker.MakerService;
import com.craftora.maker.MakerStatus;
import com.craftora.product.ProductDtos.ProductCard;
import com.craftora.product.ProductDtos.ProductMaker;
import com.craftora.product.ProductDtos.PublicProductResponse;
import com.craftora.storefront.Storefront;
import com.craftora.storefront.StorefrontRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Public, read-only discovery: marketplace search, product detail and maker directory.
 * Search runs as one SQL statement (PostgreSQL full-text + filters) returning ids, then hydrates via JPA.
 */
@Service
public class CatalogService {

    static final String VISIBLE = """
            p.status = 'PUBLISHED' and p.moderation_status = 'VISIBLE' and m.status = 'APPROVED'""";

    private final JdbcClient jdbc;
    private final ProductRepository products;
    private final MakerProfileRepository makers;
    private final StorefrontRepository storefronts;
    private final AppProperties props;

    public CatalogService(JdbcClient jdbc, ProductRepository products, MakerProfileRepository makers,
                          StorefrontRepository storefronts, AppProperties props) {
        this.jdbc = jdbc;
        this.products = products;
        this.makers = makers;
        this.storefronts = storefronts;
        this.props = props;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductCard> search(SearchCriteria c) {
        StringBuilder where = new StringBuilder(VISIBLE);
        Map<String, Object> params = new HashMap<>();
        String q = c.q() == null ? null : c.q().trim();
        boolean hasQuery = q != null && !q.isEmpty();
        if (hasQuery) {
            params.put("q", q);
            params.put("like", "%" + escapeLike(q.toLowerCase()) + "%");
            where.append("""
                     and (p.search_vector @@ websearch_to_tsquery('simple', :q)
                          or lower(p.name) like :like escape '\\'
                          or lower(m.business_name) like :like escape '\\'
                          or exists (select 1 from product_tag pt join tag t on t.id = pt.tag_id
                                     where pt.product_id = p.id and lower(t.name) like :like escape '\\'))""");
        }
        if (notBlank(c.category())) {
            params.put("category", c.category());
            where.append(" and p.category_id = (select id from category where slug = :category)");
        }
        if (notBlank(c.city())) {
            params.put("city", c.city().trim().toLowerCase());
            where.append(" and m.show_location and lower(m.city) = :city");
        }
        if (c.minPrice() != null) {
            params.put("minPrice", c.minPrice());
            where.append(" and p.price_minor >= :minPrice");
        }
        if (c.maxPrice() != null) {
            params.put("maxPrice", c.maxPrice());
            where.append(" and p.price_minor <= :maxPrice");
        }
        if (c.occasion() != null) {
            params.put("occasion", c.occasion().name());
            where.append(" and exists (select 1 from product_occasion po where po.product_id = p.id and po.occasion = :occasion)");
        }
        if ("in_stock".equals(c.availability())) {
            where.append(" and p.available and p.availability_type = 'READY_MADE'");
        } else if ("made_to_order".equals(c.availability())) {
            where.append(" and p.available and p.availability_type = 'MADE_TO_ORDER'");
        } else if ("available".equals(c.availability())) {
            where.append(" and p.available");
        }
        if (Boolean.TRUE.equals(c.customizable())) {
            where.append(" and p.customizable");
        }
        if (notBlank(c.maker())) {
            params.put("maker", c.maker());
            where.append(" and m.slug = :maker");
        }
        String order = switch (c.sort() == null ? "" : c.sort()) {
            case "newest" -> "p.published_at desc";
            case "price_asc" -> "p.price_minor asc nulls last, p.published_at desc";
            case "price_desc" -> "p.price_minor desc nulls last, p.published_at desc";
            default -> hasQuery
                    ? "ts_rank(p.search_vector, websearch_to_tsquery('simple', :q)) desc, p.published_at desc"
                    : "m.featured desc, p.published_at desc";
        };
        params.put("limit", c.size());
        params.put("offset", (long) c.page() * c.size());
        String sql = "select p.id, count(*) over () as total from product p join maker_profile m on m.id = p.maker_id"
                + " where " + where + " order by " + order + ", p.id limit :limit offset :offset";

        long[] total = {0};
        List<UUID> ids = jdbc.sql(sql).params(params).query((rs, n) -> {
            total[0] = rs.getLong("total");
            return rs.getObject("id", UUID.class);
        }).list();
        if (ids.isEmpty() && c.page() > 0) {
            total[0] = countOnly(where.toString(), params);
        }
        return PageResponse.of(hydrate(ids), c.page(), c.size(), total[0]);
    }

    private long countOnly(String where, Map<String, Object> params) {
        return jdbc.sql("select count(*) from product p join maker_profile m on m.id = p.maker_id where " + where)
                .params(params).query(Long.class).single();
    }

    @Transactional(readOnly = true)
    public PublicProductResponse product(String makerSlug, String productSlug, String locale) {
        Product p = products.findByMakerSlugAndSlug(makerSlug, productSlug).filter(Product::isPubliclyVisible)
                .orElseThrow(() -> ApiException.notFound("Product"));
        MakerProfile m = p.getMaker();
        boolean hasStorefront = storefronts.findByMakerId(m.getId())
                .map(s -> s.getStatus() == Storefront.Status.PUBLISHED).orElse(false);
        var pub = MakerService.toPublic(m, hasStorefront);
        var maker = new ProductMaker(m.getSlug(), m.getBusinessName(), pub.logoUrl(), pub.city(),
                m.isAcceptsCustomOrders(), pub.seller(), hasStorefront);
        List<ProductCard> fromMaker = search(new SearchCriteria(null, null, null, null, null, null, null, null,
                m.getSlug(), "newest", 0, 5)).items().stream().filter(x -> !x.id().equals(p.getId())).limit(4).toList();
        List<ProductCard> related = p.getCategory() == null ? List.of()
                : search(new SearchCriteria(null, p.getCategory().getSlug(), null, null, null, null, null, null, null,
                "newest", 0, 9)).items().stream()
                .filter(x -> !x.id().equals(p.getId()) && !x.makerSlug().equals(m.getSlug())).limit(4).toList();
        var cat = p.getCategory();
        return new PublicProductResponse(p.getId(), p.getSlug(), p.getName(), p.getDescription(),
                cat == null ? null : cat.getSlug(), cat == null ? null : cat.name(locale, props.defaultLocale()),
                p.getPriceMinor(), p.getCurrency(), ProductMapper.tags(p), p.getOccasions(), p.getAvailabilityType(),
                p.getProductionDays(), p.isAvailable(), p.isCustomizable(), p.getMaterials(), p.getDimensions(),
                p.getShippingInfo(), ProductMapper.images(p), ProductMapper.options(p), maker, fromMaker, related,
                p.getPublishedAt());
    }

    /** Storefront product list: maker-selected, publicly visible products. */
    @Transactional(readOnly = true)
    public List<ProductCard> storefrontProducts(UUID makerId) {
        List<UUID> ids = jdbc.sql("select p.id from product p join maker_profile m on m.id = p.maker_id where "
                        + "p.maker_id = :maker and p.show_on_storefront and p.status = 'PUBLISHED' "
                        + "and p.moderation_status = 'VISIBLE' order by p.published_at desc limit 200")
                .param("maker", makerId).query(UUID.class).list();
        return hydrate(ids);
    }

    @Transactional(readOnly = true)
    public PageResponse<MakerCard> makers(String q, String city, String category, int page, int size) {
        StringBuilder where = new StringBuilder("m.status = 'APPROVED'");
        Map<String, Object> params = new HashMap<>();
        if (notBlank(q)) {
            params.put("like", "%" + escapeLike(q.trim().toLowerCase()) + "%");
            where.append(" and (lower(m.business_name) like :like escape '\\' or lower(coalesce(m.tagline, '')) like :like escape '\\')");
        }
        if (notBlank(city)) {
            params.put("city", city.trim().toLowerCase());
            where.append(" and m.show_location and lower(m.city) = :city");
        }
        if (notBlank(category)) {
            params.put("category", category);
            where.append("""
                     and (m.primary_category_id = (select id from category where slug = :category)
                          or exists (select 1 from product p join category c on c.id = p.category_id
                                     where p.maker_id = m.id and c.slug = :category and p.status = 'PUBLISHED'
                                       and p.moderation_status = 'VISIBLE'))""");
        }
        params.put("limit", size);
        params.put("offset", (long) page * size);
        long[] total = {0};
        List<UUID> ids = jdbc.sql("select m.id, count(*) over () as total from maker_profile m where " + where
                        + " order by m.featured desc, m.approved_at desc, m.id limit :limit offset :offset")
                .params(params).query((rs, n) -> {
                    total[0] = rs.getLong("total");
                    return rs.getObject("id", UUID.class);
                }).list();
        return PageResponse.of(makerCards(ids), page, size, total[0]);
    }

    @Transactional(readOnly = true)
    public List<MakerCard> makerCards(List<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<UUID, MakerProfile> byId = makers.findAllById(ids).stream()
                .collect(Collectors.toMap(MakerProfile::getId, Function.identity()));
        Map<UUID, Long> counts = products.countVisibleByMakers(ids);
        return ids.stream().map(byId::get).filter(m -> m != null && m.getStatus() == MakerStatus.APPROVED)
                .map(m -> MakerService.toCard(m, counts.getOrDefault(m.getId(), 0L))).toList();
    }

    @Transactional(readOnly = true)
    public List<String> cities() {
        return jdbc.sql("""
                        select min(m.city) from maker_profile m
                        where m.status = 'APPROVED' and m.show_location and m.city is not null
                        group by lower(m.city) order by min(m.city)""")
                .query(String.class).list();
    }

    /** Products ordered by number of inquiries in the last 30 days (an honest "trending" without analytics). */
    @Transactional(readOnly = true)
    public List<ProductCard> trending(int limit) {
        List<UUID> ids = jdbc.sql("select p.id from product p join maker_profile m on m.id = p.maker_id "
                        + "join custom_order_inquiry i on i.product_id = p.id and i.created_at > now() - interval '30 days' "
                        + "where " + VISIBLE + " group by p.id order by count(i.id) desc, max(i.created_at) desc limit :limit")
                .param("limit", limit).query(UUID.class).list();
        return hydrate(ids);
    }

    @Transactional(readOnly = true)
    public List<String> publicProductPaths(int limit) {
        return jdbc.sql("select m.slug || '/' || p.slug from product p join maker_profile m on m.id = p.maker_id where "
                        + VISIBLE + " order by p.published_at desc limit :limit")
                .param("limit", limit).query(String.class).list();
    }

    List<ProductCard> hydrate(List<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<UUID, Product> byId = products.findAllWithMakerByIdIn(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<ProductCard> cards = new ArrayList<>(ids.size());
        ids.forEach(id -> {
            Product p = byId.get(id);
            if (p != null) {
                cards.add(ProductMapper.card(p));
            }
        });
        return cards;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
