package com.craftora.report;

import com.craftora.common.ApiException;
import com.craftora.common.AppProperties;
import com.craftora.common.ClientIp;
import com.craftora.common.RateLimiter;
import com.craftora.maker.MakerProfileRepository;
import com.craftora.product.ProductRepository;
import com.craftora.security.AuthUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reports")
@RestController
public class ReportController {

    public record ReportRequest(@NotNull Report.TargetType targetType, @NotNull UUID targetId,
                                @NotNull Report.Reason reason, @Size(max = 2000) String details,
                                @Email @Size(max = 254) String email) {}

    private final ReportRepository reports;
    private final ProductRepository products;
    private final MakerProfileRepository makers;
    private final RateLimiter rateLimiter;
    private final AppProperties props;

    public ReportController(ReportRepository reports, ProductRepository products, MakerProfileRepository makers,
                            RateLimiter rateLimiter, AppProperties props) {
        this.reports = reports;
        this.products = products;
        this.makers = makers;
        this.rateLimiter = rateLimiter;
        this.props = props;
    }

    @PostMapping("/api/public/reports")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public void report(@Valid @RequestBody ReportRequest req, @AuthenticationPrincipal AuthUser user,
                       HttpServletRequest http) {
        rateLimiter.check("report:" + ClientIp.of(http), props.rateLimit().publicFormsPerHour(), Duration.ofHours(1));
        boolean exists = switch (req.targetType()) {
            case PRODUCT -> products.existsById(req.targetId());
            case MAKER -> makers.existsById(req.targetId());
        };
        if (!exists) {
            throw ApiException.notFound("Report target");
        }
        reports.save(new Report(req.targetType(), req.targetId(), req.reason(),
                req.details() == null ? null : req.details().trim(), user == null ? null : user.id(),
                user == null ? req.email() : user.email()));
    }
}
