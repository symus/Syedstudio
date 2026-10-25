package com.craftora.media;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.util.regex.Pattern;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Serves public images. Private keys (inquiry attachments) are only reachable through authorised endpoints. */
@Tag(name = "Media")
@RestController
public class MediaController {

    private static final Pattern PUBLIC_KEY =
            Pattern.compile("^public/[a-z]+/[0-9a-f-]{36}/[0-9a-f-]{36}\\.(jpg|png|webp)$");

    private final StorageService storage;

    public MediaController(StorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/api/media/{*key}")
    public ResponseEntity<byte[]> get(@PathVariable String key) {
        String k = key.startsWith("/") ? key.substring(1) : key;
        if (!PUBLIC_KEY.matcher(k).matches()) {
            return ResponseEntity.notFound().build();
        }
        return storage.get(k)
                .map(o -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(o.contentType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                        .header("X-Content-Type-Options", "nosniff")
                        .body(o.data()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Converts a storage key into the public URL path used by clients. */
    public static String url(String key) {
        return key == null ? null : "/api/media/" + key;
    }
}
