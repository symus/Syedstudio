package com.craftora.media;

import com.craftora.common.AppProperties;
import java.net.URI;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/** S3-compatible storage (AWS S3, Cloudflare R2, MinIO, ...). The bucket must not allow public listing. */
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
public class S3StorageService implements StorageService {

    private final S3Client s3;
    private final String bucket;

    public S3StorageService(AppProperties props) {
        var cfg = props.storage().s3();
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(cfg.region()))
                .forcePathStyle(cfg.pathStyle());
        if (cfg.endpoint() != null && !cfg.endpoint().isBlank()) {
            builder.endpointOverride(URI.create(cfg.endpoint()));
        }
        if (cfg.accessKey() != null && !cfg.accessKey().isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(cfg.accessKey(), cfg.secretKey())));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
        }
        this.s3 = builder.build();
        this.bucket = cfg.bucket();
    }

    @Override
    public void put(String key, byte[] data, String contentType) {
        s3.putObject(b -> b.bucket(bucket).key(key).contentType(contentType), RequestBody.fromBytes(data));
    }

    @Override
    public Optional<StoredObject> get(String key) {
        try {
            var response = s3.getObjectAsBytes(b -> b.bucket(bucket).key(key));
            return Optional.of(new StoredObject(response.asByteArray(), response.response().contentType()));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        s3.deleteObject(b -> b.bucket(bucket).key(key));
    }
}
