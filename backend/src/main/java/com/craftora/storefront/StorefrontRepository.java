package com.craftora.storefront;

import com.craftora.maker.MakerStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StorefrontRepository extends JpaRepository<Storefront, UUID> {

    Optional<Storefront> findByMakerId(UUID makerId);

    @Query("""
            select s from Storefront s join fetch s.maker m
            where m.slug = :slug and s.status = :status and m.status = :makerStatus
            """)
    Optional<Storefront> findBySlugAndStatus(String slug, Storefront.Status status, MakerStatus makerStatus);

    @Query("select m.slug from Storefront s join s.maker m where s.status = :status and m.status = :makerStatus")
    List<String> findSlugsByStatus(Storefront.Status status, MakerStatus makerStatus);

    default Optional<Storefront> findPublishedBySlug(String slug) {
        return findBySlugAndStatus(slug, Storefront.Status.PUBLISHED, MakerStatus.APPROVED);
    }

    default List<String> findPublishedSlugs() {
        return findSlugsByStatus(Storefront.Status.PUBLISHED, MakerStatus.APPROVED);
    }
}
