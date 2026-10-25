package com.craftora.inquiry;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class InquiryDtos {

    private InquiryDtos() {}

    public record InquiryRequest(
            @NotBlank @Size(max = 60) String makerSlug,
            UUID productId,
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @Size(max = 40) String phone,
            @NotBlank @Size(min = 10, max = 5000) String message,
            @Min(0) @Max(100_000_000) Long budgetMinor,
            @Pattern(regexp = "^(PLN|EUR)$") String currency,
            @Future LocalDate deadline,
            @NotNull @AssertTrue(message = "Consent is required to send your inquiry") Boolean consent) {}

    public record InquirySubmitted(UUID id, String makerName) {}

    public record StatusChangeRequest(@NotNull InquiryStatus status, @Size(max = 5000) String message) {}

    public record NotesRequest(@Size(max = 5000) String notes) {}

    public record ReplyRequest(@NotBlank @Size(max = 5000) String message) {}

    public record AttachmentResponse(UUID id, String filename, String contentType, long sizeBytes) {}

    public record HistoryEntry(InquiryStatus from, InquiryStatus to, String message, Instant at) {}

    public record MakerInquirySummary(UUID id, String customerName, String productName, InquiryStatus status,
                                      Long budgetMinor, String currency, LocalDate deadline, Instant createdAt) {}

    public record MakerInquiryDetail(UUID id, String customerName, String customerEmail, String customerPhone,
                                     String message, Long budgetMinor, String currency, LocalDate deadline,
                                     InquiryStatus status, String internalNotes, UUID productId,
                                     String productName, String productSlug, List<AttachmentResponse> attachments,
                                     List<HistoryEntry> history, Instant consentAt, Instant createdAt) {}

    /** Customer view: deliberately omits internal notes and other customers' data. */
    public record CustomerInquiry(UUID id, String makerName, String makerSlug, String productName, String message,
                                  Long budgetMinor, String currency, LocalDate deadline, InquiryStatus status,
                                  List<HistoryEntry> history, Instant createdAt) {}
}
