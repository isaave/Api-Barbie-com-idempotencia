package com.barbie.dreamworld_api.ratelimit;

import com.barbie.dreamworld_api.exceptions.RateLimitException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Intercepta todas as requisições /api/** e aplica o rate limiting por IP.
 *
 * Headers adicionados em toda resposta bem-sucedida:
 *   X-Rate-Limit-Remaining  — tokens restantes no bucket atual
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Set<String> WRITE_METHODS = Set.of(
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.DELETE.name()
    );

    private final RateLimitConfig rateLimitConfig;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String ip = resolveClientIp(request);
        boolean isWrite = WRITE_METHODS.contains(request.getMethod().toUpperCase());

        Bucket bucket = rateLimitConfig.resolveBucket(ip, isWrite);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000L;
        log.warn("Rate limit excedido — ip={} method={} retryAfter={}s", ip, request.getMethod(), retryAfterSeconds);
        throw new RateLimitException(retryAfterSeconds);
    }

    /**
     * Resolve o IP real do cliente, respeitando proxies com X-Forwarded-For.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
