package com.ga.lanceify.identity.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket(int capacity, Duration refillDuration) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, refillDuration)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket getBucket(String key, int capacity, Duration duration) {
        return buckets.computeIfAbsent(key, k -> createBucket(capacity, duration));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String ipAddress = clientIp(request);
        Bucket bucket = null;
        if ("POST".equals(request.getMethod())) {
            if (path.equals("/api/auth/login")) {
                bucket = getBucket("login:" + ipAddress, 5, Duration.ofMinutes(1));
            } else if (path.equals("/api/auth/register")) {
                bucket = getBucket("register:" + ipAddress, 5, Duration.ofMinutes(1));
            }
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            response.setHeader("Retry-After", String.valueOf(60));
            SecurityErrorWriter.write(request, response, 429, "TOO_MANY_REQUESTS",
                    "Too many requests. Please try again in 60 seconds.");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
