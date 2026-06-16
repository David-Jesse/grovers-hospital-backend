package com.djio.grover_hospital.security.ratelimit;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registers RateLimitFilter at the highest possible precedence so it runs BEFORE
 * Spring Security's filter chain (which sits at order -100). This means brute-force
 * attempts are rejected before any password hashing happens.
 */
@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimitFilter rateLimitFilter(RateLimitProperties properties) {
        return new RateLimitFilter(properties);
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/auth/*", "/api/v1/auth/*");
        return registration;
    }
}