package com.premisave.booking.dto.listing_service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO mirroring the fields returned by the Listing Service /api/listings/{id} endpoint.
 * Field names match the Listing entity (Jackson serialisation).
 */
@Data
public class ListingResponse {

    private String id;
    private String ownerId;
    private String title;
    private String description;

    /** Maps to ListingCategory enum serialised as String (e.g. "SHORT_TERM_RENTAL") */
    private String category;

    /** Maps to ListingStatus enum serialised as String (e.g. "ACTIVE") */
    private String status;

    private BigDecimal price;
    private String currency;   // e.g. "USD"

    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private String country;

    private String mainImageUrl;
    private List<String> imageUrls;

    /**
     * Jackson serialises a boolean field named "isPromoted" as "promoted" in JSON.
     * Using @JsonProperty to handle both conventions defensively.
     */
    @JsonProperty("promoted")
    private boolean promoted;

    private LocalDateTime promotionEndDate;

    // ── ShortTermRental extras (present only when category = SHORT_TERM_RENTAL) ──
    private Integer maxGuests;
    private Integer bedrooms;
    private Integer bathrooms;
    private Boolean hasWifi;
    private Boolean hasKitchen;
    private List<String> amenities;

    // ── LongTermRental extras ──
    private Integer minLeaseMonths;
    private Boolean furnished;

    // ── HouseSale extras ──
    private Integer floors;
    private Double plotSize;
    private Boolean hasGarage;
    private String propertyType;

    // ── LandSale extras ──
    private Double sizeInAcres;
    private String landUseType;
    private Boolean hasTitleDeed;

    // ── Lease extras ──
    private Integer leaseDurationMonths;
    private BigDecimal depositAmount;
    private String leaseTerms;
    private Boolean renewable;
}