package com.premisave.booking.dto.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Represents the full JSON payload Safaricom POSTs to our callback URL
 * after an STK push completes (success or failure).
 */
@Data
public class MpesaCallbackRequest {

    @JsonProperty("Body")
    private Body body;

    @Data
    public static class Body {

        @JsonProperty("stkCallback")
        private StkCallback stkCallback;
    }

    @Data
    public static class StkCallback {

        @JsonProperty("MerchantRequestID")
        private String merchantRequestID;

        @JsonProperty("CheckoutRequestID")
        private String checkoutRequestID;

        /** 0 = success, anything else = failure */
        @JsonProperty("ResultCode")
        private int resultCode;

        @JsonProperty("ResultDesc")
        private String resultDesc;

        @JsonProperty("CallbackMetadata")
        private CallbackMetadata callbackMetadata;
    }

    @Data
    public static class CallbackMetadata {

        @JsonProperty("Item")
        private List<MetadataItem> item;
    }

    @Data
    public static class MetadataItem {

        @JsonProperty("Name")
        private String name;

        @JsonProperty("Value")
        private Object value;
    }
}