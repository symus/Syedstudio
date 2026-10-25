package com.craftora.common;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * In-memory sliding-window limiter for abuse-prone endpoints (login, registration, public forms).
 * Single-instance only: when running several backend replicas, back this with Redis instead.
 */
@Component
public class RateLimiter {

    private final Map<String, Deque<Long>> windows = new ConcurrentHashMap<>();

    /** Records a hit for {@code key}; throws 429 when more than {@code limit} hits occurred within {@code window}. */
    public void check(String key, int limit, Duration window) {
        long now = System.currentTimeMillis();
        long cutoff = now - window.toMillis();
        Deque<Long> hits = windows.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (hits) {
            while (!hits.isEmpty() && hits.peekFirst() < cutoff) {
                hits.pollFirst();
            }
            if (hits.size() >= limit) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "rate_limited",
                        "Too many requests. Please try again later.");
            }
            hits.addLast(now);
        }
    }

    public void reset() {
        windows.clear();
    }

    @Scheduled(fixedDelay = 600_000)
    void evictIdle() {
        long cutoff = System.currentTimeMillis() - Duration.ofHours(2).toMillis();
        windows.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                Long last = e.getValue().peekLast();
                return last == null || last < cutoff;
            }
        });
    }
}
