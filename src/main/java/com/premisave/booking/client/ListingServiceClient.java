package com.premisave.booking.client;

import com.premisave.booking.config.FeignConfig;
import com.premisave.booking.dto.listing_service.ListingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Listing service runs on port 8082 with context-path /api.
 * The context-path is already applied by the server, so paths here
 * must NOT include /api — otherwise requests hit /api/api/...
 */
@FeignClient(
    name = "listing-service",
    url = "${listing.service.url:http://localhost:8082}",
    configuration = FeignConfig.class
)
public interface ListingServiceClient {

    @GetMapping("/listings/{id}")
    ListingResponse getListingById(@PathVariable String id,
                                   @RequestHeader("Authorization") String token);
}