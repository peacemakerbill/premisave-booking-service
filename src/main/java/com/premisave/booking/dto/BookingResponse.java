package com.premisave.booking.dto;

import com.premisave.booking.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private String bookingId;
    private String listingId;
    private BookingStatus status;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private int numberOfGuests;
    private BigDecimal totalAmount;
    private String currency;
    private String message;
    private boolean success = true;
}