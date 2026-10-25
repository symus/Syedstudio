package com.craftora.inquiry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InquiryRepository extends JpaRepository<CustomOrderInquiry, UUID> {

    /** Tenant-scoped lookup. */
    Optional<CustomOrderInquiry> findByIdAndMakerId(UUID id, UUID makerId);

    Page<CustomOrderInquiry> findByMakerIdOrderByCreatedAtDesc(UUID makerId, Pageable pageable);

    Page<CustomOrderInquiry> findByMakerIdAndStatusOrderByCreatedAtDesc(UUID makerId, InquiryStatus status,
                                                                        Pageable pageable);

    long countByMakerIdAndStatus(UUID makerId, InquiryStatus status);

    long countByMakerIdAndStatusIn(UUID makerId, List<InquiryStatus> statuses);

    List<CustomOrderInquiry> findTop5ByMakerIdOrderByCreatedAtDesc(UUID makerId);

    /** A customer's inquiries: linked to their account, or sent from their (verified) e-mail address. */
    @Query("""
            select i from CustomOrderInquiry i join fetch i.maker
            where i.customer.id = :userId or (:verifiedEmail is not null and i.customerEmail = :verifiedEmail)
            order by i.createdAt desc
            """)
    List<CustomOrderInquiry> findForCustomer(UUID userId, String verifiedEmail);

    @Query("select a.storageKey from InquiryAttachment a where a.inquiry.maker.id = :makerId")
    List<String> findAttachmentKeysByMaker(UUID makerId);
}
