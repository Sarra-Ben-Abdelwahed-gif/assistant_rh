package com.example.assistant_rh.filter;

import com.example.assistant_rh.config
    .RateLimitConfig;
import com.fasterxml.jackson.databind
    .ObjectMapper;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter
    .OncePerRequestFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter
        extends OncePerRequestFilter {

    private final RateLimitConfig
        rateLimitConfig;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String ip = getClientIp(request);
        String path = request.getRequestURI();

        Bucket bucket;

        
        if (path.startsWith("/api/auth/login")
                || path.startsWith(
                    "/api/auth/register")) {
            bucket = rateLimitConfig
                .resolveAuthBucket(ip);
        } else {
            bucket = rateLimitConfig
                .resolveBucket(ip);
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(
                request, response);
        } else {
            log.warn("Rate limit exceeded for IP : {}",
                ip);
            response.setStatus(
                HttpStatus.TOO_MANY_REQUESTS
                    .value());
            response.setContentType(
                MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> body = Map.of(
                "timestamp",
                    LocalDateTime.now().toString(),
                "status", 429,
                "error", "Too Many Requests",
                "message",
                    "Too many requests. Please try again in 1 minute.");
            response.getWriter().write(
                objectMapper.writeValueAsString(
                    body));
        }
    }

    private String getClientIp(
            HttpServletRequest request) {
        String forwarded = request.getHeader(
            "X-Forwarded-For");
        if (forwarded != null
                && !forwarded.isEmpty())
            return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
