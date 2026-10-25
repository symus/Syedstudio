package com.craftora.common;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed application settings bound from the {@code app.*} namespace. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String brandName,
        String publicUrl,
        String defaultLocale,
        List<String> supportedLocales,
        Makers makers,
        Session session,
        Mail mail,
        Storage storage,
        Uploads uploads,
        RateLimit rateLimit,
        Dev dev,
        Bootstrap bootstrap) {

    public record Makers(boolean autoApprove) {}

    public record Session(String cookieName, Duration ttl, boolean cookieSecure) {}

    public record Mail(String mode, String from) {}

    public record Storage(String provider, String localRoot, S3 s3) {
        public record S3(String bucket, String region, String endpoint, String accessKey, String secretKey,
                         boolean pathStyle) {}
    }

    public record Uploads(long maxImageBytes, int maxInquiryAttachments) {}

    public record RateLimit(int authPerMinute, int publicFormsPerHour) {}

    public record Dev(boolean outboxEnabled) {}

    public record Bootstrap(String adminEmail, String adminPassword) {}

    public String normalizeLocale(String locale) {
        return locale != null && supportedLocales.contains(locale) ? locale : defaultLocale;
    }
}
