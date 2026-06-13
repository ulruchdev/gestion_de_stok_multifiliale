package com.stockmaster.auth.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final StringRedisTemplate redisTemplate;
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        if (!req.getRequestURI().equals("/api/v1/auth/login")) {
            chain.doFilter(req, res);
            return;
        }

        String key = "rate_limit:login:" + getClientIp(req);
        String attemptsStr = redisTemplate.opsForValue().get(key);
        int attempts = (attemptsStr != null) ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= MAX_ATTEMPTS) {
            log.warn("Rate limit atteint — IP {} bloquée pour 15 min", getClientIp(req));
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("{\"errorCode\":\"AUTH_429\",\"detail\":\"Trop de tentatives. Réessayez dans 15 minutes.\"}");
            return;
        }

        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, WINDOW);
        chain.doFilter(req, res);
    }

    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (xff != null) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }
}
