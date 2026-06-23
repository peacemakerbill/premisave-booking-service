package com.premisave.booking.dto;

import com.premisave.booking.enums.BookingStatus;
import com.premisave.booking.enums.BookingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private String bookingId;
    private String listingId;
    private String listingTitle;
    private BookingType bookingType;
    private BookingStatus status;

    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private int numberOfGuests;

    private BigDecimal totalAmount;
    private String currency;

    /** M-Pesa checkout request ID — included so the client can poll status */
    private String mpesaCheckoutRequestId;

    private String message;
    private boolean success;
}