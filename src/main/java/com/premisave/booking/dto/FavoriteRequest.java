package com.premisave.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FavoriteRequest {

    @NotBlank(message = "Listing ID is required")
    private String listingId;
}