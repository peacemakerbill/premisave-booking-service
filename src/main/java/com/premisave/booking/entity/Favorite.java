package com.premisave.booking.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "favorites")
@CompoundIndex(name = "user_listing_idx", def = "{'userId': 1, 'listingId': 1}", unique = true)
public class Favorite extends BaseEntity {

    private String userId;
    private String listingId;

    /** Snapshot of listing title at the time it was favorited */
    private String listingTitle;
}