package com.premisave.booking.entity;

import com.premisave.booking.enums.BookingStatus;
import com.premisave.booking.enums.BookingType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "bookings")
public class Booking extends BaseEntity {

    @Indexed
    private String userId;

    @Indexed
    private String listingId;

    private String ownerId;

    /** Human-readable listing title stored at booking time (immutable snapshot) */
    private String listingTitle;

    private BookingType bookingType = BookingType.SHORT_TERM_RENTAL;

    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private int numberOfGuests;

    /** Amount in KES (canonical currency) */
    private BigDecimal totalAmount;
    private String currency = "KES";

    /** Original listing price currency (e.g. USD) — stored for audit */
    private String originalCurrency;

    /** Exchange rate used: 1 originalCurrency = ? KES */
    private BigDecimal exchangeRateUsed;

    private BookingStatus status = BookingStatus.PENDING;

    /** M-Pesa checkout request ID returned by STK push */
    private String mpesaCheckoutRequestId;

    /** M-Pesa merchant request ID */
    private String mpesaMerchantRequestId;

    /** M-Pesa transaction receipt (e.g. QGH7XXXXXXX) */
    private String mpesaReceiptNumber;

    /** Phone number used for M-Pesa payment */
    private String paymentPhone;

    private LocalDateTime paymentInitiatedAt;
    private LocalDateTime paymentConfirmedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;

    /** Reason for cancellation, if applicable */
    private String cancellationReason;

    /** Auto-set to true when pending timeout job fires */
    private boolean autoExpired = false;
}