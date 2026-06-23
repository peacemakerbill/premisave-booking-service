package com.premisave.booking.config;

import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public JwtFeignInterceptor jwtFeignInterceptor() {
        return new JwtFeignInterceptor();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Custom error decoder so Feign errors surface with meaningful messages
     * instead of generic FeignException.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            String msg = "Error calling " + methodKey + " — HTTP " + response.status();
            return switch (response.status()) {
                case 404 -> new RuntimeException("Resource not found: " + methodKey);
                case 401 -> new RuntimeException("Unauthorized when calling downstream service: " + methodKey);
                case 403 -> new RuntimeException("Access denied by downstream service: " + methodKey);
                default  -> new RuntimeException(msg);
            };
        };
    }
}