package com.premisave.booking.client;

import com.premisave.booking.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "listing-service",
    url = "${listing.service.url:http://localhost:8082}",
    configuration = FeignConfig.class
)
public interface ListingServiceClient {

    @GetMapping("/listings/{id}")
    Object getListingById(@PathVariable String id, @RequestHeader("Authorization") String token);
}