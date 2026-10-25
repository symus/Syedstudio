package com.craftora.maker;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MakerProfileRepository extends JpaRepository<MakerProfile, UUID> {

    Optional<MakerProfile> findByUserId(UUID userId);

    Optional<MakerProfile> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<MakerProfile> findTop8ByStatusAndFeaturedTrueOrderByApprovedAtDesc(MakerStatus status);

    List<MakerProfile> findTop8ByStatusOrderByApprovedAtDesc(MakerStatus status);

    Page<MakerProfile> findByStatusOrderByCreatedAtDesc(MakerStatus status, Pageable pageable);

    Page<MakerProfile> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(MakerStatus status);

    @Query("select m.slug from MakerProfile m where m.status = :status")
    List<String> findSlugsByStatus(MakerStatus status);
}
