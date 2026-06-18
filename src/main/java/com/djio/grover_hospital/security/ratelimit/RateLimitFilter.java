package com.djio.grover_hospital.security.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Per-IP token-bucket rate limiting for auth endpoints. Runs BEFORE Spring Security so
 * we never burn BCrypt-12 cycles (~250ms each) on brute-force attempts.
 *
 * <p>Buckets are kept in-memory per Cloud Run instance. With min-instances=1 and low
 * traffic that's plenty. If we ever scale beyond a single instance under sustained load,
 * a determined attacker could spread retries across instances — at that point promote
 * to Redis or Cloud Armor.</p>
 *
 * <p>Cleanup: idle bucket entries pruned hourly to cap memory at ~hundreds of KB.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;

    private final Map<String, BucketHolder> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, BucketHolder> registerBuckets = new ConcurrentHashMap<>();
    private final Map<String, BucketHolder> passwordResetBuckets = new ConcurrentHashMap<>();
    private final Map<String, BucketHolder> downloadRequestBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (!properties.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        Bucket bucket = resolveBucket(request);
        if (bucket == null) {
            // Not a rate-limited path — let it through unchanged.
            chain.doFilter(request, response);
            return;
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
            return;
        }

        long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
        if (waitSeconds < 1) waitSeconds = 1;

        log.warn("Rate limit hit: ip={} path={} retry-after={}s",
                getClientIp(request), request.getRequestURI(), waitSeconds);

        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(waitSeconds));
        response.setHeader("X-RateLimit-Retry-After", String.valueOf(waitSeconds));
        response.setContentType("application/json");
        response.getWriter().write("""
                {"success":false,"message":"Too many requests. Try again in %d seconds.","data":null}
                """.formatted(waitSeconds));
    }

    /**
     * Maps the request to a per-IP bucket if the path is rate-limited, else null.
     * Path matching uses endsWith to ignore the /api/v1 context-path prefix.
     */
    private Bucket resolveBucket(HttpServletRequest request) {
        String path = request.getRequestURI();
        String ip = getClientIp(request);

        if (path.endsWith("/auth/login") || path.endsWith("/auth/admin/login")) {
            return obtain(loginBuckets, ip, properties.getLoginRequestsPerMinute(), Duration.ofMinutes(1));
        }
        if (path.endsWith("/auth/register")) {
            return obtain(registerBuckets, ip, properties.getRegisterRequestsPer10Min(), Duration.ofMinutes(10));
        }
        if (path.endsWith("/auth/refresh")) {
            return obtain(loginBuckets, ip, properties.getLoginRequestsPerMinute(), Duration.ofMinutes(1));
        }
        if (path.endsWith("/auth/forgot-password") || path.endsWith("/auth/reset-password")) {
            return obtain(passwordResetBuckets, ip,
                    properties.getPasswordResetRequestsPer5Min(), Duration.ofMinutes(5));
        }
        if (path.contains("/email-link")) {
            return obtain(downloadRequestBuckets, ip,
                    properties.getDownloadRequestRequestsPer5Min(), Duration.ofMinutes(5));
        }

        return null;
    }

    private Bucket obtain(Map<String, BucketHolder> cache, String ip, int capacity, Duration period) {
        BucketHolder holder = cache.computeIfAbsent(ip, k -> new BucketHolder(newBucket(capacity, period)));
        holder.touch();
        return holder.bucket;
    }

    private Bucket newBucket(int capacity, Duration refillPeriod) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(capacity).refillIntervally(capacity, refillPeriod))
                .build();
    }

    /**
     * Cloud Run sets X-Forwarded-For to the real client IP (Google's edge proxy adds it,
     * untrusted callers can't reach the container directly). Use it when present, else fall
     * back to the socket address.
     */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return request.getRemoteAddr();
    }

    /** Runs hourly. Drops bucket entries unused for >2 hours to keep memory bounded. */
    @Scheduled(fixedDelay = 3_600_000L, initialDelay = 3_600_000L)
    public void pruneIdleBuckets() {
        long cutoff = System.nanoTime() - Duration.ofHours(2).toNanos();
        int removed = 0;
        removed += removeStale(loginBuckets, cutoff);
        removed += removeStale(registerBuckets, cutoff);
        removed += removeStale(passwordResetBuckets, cutoff);
        removed += removeStale(downloadRequestBuckets, cutoff);
        if (removed > 0) {
            log.info("Rate-limit prune: removed {} idle bucket entries", removed);
        }
    }

    private int removeStale(Map<String, BucketHolder> cache, long cutoffNanos) {
        int before = cache.size();
        cache.entrySet().removeIf(e -> e.getValue().lastAccessNanos < cutoffNanos);
        return before - cache.size();
    }

    private static final class BucketHolder {
        final Bucket bucket;
        volatile long lastAccessNanos;

        BucketHolder(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccessNanos = System.nanoTime();
        }

        void touch() {
            this.lastAccessNanos = System.nanoTime();
        }
    }
}