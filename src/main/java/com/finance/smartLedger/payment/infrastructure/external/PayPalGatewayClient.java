package com.finance.smartLedger.payment.infrastructure.external;

import java.math.BigDecimal;
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
public class PayPalGatewayClient implements PaymentGatewayClient {

  private final RestTemplate restTemplate;
  private final String clientId;
  private final String clientSecret;
  private final String baseUrl;
  private String accessToken;

  public PayPalGatewayClient(
      RestTemplate restTemplate,
      @Value("${payment.gateway.paypal.client-id}") String clientId,
      @Value("${payment.gateway.paypal.client-secret}") String clientSecret,
      @Value("${payment.gateway.paypal.base-url:https://api-m.sandbox.paypal.com}")
          String baseUrl) {
    this.restTemplate = restTemplate;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.baseUrl = baseUrl;
  }

  private String getAccessToken() {
    if (accessToken == null) {
      String authUrl = baseUrl + "/v1/oauth2/token";
      HttpHeaders headers = new HttpHeaders();
      headers.setBasicAuth(clientId, clientSecret);
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);
      Map<String, Object> response = restTemplate.postForObject(authUrl, request, Map.class);
      accessToken = (String) response.get("access_token");
    }
    return accessToken;
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
      String url = baseUrl + "/v2/checkout/orders";

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(getAccessToken());
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> amountMap = Map.of("currency_code", currency, "value", amount.toString());

      Map<String, Object> requestBody =
          Map.of(
              "intent",
              "CAPTURE",
              "purchase_units",
              java.util.List.of(
                  Map.of(
                      "amount",
                      amountMap,
                      "description",
                      description,
                      "custom_id",
                      metadata != null ? metadata.getOrDefault("custom_id", "") : "")));

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

      String orderId = (String) response.get("id");
      log.info("PayPal order created: {}", orderId);
      return orderId;

    } catch (Exception e) {
      log.error("Failed to initiate PayPal payment", e);
      throw new RuntimeException("Failed to initiate payment with PayPal", e);
    }
  }

  @Override
  public String retrievePaymentStatus(String paymentId) {
    try {
      String url = baseUrl + "/v2/checkout/orders/" + paymentId;

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(getAccessToken());

      HttpEntity<Void> request = new HttpEntity<>(headers);
      Map<String, Object> response =
          restTemplate
              .exchange(url, org.springframework.http.HttpMethod.GET, request, Map.class)
              .getBody();

      return (String) response.get("status");

    } catch (Exception e) {
      log.error("Failed to retrieve PayPal payment status: {}", paymentId, e);
      throw new RuntimeException("Failed to retrieve payment status from PayPal", e);
    }
  }

  @Override
  public boolean refundPayment(String paymentId, BigDecimal amount) {
    try {
      String url = baseUrl + "/v2/payments/captures/" + paymentId + "/refund";

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(getAccessToken());
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> requestBody =
          Map.of("amount", Map.of("value", amount.toString(), "currency_code", "USD"));

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

      log.info("PayPal refund processed: {}", response.get("id"));
      return "COMPLETED".equals(response.get("status"));

    } catch (Exception e) {
      log.error("Failed to refund PayPal payment: {}", paymentId, e);
      throw new RuntimeException("Failed to refund payment with PayPal", e);
    }
  }

  @Override
  public boolean cancelPayment(String paymentId) {
    try {
      String url = baseUrl + "/v2/checkout/orders/" + paymentId;

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(getAccessToken());
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> requestBody = Map.of("intent", "CANCEL");

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      restTemplate.postForObject(url, request, Map.class);

      log.info("PayPal order cancelled: {}", paymentId);
      return true;

    } catch (Exception e) {
      log.error("Failed to cancel PayPal payment: {}", paymentId, e);
      throw new RuntimeException("Failed to cancel payment with PayPal", e);
    }
  }
}
