package com.premisave.booking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mpesa.daraja")
public class MpesaConfig {

    private String consumerKey;
    private String consumerSecret;
    private String shortcode;
    private String passkey;
    private String callbackUrl;

    /** "sandbox" or "production" */
    private String environment = "sandbox";

    /** Base URL for Daraja API */
    private String baseUrl = "https://sandbox.safaricom.co.ke";

    public boolean isSandbox() {
        return "sandbox".equalsIgnoreCase(environment);
    }
}