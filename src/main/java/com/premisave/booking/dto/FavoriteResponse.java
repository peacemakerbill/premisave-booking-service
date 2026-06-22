package com.premisave.booking.dto;

import lombok.Data;

@Data
public class FavoriteResponse {
    private String message;
    private boolean success = true;
    private String listingId;
}