package com.premisave.booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @Value("${rate-limit.requests-per-minute:100}")
    public int requestsPerMinute;
}