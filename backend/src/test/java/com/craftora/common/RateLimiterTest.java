package com.craftora.common;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

    @Test
    void blocksAfterLimitWithinWindow() {
        RateLimiter limiter = new RateLimiter();
        for (int i = 0; i < 3; i++) {
            limiter.check("k", 3, Duration.ofMinutes(1));
        }
        assertThatThrownBy(() -> limiter.check("k", 3, Duration.ofMinutes(1)))
                .isInstanceOf(ApiException.class).hasMessageContaining("Too many requests");
        limiter.check("other-key", 3, Duration.ofMinutes(1));
    }
}
