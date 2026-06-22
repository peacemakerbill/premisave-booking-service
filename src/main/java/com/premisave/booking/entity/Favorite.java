package com.premisave.booking.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "favorites")
public class Favorite extends BaseEntity {

    private String userId;
    private String listingId;
}