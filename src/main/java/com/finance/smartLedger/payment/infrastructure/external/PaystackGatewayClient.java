package com.finance.smartLedger.payment.infrastructure.external;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class PaystackGatewayClient implements PaymentGatewayClient {

  private final RestTemplate restTemplate;
  private final String secretKey;
  private final String baseUrl;

  public PaystackGatewayClient(
      RestTemplate restTemplate,
      @Value("${payment.gateway.paystack.secret-key}") String secretKey,
      @Value("${payment.gateway.paystack.base-url:https://api.paystack.co}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.secretKey = secretKey;
    this.baseUrl = baseUrl;
  }

  @Override
  public String initiatePayment(
      BigDecimal amount,
      String currency,
      String description,
      String customerEmail,
      String customerName,
      Map<String, String> metadata) {

    try {
      String url = baseUrl + "/transaction/initialize";

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(secretKey);
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

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

      Map<String, Object> data = (Map<String, Object>) response.get("data");
      String reference = (String) data.get("reference");

      log.info("Paystack transaction initialized: {}", reference);
      return reference;

    } catch (Exception e) {
      log.error("Failed to initiate Paystack payment", e);
      throw new RuntimeException("Failed to initiate payment with Paystack", e);
    }
  }

  @Override
  public String retrievePaymentStatus(String paymentId) {
    try {
      String url = baseUrl + "/transaction/verify/" + paymentId;

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(secretKey);

      HttpEntity<Void> request = new HttpEntity<>(headers);
      Map<String, Object> response =
          restTemplate
              .exchange(url, org.springframework.http.HttpMethod.GET, request, Map.class)
              .getBody();

      Map<String, Object> data = (Map<String, Object>) response.get("data");
      return (String) data.get("status");

    } catch (Exception e) {
      log.error("Failed to retrieve Paystack payment status: {}", paymentId, e);
      throw new RuntimeException("Failed to retrieve payment status from Paystack", e);
    }
  }

  @Override
  public boolean refundPayment(String paymentId, BigDecimal amount) {
    try {
      String url = baseUrl + "/refund";

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(secretKey);
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
