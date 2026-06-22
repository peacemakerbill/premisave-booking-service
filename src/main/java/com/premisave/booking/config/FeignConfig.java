package com.premisave.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public JwtFeignInterceptor jwtFeignInterceptor() {
        return new JwtFeignInterceptor();
    }
}