package com.premisave.booking.client;

import com.premisave.booking.config.FeignConfig;
import com.premisave.booking.dto.listing_service.ListingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Listing service runs on port 8082 with context-path /api.
 * All endpoints are therefore reachable at http://localhost:8082/api/...
 *
 */
@FeignClient(
    name = "listing-service",
    url = "${listing.service.url:http://localhost:8082}",
    configuration = FeignConfig.class
)
public interface ListingServiceClient {

    @GetMapping("/api/listings/{id}")
    ListingResponse getListingById(@PathVariable String id,
                                   @RequestHeader("Authorization") String token);
}