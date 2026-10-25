package com.craftora.media;

import com.craftora.common.AppProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Filesystem storage for local development and tests. */
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path root;

    public LocalStorageService(AppProperties props) {
        this.root = Path.of(props.storage().localRoot()).toAbsolutePath().normalize();
    }

    @Override
    public void put(String key, byte[] data, String contentType) {
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, data);
            Files.writeString(target.resolveSibling(target.getFileName() + ".type"), contentType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<StoredObject> get(String key) {
        Path target = resolve(key);
        if (!Files.isRegularFile(target)) {
            return Optional.empty();
        }
        try {
            String type = Files.readString(target.resolveSibling(target.getFileName() + ".type"));
            return Optional.of(new StoredObject(Files.readAllBytes(target), type));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void delete(String key) {
        Path target = resolve(key);
        try {
            Files.deleteIfExists(target);
            Files.deleteIfExists(target.resolveSibling(target.getFileName() + ".type"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path resolve(String key) {
        Path p = root.resolve(key).normalize();
        if (!p.startsWith(root)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return p;
    }
}
