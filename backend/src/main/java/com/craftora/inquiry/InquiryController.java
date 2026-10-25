package com.craftora.inquiry;

import com.craftora.common.AppProperties;
import com.craftora.common.ClientIp;
import com.craftora.common.PageResponse;
import com.craftora.common.RateLimiter;
import com.craftora.inquiry.InquiryDtos.CustomerInquiry;
import com.craftora.inquiry.InquiryDtos.InquiryRequest;
import com.craftora.inquiry.InquiryDtos.InquirySubmitted;
import com.craftora.inquiry.InquiryDtos.MakerInquiryDetail;
import com.craftora.inquiry.InquiryDtos.MakerInquirySummary;
import com.craftora.inquiry.InquiryDtos.NotesRequest;
import com.craftora.inquiry.InquiryDtos.ReplyRequest;
import com.craftora.inquiry.InquiryDtos.StatusChangeRequest;
import com.craftora.security.AuthUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Inquiries")
@RestController
public class InquiryController {

    private final InquiryService service;
    private final RateLimiter rateLimiter;
    private final AppProperties props;

    public InquiryController(InquiryService service, RateLimiter rateLimiter, AppProperties props) {
        this.service = service;
        this.rateLimiter = rateLimiter;
        this.props = props;
    }

    /** Multipart: part {@code data} (JSON {@link InquiryRequest}) plus up to N image parts named {@code files}. */
    @PostMapping(path = "/api/public/inquiries", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public InquirySubmitted submit(@Valid @RequestPart("data") InquiryRequest data,
                                   @RequestPart(name = "files", required = false) List<MultipartFile> files,
                                   @RequestParam(defaultValue = "pl") String locale,
                                   @AuthenticationPrincipal AuthUser user, HttpServletRequest http) {
        rateLimiter.check("inquiry:" + ClientIp.of(http), props.rateLimit().publicFormsPerHour(), Duration.ofHours(1));
        return service.submit(data, files, user == null ? null : user.id(), locale);
    }

    @GetMapping("/api/maker/inquiries")
    public PageResponse<MakerInquirySummary> list(@AuthenticationPrincipal AuthUser user,
                                                  @RequestParam(required = false) InquiryStatus status,
                                                  @RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer size) {
        return service.list(user.id(), status, page, size);
    }

    @GetMapping("/api/maker/inquiries/{id}")
    public MakerInquiryDetail get(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id) {
        return service.get(user.id(), id);
    }

    @PostMapping("/api/maker/inquiries/{id}/status")
    public MakerInquiryDetail status(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id,
                                     @Valid @RequestBody StatusChangeRequest req) {
        return service.changeStatus(user.id(), id, req.status(), req.message());
    }

    @PostMapping("/api/maker/inquiries/{id}/reply")
    public MakerInquiryDetail reply(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id,
                                    @Valid @RequestBody ReplyRequest req) {
        return service.reply(user.id(), id, req.message());
    }

    @PutMapping("/api/maker/inquiries/{id}/notes")
    public MakerInquiryDetail notes(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id,
                                    @Valid @RequestBody NotesRequest req) {
        return service.updateNotes(user.id(), id, req.notes());
    }

    @GetMapping("/api/maker/inquiries/{id}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> attachment(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id,
                                             @PathVariable UUID attachmentId) {
        var file = service.attachment(user.id(), id, attachmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.data());
    }

    @GetMapping("/api/account/inquiries")
    public List<CustomerInquiry> mine(@AuthenticationPrincipal AuthUser user) {
        return service.forCustomer(user.id());
    }
}
