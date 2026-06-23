package com.premisave.booking.client;

import com.premisave.booking.config.FeignConfig;
import com.premisave.booking.dto.auth_service.UserSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Auth service has no context-path, so paths are direct.
 * Port: 8080
 */
@FeignClient(
    name = "auth-service",
    url = "${auth.service.url:http://localhost:8080}",
    configuration = FeignConfig.class
)
public interface AuthServiceClient {

    @GetMapping("/profile/me")
    UserSummaryResponse getCurrentUser(@RequestHeader("Authorization") String token);

    @GetMapping("/profile/user/{userId}")
    UserSummaryResponse getUserSummary(@PathVariable String userId,
                                       @RequestHeader("Authorization") String token);
}