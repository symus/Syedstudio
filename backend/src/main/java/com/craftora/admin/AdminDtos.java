package com.craftora.admin;

import com.craftora.maker.MakerStatus;
import com.craftora.product.ProductEnums;
import com.craftora.report.Report;
import com.craftora.user.Role;
import com.craftora.user.UserStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class AdminDtos {

    private AdminDtos() {}

    public record StatsResponse(long users, Map<String, Long> makersByStatus, long publishedProducts,
                                long openReports, long inquiriesLast30Days, Map<String, Long> subscriptionsByPlan) {}

    public record AdminUser(UUID id, String email, String displayName, Set<Role> roles, UserStatus status,
                            boolean emailVerified, Instant createdAt) {}

    public record AdminMaker(UUID id, String slug, String businessName, String ownerEmail, MakerStatus status,
                             boolean featured, String planCode, long publishedProducts, Instant createdAt) {}

    public record AdminProduct(UUID id, String slug, String name, String makerSlug, String makerName,
                               ProductEnums.Status status, ProductEnums.Moderation moderation, String moderationReason,
                               String imageUrl, Instant updatedAt) {}

    public record AdminReport(UUID id, Report.TargetType targetType, UUID targetId, String targetLabel,
                              String targetPath, Report.Reason reason, String details, String reporterEmail,
                              Report.Status status, String resolutionNote, Instant createdAt) {}

    public record AuditEntry(UUID id, UUID actorUserId, String actorEmail, String action, String targetType,
                             UUID targetId, String details, Instant createdAt) {}

    public record UserStatusRequest(@NotNull UserStatus status) {}

    public record MakerStatusRequest(@NotNull MakerStatus status, @Size(max = 255) String reason) {}

    public record FeaturedRequest(boolean featured) {}

    public record ModerationRequest(@NotNull ProductEnums.Moderation status, @Size(max = 255) String reason) {}

    public record ResolveReportRequest(@NotNull Report.Status status, @Size(max = 500) String note) {}
}
