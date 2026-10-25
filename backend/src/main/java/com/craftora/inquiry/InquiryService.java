package com.craftora.inquiry;

import com.craftora.common.ApiException;
import com.craftora.common.AppProperties;
import com.craftora.common.PageResponse;
import com.craftora.inquiry.InquiryDtos.AttachmentResponse;
import com.craftora.inquiry.InquiryDtos.CustomerInquiry;
import com.craftora.inquiry.InquiryDtos.HistoryEntry;
import com.craftora.inquiry.InquiryDtos.InquiryRequest;
import com.craftora.inquiry.InquiryDtos.InquirySubmitted;
import com.craftora.inquiry.InquiryDtos.MakerInquiryDetail;
import com.craftora.inquiry.InquiryDtos.MakerInquirySummary;
import com.craftora.mail.EmailService;
import com.craftora.mail.MailTemplates;
import com.craftora.maker.MakerProfile;
import com.craftora.maker.MakerProfileRepository;
import com.craftora.maker.MakerService;
import com.craftora.media.ImageUploads;
import com.craftora.media.StorageService;
import com.craftora.product.Product;
import com.craftora.product.ProductRepository;
import com.craftora.user.User;
import com.craftora.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InquiryService {

    private final InquiryRepository inquiries;
    private final MakerProfileRepository makerRepo;
    private final MakerService makers;
    private final ProductRepository products;
    private final UserRepository users;
    private final ImageUploads uploads;
    private final StorageService storage;
    private final EmailService email;
    private final MailTemplates templates;
    private final AppProperties props;

    public InquiryService(InquiryRepository inquiries, MakerProfileRepository makerRepo, MakerService makers,
                          ProductRepository products, UserRepository users, ImageUploads uploads,
                          StorageService storage, EmailService email, MailTemplates templates, AppProperties props) {
        this.inquiries = inquiries;
        this.makerRepo = makerRepo;
        this.makers = makers;
        this.products = products;
        this.users = users;
        this.uploads = uploads;
        this.storage = storage;
        this.email = email;
        this.templates = templates;
        this.props = props;
    }

    @Transactional
    public InquirySubmitted submit(InquiryRequest req, List<MultipartFile> files, UUID customerUserId,
                                   String locale) {
        MakerProfile maker = makerRepo.findBySlug(req.makerSlug()).filter(MakerProfile::isPubliclyVisible)
                .orElseThrow(() -> ApiException.notFound("Maker"));
        Product product = null;
        if (req.productId() != null) {
            product = products.findByIdAndMakerId(req.productId(), maker.getId())
                    .filter(Product::isPubliclyVisible)
                    .orElseThrow(() -> ApiException.badRequest("unknown_product", "Unknown product"));
        }
        List<MultipartFile> attachments = files == null ? List.of()
                : files.stream().filter(f -> f != null && !f.isEmpty()).toList();
        if (attachments.size() > props.uploads().maxInquiryAttachments()) {
            throw ApiException.badRequest("too_many_files",
                    "At most " + props.uploads().maxInquiryAttachments() + " reference images are allowed");
        }
        User customer = customerUserId == null ? null : users.findById(customerUserId).orElse(null);
        var inquiry = new CustomOrderInquiry(maker, product, customer, req.name().trim(),
                req.email().trim().toLowerCase(), blankToNull(req.phone()), req.message().trim(), req.budgetMinor(),
                req.currency() == null ? "PLN" : req.currency(), req.deadline());
        inquiries.save(inquiry);

        List<String> stored = new ArrayList<>();
        try {
            for (MultipartFile f : attachments) {
                var img = uploads.store(f, "private/inquiries/" + inquiry.getId());
                stored.add(img.key());
                inquiry.getAttachments().add(new InquiryAttachment(inquiry, img.key(),
                        sanitizeFilename(f.getOriginalFilename()), img.contentType(), img.sizeBytes()));
            }
        } catch (RuntimeException e) {
            stored.forEach(uploads::deleteQuietly);
            throw e;
        }

        User makerUser = maker.getUser();
        String makerEmail = maker.getContactEmail() != null ? maker.getContactEmail() : makerUser.getEmail();
        email.send(templates.inquiryReceivedByMaker(makerEmail, makerUser.getPreferredLocale(),
                inquiry.getCustomerName(), inquiry.getId().toString()));
        email.send(templates.inquiryConfirmation(inquiry.getCustomerEmail(), props.normalizeLocale(locale),
                inquiry.getCustomerName(), maker.getBusinessName()));
        return new InquirySubmitted(inquiry.getId(), maker.getBusinessName());
    }

    // ------------------------------------------------------------------ maker side (tenant-scoped)

    @Transactional(readOnly = true)
    public PageResponse<MakerInquirySummary> list(UUID userId, InquiryStatus status, Integer page, Integer size) {
        MakerProfile maker = makers.requireOwn(userId);
        var pageable = PageRequest.of(PageResponse.clampPage(page), PageResponse.clampSize(size, 20, 100));
        var result = status == null
                ? inquiries.findByMakerIdOrderByCreatedAtDesc(maker.getId(), pageable)
                : inquiries.findByMakerIdAndStatusOrderByCreatedAtDesc(maker.getId(), status, pageable);
        return PageResponse.of(result, i -> new MakerInquirySummary(i.getId(), i.getCustomerName(),
                i.getProduct() == null ? null : i.getProduct().getName(), i.getStatus(), i.getBudgetMinor(),
                i.getCurrency(), i.getDeadline(), i.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public MakerInquiryDetail get(UUID userId, UUID id) {
        return detail(own(userId, id));
    }

    @Transactional
    public MakerInquiryDetail changeStatus(UUID userId, UUID id, InquiryStatus next, String message) {
        CustomOrderInquiry i = own(userId, id);
        if (i.getStatus() == next) {
            throw ApiException.badRequest("status_unchanged", "Inquiry already has this status");
        }
        String msg = blankToNull(message);
        i.changeStatus(next, msg, users.getReferenceById(userId));
        if (msg != null) {
            notifyCustomer(i, msg);
        }
        return detail(i);
    }

    @Transactional
    public MakerInquiryDetail reply(UUID userId, UUID id, String message) {
        CustomOrderInquiry i = own(userId, id);
        InquiryStatus next = i.getStatus() == InquiryStatus.NEW ? InquiryStatus.IN_PROGRESS : i.getStatus();
        i.changeStatus(next, message.trim(), users.getReferenceById(userId));
        notifyCustomer(i, message.trim());
        return detail(i);
    }

    @Transactional
    public MakerInquiryDetail updateNotes(UUID userId, UUID id, String notes) {
        CustomOrderInquiry i = own(userId, id);
        i.setInternalNotes(blankToNull(notes));
        return detail(i);
    }

    @Transactional(readOnly = true)
    public StorageService.StoredObject attachment(UUID userId, UUID inquiryId, UUID attachmentId) {
        CustomOrderInquiry i = own(userId, inquiryId);
        var a = i.getAttachments().stream().filter(x -> x.getId().equals(attachmentId)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Attachment"));
        return storage.get(a.getStorageKey()).orElseThrow(() -> ApiException.notFound("Attachment"));
    }

    // ------------------------------------------------------------------ customer side

    @Transactional(readOnly = true)
    public List<CustomerInquiry> forCustomer(UUID userId) {
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        String verifiedEmail = user.isEmailVerified() ? user.getEmail() : null;
        return inquiries.findForCustomer(userId, verifiedEmail).stream()
                .map(i -> new CustomerInquiry(i.getId(), i.getMaker().getBusinessName(), i.getMaker().getSlug(),
                        i.getProduct() == null ? null : i.getProduct().getName(), i.getMessage(), i.getBudgetMinor(),
                        i.getCurrency(), i.getDeadline(), i.getStatus(), history(i), i.getCreatedAt()))
                .toList();
    }

    private void notifyCustomer(CustomOrderInquiry i, String message) {
        String locale = i.getCustomer() != null ? i.getCustomer().getPreferredLocale() : props.defaultLocale();
        email.send(templates.inquiryReply(i.getCustomerEmail(), locale, i.getMaker().getBusinessName(),
                i.getStatus().name(), message));
    }

    private CustomOrderInquiry own(UUID userId, UUID id) {
        MakerProfile maker = makers.requireOwn(userId);
        return inquiries.findByIdAndMakerId(id, maker.getId()).orElseThrow(() -> ApiException.notFound("Inquiry"));
    }

    private static MakerInquiryDetail detail(CustomOrderInquiry i) {
        var p = i.getProduct();
        return new MakerInquiryDetail(i.getId(), i.getCustomerName(), i.getCustomerEmail(), i.getCustomerPhone(),
                i.getMessage(), i.getBudgetMinor(), i.getCurrency(), i.getDeadline(), i.getStatus(),
                i.getInternalNotes(), p == null ? null : p.getId(), p == null ? null : p.getName(),
                p == null ? null : p.getSlug(),
                i.getAttachments().stream().map(a -> new AttachmentResponse(a.getId(), a.getOriginalFilename(),
                        a.getContentType(), a.getSizeBytes())).toList(),
                history(i), i.getConsentAt(), i.getCreatedAt());
    }

    private static List<HistoryEntry> history(CustomOrderInquiry i) {
        return i.getHistory().stream()
                .map(h -> new HistoryEntry(h.getFromStatus(), h.getToStatus(), h.getCustomerMessage(), h.getCreatedAt()))
                .toList();
    }

    static String sanitizeFilename(String name) {
        if (name == null || name.isBlank()) {
            return "reference-image";
        }
        String base = name.replaceAll("^.*[/\\\\]", "").replaceAll("[^A-Za-z0-9._ -]", "_");
        return base.length() > 100 ? base.substring(base.length() - 100) : base;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
