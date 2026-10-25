package com.craftora.media;

import com.craftora.common.ApiException;
import com.craftora.common.AppProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Validates uploaded images by size and by their actual file signature (magic bytes), never trusting the
 * client-supplied content type or extension, then stores them under a random key.
 */
@Component
public class ImageUploads {

    public record StoredImage(String key, String contentType, long sizeBytes) {}

    private final StorageService storage;
    private final long maxBytes;

    public ImageUploads(StorageService storage, AppProperties props) {
        this.storage = storage;
        this.maxBytes = props.uploads().maxImageBytes();
    }

    public StoredImage store(MultipartFile file, String prefix) {
        byte[] data = read(file);
        String type = detectImageType(data);
        String key = prefix + "/" + UUID.randomUUID() + "." + extension(type);
        storage.put(key, data, type);
        return new StoredImage(key, type, data.length);
    }

    public void deleteQuietly(String key) {
        if (key != null) {
            try {
                storage.delete(key);
            } catch (RuntimeException ignored) {
                // orphaned objects are harmless; a periodic bucket sweep can remove them
            }
        }
    }

    private byte[] read(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("file_missing", "No file uploaded");
        }
        if (file.getSize() > maxBytes) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "file_too_large",
                    "Images must be at most " + (maxBytes / 1024 / 1024) + " MB");
        }
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String detectImageType(byte[] d) {
        if (d.length >= 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (d.length >= 8 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G'
                && d[4] == 0x0D && d[5] == 0x0A && d[6] == 0x1A && d[7] == 0x0A) {
            return "image/png";
        }
        if (d.length >= 12 && d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P') {
            return "image/webp";
        }
        throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported_file_type",
                "Only JPEG, PNG and WebP images are allowed");
    }

    private static String extension(String type) {
        return switch (type) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
