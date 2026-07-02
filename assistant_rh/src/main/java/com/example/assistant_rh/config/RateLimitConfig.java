package com.example.assistant_rh.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation
    .Configuration;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> buckets =
        new ConcurrentHashMap<>();

    // Bucket par IP ou par email
    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(
            key, this::newBucket);
    }

    private Bucket newBucket(String key) {
        // 60 requêtes par minute
        Bandwidth limit = Bandwidth.classic(
            60,
            Refill.greedy(60,
                Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    // Bucket strict for auth
    // (5 tentatives par minute)
    public Bucket resolveAuthBucket(String ip) {
        return buckets.computeIfAbsent(
            "auth_" + ip, k -> {
                Bandwidth limit =
                    Bandwidth.classic(5,
                        Refill.greedy(5,
                            Duration.ofMinutes(1)));
                return Bucket.builder()
                    .addLimit(limit)
                    .build();
            });
    }
}
