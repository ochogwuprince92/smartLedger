package com.finance.smartLedger.payment.presentation;

import static org.junit.jupiter.api.Assertions.*;

import com.finance.smartLedger.payment.infrastructure.security.WebhookSignatureValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test to verify webhook signature validation works correctly.
 * 
 * This test validates that:
 * 1. Valid HMAC signatures are accepted
 * 2. Invalid HMAC signatures are rejected
 * 3. Missing signatures are rejected
 */
@ExtendWith(MockitoExtension.class)
class WebhookSecurityIntegrationTest {

  @Test
  void validHmacSignature_ShouldBeAccepted() {
    // Test that a valid HMAC signature is accepted
    String payload = "test-payload";
    String secret = "test-secret";
    String signature = calculateHmacSha512(payload, secret);
    
    WebhookSignatureValidator webhookSignatureValidator = new WebhookSignatureValidator(secret);
    
    boolean isValid = webhookSignatureValidator.validatePaystackSignature(payload, signature);
    assertTrue(isValid, "Valid HMAC signature should be accepted");
  }

  @Test
  void invalidHmacSignature_ShouldBeRejected() {
    // Test that an invalid HMAC signature is rejected
    String payload = "test-payload";
    String secret = "test-secret";
    String invalidSignature = "invalid-signature";
    
    WebhookSignatureValidator webhookSignatureValidator = new WebhookSignatureValidator(secret);
    
    boolean isValid = webhookSignatureValidator.validatePaystackSignature(payload, invalidSignature);
    assertFalse(isValid, "Invalid HMAC signature should be rejected");
  }

  @Test
  void missingSignature_ShouldBeRejected() {
    // Test that a missing signature is rejected
    String payload = "test-payload";
    String secret = "test-secret";
    
    WebhookSignatureValidator webhookSignatureValidator = new WebhookSignatureValidator(secret);
    
    boolean isValid = webhookSignatureValidator.validatePaystackSignature(payload, null);
    assertFalse(isValid, "Missing signature should be rejected");
  }

  @Test
  void emptySignature_ShouldBeRejected() {
    // Test that an empty signature is rejected
    String payload = "test-payload";
    String secret = "test-secret";
    
    WebhookSignatureValidator webhookSignatureValidator = new WebhookSignatureValidator(secret);
    
    boolean isValid = webhookSignatureValidator.validatePaystackSignature(payload, "");
    assertFalse(isValid, "Empty signature should be rejected");
  }

  private String calculateHmacSha512(String data, String secret) {
    try {
      javax.crypto.Mac sha512Hmac = javax.crypto.Mac.getInstance("HmacSHA512");
      javax.crypto.spec.SecretKeySpec secretKey =
          new javax.crypto.spec.SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA512");
      sha512Hmac.init(secretKey);
      byte[] hmacBytes = sha512Hmac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return bytesToHex(hmacBytes);
    } catch (Exception e) {
      throw new RuntimeException("Failed to calculate HMAC", e);
    }
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}