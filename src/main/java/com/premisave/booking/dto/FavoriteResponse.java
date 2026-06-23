package com.premisave.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {

    private String message;
    private boolean success;
    private String listingId;
    private String listingTitle;   // nullable — populated when listing details are available
}