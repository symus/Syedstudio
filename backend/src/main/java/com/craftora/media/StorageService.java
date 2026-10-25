package com.craftora.media;

import java.util.Optional;

/** Object storage abstraction. Keys under {@code public/} may be served anonymously; {@code private/} never are. */
public interface StorageService {

    void put(String key, byte[] data, String contentType);

    Optional<StoredObject> get(String key);

    void delete(String key);

    record StoredObject(byte[] data, String contentType) {}
}
