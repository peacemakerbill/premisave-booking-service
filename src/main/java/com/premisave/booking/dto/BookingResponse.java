package com.premisave.booking.dto;

import com.premisave.booking.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    /**
     * Convenient constructor for creating success responses
     */
    public BookingResponse(String bookingId, String listingId, BookingStatus status,
                           LocalDateTime checkInDate, LocalDateTime checkOutDate,
                           int numberOfGuests, BigDecimal totalAmount, String currency,
                           String message) {
        
        this.bookingId = bookingId;
        this.listingId = listingId;
        this.status = status;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.message = message;
        this.success = true;
    }
}