package com.premisave.booking.util;

import com.premisave.booking.config.RateLimiterConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP rate limiter using Bucket4j in-memory buckets.
 *
 * FIX: Previously used a single global Bucket — one busy user could exhaust
 *      the limit for everyone. Now each IP address gets its own bucket.
 *
 * Note: For multi-instance deployments, replace ConcurrentHashMap with a
 *       Redis-backed Bucket4j provider (bucket4j-redis) so limits are
 *       enforced across all pods.
 */
@Component
public class RateLimiterInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;

    public RateLimiterInterceptor(RateLimiterConfig config) {
        this.requestsPerMinute = config.requestsPerMinute;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String ip = resolveClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, this::newBucket);

        if (bucket.tryConsume(1)) {
            return true;
        }

        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"success\":false,\"message\":\"Too many requests. Please try again in a minute.\"}");
        return false;
    }

    private Bucket newBucket(String ip) {
        Refill refill = Refill.intervally(requestsPerMinute, Duration.ofMinutes(1));
        Bandwidth bandwidth = Bandwidth.classic(requestsPerMinute, refill);
        return Bucket.builder().addLimit(bandwidth).build();
    }

    /**
     * Resolve the real client IP, accounting for reverse proxies / load balancers.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}