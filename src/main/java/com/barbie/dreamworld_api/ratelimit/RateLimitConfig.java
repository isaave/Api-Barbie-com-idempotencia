package com.barbie.dreamworld_api.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia buckets por IP usando armazenamento em memória (ConcurrentHashMap).
 *
 * Limites configurados:
 *   - GET  → 60 requisições / minuto  por IP
 *   - POST/PUT/DELETE → 20 requisições / minuto por IP
 *
 * Para produção, substitua o ConcurrentHashMap por uma implementação
 * distribuída (Redis via bucket4j-redis, Hazelcast etc.).
 */
@Configuration
public class RateLimitConfig {

    // Capacidade e recarga para leituras
    private static final int  READ_CAPACITY     = 2;
    private static final long READ_REFILL_SECS  = 2;

    // Capacidade e recarga para escritas
    private static final int  WRITE_CAPACITY    = 2;
    private static final long WRITE_REFILL_SECS = 2;

    // Um bucket por IP + tipo (READ | WRITE)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ip, boolean isWriteOperation) {
        String key = ip + ":" + (isWriteOperation ? "W" : "R");
        return buckets.computeIfAbsent(key, k -> buildBucket(isWriteOperation));
    }

    private Bucket buildBucket(boolean write) {
        Bandwidth limit = write
                ? Bandwidth.builder()
                        .capacity(WRITE_CAPACITY)
                        .refillGreedy(WRITE_CAPACITY, Duration.ofSeconds(WRITE_REFILL_SECS))
                        .build()
                : Bandwidth.builder()
                        .capacity(READ_CAPACITY)
                        .refillGreedy(READ_CAPACITY, Duration.ofSeconds(READ_REFILL_SECS))
                        .build();

        return Bucket.builder().addLimit(limit).build();
    }
}
