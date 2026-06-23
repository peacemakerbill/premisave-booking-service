package com.premisave.booking.enums;

public enum BookingStatus {
    PENDING,        // Created, awaiting payment
    PAYMENT_INITIATED, // STK push sent, waiting for user to confirm on phone
    CONFIRMED,      // Payment received and confirmed
    CANCELLED,      // Cancelled by user or auto-expired
    COMPLETED,      // Stay completed
    REJECTED        // Rejected by owner (inquiry type)
}