package com.craftora.admin;

import com.craftora.admin.AdminDtos.AdminMaker;
import com.craftora.admin.AdminDtos.AdminProduct;
import com.craftora.admin.AdminDtos.AdminReport;
import com.craftora.admin.AdminDtos.AdminUser;
import com.craftora.admin.AdminDtos.AuditEntry;
import com.craftora.admin.AdminDtos.StatsResponse;
import com.craftora.auth.SessionService;
import com.craftora.common.ApiException;
import com.craftora.common.PageResponse;
import com.craftora.maker.MakerProfile;
import com.craftora.maker.MakerProfileRepository;
import com.craftora.maker.MakerStatus;
import com.craftora.media.MediaController;
import com.craftora.product.Product;
import com.craftora.product.ProductEnums;
import com.craftora.product.ProductRepository;
import com.craftora.report.Report;
import com.craftora.report.ReportRepository;
import com.craftora.subscription.PlanService;
import com.craftora.subscription.SubscriptionRepository;
import com.craftora.user.User;
import com.craftora.user.UserRepository;
import com.craftora.user.UserStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository users;
    private final MakerProfileRepository makers;
    private final ProductRepository products;
    private final ReportRepository reports;
    private final SubscriptionRepository subscriptions;
    private final AuditLogRepository auditLogs;
    private final AuditService audit;
    private final PlanService plans;
    private final SessionService sessions;
    private final JdbcClient jdbc;

    public AdminService(UserRepository users, MakerProfileRepository makers, ProductRepository products,
                        ReportRepository reports, SubscriptionRepository subscriptions, AuditLogRepository auditLogs,
                        AuditService audit, PlanService plans, SessionService sessions, JdbcClient jdbc) {
        this.users = users;
        this.makers = makers;
        this.products = products;
        this.reports = reports;
        this.subscriptions = subscriptions;
        this.auditLogs = auditLogs;
        this.audit = audit;
        this.plans = plans;
        this.sessions = sessions;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        Map<String, Long> makersByStatus = new LinkedHashMap<>();
        for (MakerStatus s : MakerStatus.values()) {
            makersByStatus.put(s.name(), makers.countByStatus(s));
        }
        Map<String, Long> byPlan = new LinkedHashMap<>();
        subscriptions.countByPlan().forEach(r -> byPlan.put((String) r[0], (Long) r[1]));
        long inquiries = jdbc.sql("select count(*) from custom_order_inquiry where created_at > now() - interval '30 days'")
                .query(Long.class).single();
        return new StatsResponse(users.count(), makersByStatus, products.countByStatus(ProductEnums.Status.PUBLISHED),
                reports.countByStatus(Report.Status.OPEN), inquiries, byPlan);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUser> users(String q, Integer page) {
        return PageResponse.of(users.search(blankToNull(q), pageable(page)),
                u -> new AdminUser(u.getId(), u.getEmail(), u.getDisplayName(), u.getRoles(), u.getStatus(),
                        u.isEmailVerified(), u.getCreatedAt()));
    }

    @Transactional
    public void setUserStatus(UUID adminId, UUID userId, UserStatus status) {
        if (adminId.equals(userId)) {
            throw ApiException.badRequest("self_action", "You cannot change your own account status");
        }
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        u.setStatus(status);
        if (status == UserStatus.SUSPENDED) {
            sessions.revokeAll(userId);
        }
        audit.record(adminId, "USER_" + status.name(), "USER", userId, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminMaker> makers(MakerStatus status, Integer page) {
        var result = status == null ? makers.findAllByOrderByCreatedAtDesc(pageable(page))
                : makers.findByStatusOrderByCreatedAtDesc(status, pageable(page));
        List<UUID> ids = result.getContent().stream().map(MakerProfile::getId).toList();
        Map<UUID, Long> counts = products.countVisibleByMakers(ids);
        return PageResponse.of(result, m -> new AdminMaker(m.getId(), m.getSlug(), m.getBusinessName(),
                m.getUser().getEmail(), m.getStatus(), m.isFeatured(),
                subscriptions.findByMakerId(m.getId()).map(s -> s.getPlan().getCode()).orElse(null),
                counts.getOrDefault(m.getId(), 0L), m.getCreatedAt()));
    }

    @Transactional
    public void setMakerStatus(UUID adminId, UUID makerId, MakerStatus status, String reason) {
        MakerProfile m = makers.findById(makerId).orElseThrow(() -> ApiException.notFound("Maker"));
        if (status == MakerStatus.APPROVED) {
            m.approve();
        } else {
            m.setStatus(status);
        }
        audit.record(adminId, "MAKER_" + status.name(), "MAKER", makerId, reason);
    }

    @Transactional
    public void setFeatured(UUID adminId, UUID makerId, boolean featured) {
        MakerProfile m = makers.findById(makerId).orElseThrow(() -> ApiException.notFound("Maker"));
        m.setFeatured(featured);
        audit.record(adminId, featured ? "MAKER_FEATURED" : "MAKER_UNFEATURED", "MAKER", makerId, null);
    }

    @Transactional
    public void changePlan(UUID adminId, UUID makerId, String planCode) {
        plans.changePlan(makerId, planCode);
        audit.record(adminId, "MAKER_PLAN_CHANGED", "MAKER", makerId, planCode);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminProduct> products(String q, ProductEnums.Moderation moderation, Integer page) {
        return PageResponse.of(products.adminSearch(blankToNull(q), moderation, pageable(page)), p -> {
            var img = p.firstImage();
            return new AdminProduct(p.getId(), p.getSlug(), p.getName(), p.getMaker().getSlug(),
                    p.getMaker().getBusinessName(), p.getStatus(), p.getModerationStatus(), p.getModerationReason(),
                    img == null ? null : MediaController.url(img.getStorageKey()), p.getUpdatedAt());
        });
    }

    @Transactional
    public void moderateProduct(UUID adminId, UUID productId, ProductEnums.Moderation status, String reason) {
        Product p = products.findById(productId).orElseThrow(() -> ApiException.notFound("Product"));
        p.setModerationStatus(status);
        p.setModerationReason(status == ProductEnums.Moderation.HIDDEN ? blankToNull(reason) : null);
        audit.record(adminId, "PRODUCT_" + status.name(), "PRODUCT", productId, reason);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminReport> reports(Report.Status status, Integer page) {
        var result = reports.findByStatusOrderByCreatedAtDesc(status == null ? Report.Status.OPEN : status,
                pageable(page));
        List<UUID> productIds = result.getContent().stream()
                .filter(r -> r.getTargetType() == Report.TargetType.PRODUCT).map(Report::getTargetId).toList();
        List<UUID> makerIds = result.getContent().stream()
                .filter(r -> r.getTargetType() == Report.TargetType.MAKER).map(Report::getTargetId).toList();
        Map<UUID, Product> productMap = products.findAllWithMakerByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<UUID, MakerProfile> makerMap = makers.findAllById(makerIds).stream()
                .collect(Collectors.toMap(MakerProfile::getId, Function.identity()));
        return PageResponse.of(result, r -> {
            String label = null;
            String path = null;
            if (r.getTargetType() == Report.TargetType.PRODUCT && productMap.containsKey(r.getTargetId())) {
                Product p = productMap.get(r.getTargetId());
                label = p.getName() + " (" + p.getMaker().getBusinessName() + ")";
                path = "/p/" + p.getMaker().getSlug() + "/" + p.getSlug();
            } else if (makerMap.containsKey(r.getTargetId())) {
                MakerProfile m = makerMap.get(r.getTargetId());
                label = m.getBusinessName();
                path = "/makers/" + m.getSlug();
            }
            return new AdminReport(r.getId(), r.getTargetType(), r.getTargetId(), label, path, r.getReason(),
                    r.getDetails(), r.getReporterEmail(), r.getStatus(), r.getResolutionNote(), r.getCreatedAt());
        });
    }

    @Transactional
    public void resolveReport(UUID adminId, UUID reportId, Report.Status outcome, String note) {
        if (outcome == Report.Status.OPEN) {
            throw ApiException.badRequest("invalid_status", "Choose RESOLVED or DISMISSED");
        }
        Report r = reports.findById(reportId).orElseThrow(() -> ApiException.notFound("Report"));
        r.resolve(outcome, blankToNull(note), adminId);
        audit.record(adminId, "REPORT_" + outcome.name(), "REPORT", reportId, note);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditEntry> audit(Integer page) {
        var result = auditLogs.findAllByOrderByCreatedAtDesc(pageable(page));
        Map<UUID, String> emails = users.findAllById(result.getContent().stream().map(AuditLog::getActorUserId)
                        .filter(java.util.Objects::nonNull).distinct().toList()).stream()
                .collect(Collectors.toMap(User::getId, User::getEmail));
        return PageResponse.of(result, a -> new AuditEntry(a.getId(), a.getActorUserId(),
                emails.get(a.getActorUserId()), a.getAction(), a.getTargetType(), a.getTargetId(), a.getDetails(),
                a.getCreatedAt()));
    }

    private static Pageable pageable(Integer page) {
        return PageRequest.of(PageResponse.clampPage(page), 25);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
