package com.craftora.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.craftora.common.ApiException;
import org.junit.jupiter.api.Test;

class ImageUploadsTest {

    @Test
    void detectsImageTypesFromMagicBytes() {
        assertThat(ImageUploads.detectImageType(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0})).isEqualTo("image/jpeg");
        assertThat(ImageUploads.detectImageType(new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}))
                .isEqualTo("image/png");
        assertThat(ImageUploads.detectImageType("RIFF0000WEBPVP8 ".getBytes())).isEqualTo("image/webp");
    }

    @Test
    void rejectsEverythingElse() {
        assertThatThrownBy(() -> ImageUploads.detectImageType("<svg onload=alert(1)>".getBytes()))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> ImageUploads.detectImageType("GIF89a".getBytes())).isInstanceOf(ApiException.class);
    }
}
