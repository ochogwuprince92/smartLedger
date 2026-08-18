package com.finance.smartLedger.payment.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "payment.gateway.paystack")
public class PaystackProperties {
    private String secretKey;
    private String publicKey;
    private String callbackUrl;
    private String webhookUrl;
    private String apiUrl;
}
