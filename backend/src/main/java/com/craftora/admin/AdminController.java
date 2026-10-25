package com.craftora.admin;

import com.craftora.admin.AdminDtos.AdminMaker;
import com.craftora.admin.AdminDtos.AdminProduct;
import com.craftora.admin.AdminDtos.AdminReport;
import com.craftora.admin.AdminDtos.AdminUser;
import com.craftora.admin.AdminDtos.AuditEntry;
import com.craftora.admin.AdminDtos.FeaturedRequest;
import com.craftora.admin.AdminDtos.MakerStatusRequest;
import com.craftora.admin.AdminDtos.ModerationRequest;
import com.craftora.admin.AdminDtos.ResolveReportRequest;
import com.craftora.admin.AdminDtos.StatsResponse;
import com.craftora.admin.AdminDtos.UserStatusRequest;
import com.craftora.common.PageResponse;
import com.craftora.maker.MakerStatus;
import com.craftora.product.ProductEnums;
import com.craftora.report.Report;
import com.craftora.security.AuthUser;
import com.craftora.subscription.PlanDtos.ChangePlanRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Every route here requires ROLE_ADMIN (enforced in SecurityConfig). Mutations are audit-logged. */
@Tag(name = "Admin")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return service.stats();
    }

    @GetMapping("/users")
    public PageResponse<AdminUser> users(@RequestParam(required = false) String q,
                                         @RequestParam(required = false) Integer page) {
        return service.users(q, page);
    }

    @PostMapping("/users/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void userStatus(@AuthenticationPrincipal AuthUser admin, @PathVariable UUID id,
                           @Valid @RequestBody UserStatusRequest req) {
        service.setUserStatus(admin.id(), id, req.status());
    }

    @GetMapping("/makers")
    public PageResponse<AdminMaker> makers(@RequestParam(required = false) MakerStatus status,
                                           @RequestParam(required = false) Integer page) {
        return service.makers(status, page);
    }

    @PostMapping("/makers/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void makerStatus(@AuthenticationPrincipal AuthUser admin, @PathVariable UUID id,
                            @Valid @RequestBody MakerStatusRequest req) {
        service.setMakerStatus(admin.id(), id, req.status(), req.reason());
    }

    @PostMapping("/makers/{id}/featured")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void featured(@AuthenticationPrincipal AuthUser admin, @PathVariable UUID id,
                         @RequestBody FeaturedRequest req) {
        service.setFeatured(admin.id(), id, req.featured());
    }

    @PutMapping("/makers/{id}/plan")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void plan(@AuthenticationPrincipal AuthUser admin, @PathVariable UUID id,
                     @Valid @RequestBody ChangePlanRequest req) {
        service.changePlan(admin.id(), id, req.planCode());
    }

    @GetMapping("/products")
    public PageResponse<AdminProduct> products(@RequestParam(required = false) String q,
                                               @RequestParam(required = false) ProductEnums.Moderation moderation,
                                               @RequestParam(required = false) Integer page) {
        return service.products(q, moderation, page);
    }

    @PostMapping("/products/{id}/moderation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moderate(@AuthenticationPrincipal AuthUser admin, @PathVariable UUID id,
                         @Valid @RequestBody ModerationRequest req) {
        service.moderateProduct(admin.id(), id, req.status(), req.reason());
    }

    @GetMapping("/reports")
    public PageResponse<AdminReport> reports(@RequestParam(required = false) Report.Status status,
                                             @RequestParam(required = false) Integer page) {
        return service.reports(status, page);
    }

    @PostMapping("/reports/{id}/resolve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resolve(@AuthenticationPrincipal AuthUser admin, @PathVariable UUID id,
                        @Valid @RequestBody ResolveReportRequest req) {
        service.resolveReport(admin.id(), id, req.status(), req.note());
    }

    @GetMapping("/audit")
    public PageResponse<AuditEntry> audit(@RequestParam(required = false) Integer page) {
        return service.audit(page);
    }
}
