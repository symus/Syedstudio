package com.craftora.product;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    /** Tenant-scoped lookup: returns empty when the product belongs to another maker. */
    Optional<Product> findByIdAndMakerId(UUID id, UUID makerId);

    Page<Product> findByMakerIdOrderByUpdatedAtDesc(UUID makerId, Pageable pageable);

    Page<Product> findByMakerIdAndStatusOrderByUpdatedAtDesc(UUID makerId, ProductEnums.Status status, Pageable pageable);

    long countByMakerId(UUID makerId);

    long countByMakerIdAndStatus(UUID makerId, ProductEnums.Status status);

    boolean existsByMakerIdAndSlug(UUID makerId, String slug);

    long countByStatus(ProductEnums.Status status);

    @Query("select p from Product p join fetch p.maker m where p.id in :ids")
    List<Product> findAllWithMakerByIdIn(Collection<UUID> ids);

    @Query("select p from Product p join fetch p.maker m where m.slug = :makerSlug and p.slug = :slug")
    Optional<Product> findByMakerSlugAndSlug(String makerSlug, String slug);

    @Query("""
            select p from Product p join fetch p.maker m
            where (:q is null or lower(p.name) like lower(concat('%', cast(:q as string), '%'))
                   or lower(m.businessName) like lower(concat('%', cast(:q as string), '%')))
              and (:moderation is null or p.moderationStatus = :moderation)
            order by p.updatedAt desc
            """)
    Page<Product> adminSearch(String q, ProductEnums.Moderation moderation, Pageable pageable);

    @Query("select p.maker.id, count(p) from Product p where p.maker.id in :makerIds and p.status = :status "
            + "and p.moderationStatus = :moderation group by p.maker.id")
    List<Object[]> countByMakers(Collection<UUID> makerIds, ProductEnums.Status status,
                                 ProductEnums.Moderation moderation);

    default java.util.Map<UUID, Long> countVisibleByMakers(Collection<UUID> makerIds) {
        java.util.Map<UUID, Long> result = new java.util.HashMap<>();
        if (!makerIds.isEmpty()) {
            countByMakers(makerIds, ProductEnums.Status.PUBLISHED, ProductEnums.Moderation.VISIBLE)
                    .forEach(r -> result.put((UUID) r[0], (Long) r[1]));
        }
        return result;
    }

    @Query("select i.storageKey from ProductImage i where i.product.maker.id = :makerId")
    List<String> findImageKeysByMaker(UUID makerId);
}
