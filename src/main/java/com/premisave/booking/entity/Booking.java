package com.premisave.booking.entity;

import com.premisave.booking.enums.BookingStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "bookings")
public class Booking extends BaseEntity {

    private String userId;
    private String listingId;
    private String ownerId;

    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private int numberOfGuests;

    private BigDecimal totalAmount;
    private String currency = "KES";

    private BookingStatus status = BookingStatus.PENDING;
    private String paymentId;
    private LocalDateTime confirmedAt;
}