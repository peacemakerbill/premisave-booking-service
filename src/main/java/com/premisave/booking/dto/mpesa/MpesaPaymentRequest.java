package com.premisave.booking.dto.mpesa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MpesaPaymentRequest {

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    /**
     * Kenyan phone number in format 254XXXXXXXXX.
     * e.g. 254712345678
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^254[0-9]{9}$", message = "Phone must be in format 254XXXXXXXXX")
    private String phoneNumber;
}