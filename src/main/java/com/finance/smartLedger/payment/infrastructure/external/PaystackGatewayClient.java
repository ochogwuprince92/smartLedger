package com.finance.smartLedger.payment.infrastructure.external;

import com.finance.smartLedger.payment.application.dto.PaymentVerifyResponse;
import com.finance.smartLedger.payment.infrastructure.config.PaystackProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class PaystackGatewayClient implements PaymentGatewayClient {

  private final RestTemplate restTemplate;
  private final PaystackProperties paystackProperties;

  public PaystackGatewayClient(
      RestTemplate restTemplate,
      PaystackProperties paystackProperties) {
    this.restTemplate = restTemplate;
    this.paystackProperties = paystackProperties;
  }

  @Override
  public PaystackInitiationResult initiatePayment(
      BigDecimal amount,
      String currency,
      String description,
      String customerEmail,
      String customerName,
      Map<String, String> metadata,
      String callbackUrl) {

    try {
      String url = paystackProperties.getApiUrl() + "/transaction/initialize";

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(paystackProperties.getSecretKey());
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put(
          "amount", amount.multiply(new BigDecimal("100")).longValue()); // Paystack uses kobo
      requestBody.put("email", customerEmail);

      if (currency != null) {
        requestBody.put("currency", currency);
      }
      if (description != null) {
        requestBody.put("description", description);
      }
      if (metadata != null) {
        requestBody.put("metadata", metadata);
      }
      // Use provided callbackUrl, fallback to configured one
      String effectiveCallbackUrl = callbackUrl != null ? callbackUrl : paystackProperties.getCallbackUrl();
      if (effectiveCallbackUrl != null) {
        requestBody.put("callback_url", effectiveCallbackUrl);
      }

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

      Object dataObj = response.get("data");
      if (!(dataObj instanceof Map)) {
        log.error("Payment initiation data is not a Map: {}", dataObj.getClass().getName());
        throw new RuntimeException("Failed to initiate payment with Paystack: Invalid data format");
      }
      
      Map<String, Object> data = (Map<String, Object>) dataObj;
      String reference = data.get("reference") != null ? data.get("reference").toString() : null;
      String authorizationUrl = data.get("authorization_url") != null ? data.get("authorization_url").toString() : null;
      String accessCode = data.get("access_code") != null ? data.get("access_code").toString() : null;

      log.info("Paystack transaction initialized: {}", reference);
      return new PaystackInitiationResult(reference, authorizationUrl, accessCode);

    } catch (Exception e) {
      log.error("Failed to initiate Paystack payment", e);
      throw new RuntimeException("Failed to initiate payment with Paystack", e);
    }
  }

  @Override
  public String retrievePaymentStatus(String paymentId) {
    try {
      String url = paystackProperties.getApiUrl() + "/transaction/verify/" + paymentId;

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(paystackProperties.getSecretKey());

      HttpEntity<Void> request = new HttpEntity<>(headers);
      Map<String, Object> response =
          restTemplate
              .exchange(url, org.springframework.http.HttpMethod.GET, request, Map.class)
              .getBody();

      Object dataObj = response.get("data");
      if (!(dataObj instanceof Map)) {
        log.error("Payment status data is not a Map: {}", dataObj.getClass().getName());
        throw new RuntimeException("Failed to retrieve payment status: Invalid data format");
      }
      
      Map<String, Object> data = (Map<String, Object>) dataObj;
      return data.get("status") != null ? data.get("status").toString() : null;

    } catch (Exception e) {
      log.error("Failed to retrieve Paystack payment status: {}", paymentId, e);
      throw new RuntimeException("Failed to retrieve payment status from Paystack", e);
    }
  }

  @Override
  public PaymentVerifyResponse verifyPayment(String reference) {
    try {
      String url = paystackProperties.getApiUrl() + "/transaction/verify/" + reference;

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(paystackProperties.getSecretKey());

      HttpEntity<Void> request = new HttpEntity<>(headers);
      Map<String, Object> response =
          restTemplate
              .exchange(url, org.springframework.http.HttpMethod.GET, request, Map.class)
              .getBody();

      boolean status = (Boolean) response.getOrDefault("status", false);
      String message = (String) response.getOrDefault("message", "Verification failed");

      if (status && response.containsKey("data")) {
        Object dataObj = response.get("data");
        if (!(dataObj instanceof Map)) {
          log.error("Payment verification data is not a Map: {}", dataObj.getClass().getName());
          return new PaymentVerifyResponse(false, "Payment verification failed: Invalid data format", null);
        }
        
        Map<String, Object> data = (Map<String, Object>) dataObj;

        // Parse customer information
        PaymentVerifyResponse.PaymentData.Customer customer = null;
        if (data.containsKey("customer")) {
          Object customerObj = data.get("customer");
          if (customerObj instanceof Map) {
            Map<String, Object> customerData = (Map<String, Object>) customerObj;
            customer = new PaymentVerifyResponse.PaymentData.Customer(
                customerData.get("email") != null ? customerData.get("email").toString() : null,
                customerData.get("customer_code") != null ? customerData.get("customer_code").toString() : null);
          }
        }

        // Parse authorization information
        PaymentVerifyResponse.PaymentData.Authorization authorization = null;
        if (data.containsKey("authorization")) {
          Object authObj = data.get("authorization");
          if (authObj instanceof Map) {
            Map<String, Object> authData = (Map<String, Object>) authObj;
            authorization = new PaymentVerifyResponse.PaymentData.Authorization(
                authData.get("authorization_code") != null ? authData.get("authorization_code").toString() : null,
                authData.get("bin") != null ? authData.get("bin").toString() : null,
                authData.get("last4") != null ? authData.get("last4").toString() : null,
                authData.get("exp_month") != null ? authData.get("exp_month").toString() : null,
                authData.get("exp_year") != null ? authData.get("exp_year").toString() : null,
                authData.get("card_type") != null ? authData.get("card_type").toString() : null,
                authData.get("bank") != null ? authData.get("bank").toString() : null);
          }
        }

        // Parse metadata
        Map<String, Object> metadata = new HashMap<>();
        if (data.containsKey("metadata")) {
          Object metadataObj = data.get("metadata");
          if (metadataObj instanceof Map) {
            Map<String, Object> metadataData = (Map<String, Object>) metadataObj;
            metadata.putAll(metadataData);
          }
        }

        PaymentVerifyResponse.PaymentData paymentData = new PaymentVerifyResponse.PaymentData(
            (String) data.get("reference"),
            (String) data.get("gateway_response"),
            parseDateTime((String) data.get("paid_at")),
            parseDateTime((String) data.get("createdAt")),
            (String) data.get("channel"),
            (String) data.get("currency"),
            data.get("amount") != null ? data.get("amount").toString() : null,
            metadata,
            customer,
            authorization);

        return new PaymentVerifyResponse(status, message, paymentData);
      } else {
        return new PaymentVerifyResponse(status, message, null);
      }

    } catch (Exception e) {
      log.error("Failed to verify Paystack payment: {}", reference, e);
      return new PaymentVerifyResponse(false, "Payment verification failed: " + e.getMessage(), null);
    }
  }

  private LocalDateTime parseDateTime(String dateTime) {
    if (dateTime == null || dateTime.isEmpty()) {
      return null;
    }
    try {
      return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
    } catch (DateTimeParseException e) {
      log.warn("Failed to parse datetime: {}", dateTime);
      return null;
    }
  }

  @Override
  public boolean refundPayment(String paymentId, BigDecimal amount) {
    try {
      String url = paystackProperties.getApiUrl() + "/refund";

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(paystackProperties.getSecretKey());
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("transaction", paymentId);
      requestBody.put("amount", amount.multiply(new BigDecimal("100")).longValue());

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

      log.info("Paystack refund processed: {}", response.get("data"));
      return true;

    } catch (Exception e) {
      log.error("Failed to refund Paystack payment: {}", paymentId, e);
      throw new RuntimeException("Failed to refund payment with Paystack", e);
    }
  }

  @Override
  public boolean cancelPayment(String paymentId) {
    try {
      // Paystack doesn't have a direct cancel API, payments expire after some time
      // We'll mark it as cancelled in our system
      log.info("Paystack payment marked as cancelled: {}", paymentId);
      return true;

    } catch (Exception e) {
      log.error("Failed to cancel Paystack payment: {}", paymentId, e);
      throw new RuntimeException("Failed to cancel payment with Paystack", e);
    }
  }
}
