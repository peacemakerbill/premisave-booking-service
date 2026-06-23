package com.premisave.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {

    @NotBlank(message = "Listing ID is required")
    private String listingId;

    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    private LocalDateTime checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDateTime checkOutDate;

    @Min(value = 1, message = "Number of guests must be at least 1")
    private int numberOfGuests = 1;

    /**
     * Phone number for M-Pesa payment (Kenyan format: 254XXXXXXXXX).
     * Required only for SHORT_TERM_RENTAL bookings that need payment.
     */
    @Pattern(regexp = "^254[0-9]{9}$", message = "Phone number must be in format 254XXXXXXXXX")
    private String paymentPhone;
}