package com.finance.smartLedger.payment.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WebhookSignatureValidatorTest {

  private WebhookSignatureValidator validator;
  private static final String SECRET_KEY = "sk_test_d00a8a661d4dec8e0eeae18fac6ccfa331bce99c";

  @BeforeEach
  void setUp() {
    validator = new WebhookSignatureValidator(SECRET_KEY);
  }

  @Test
  void validatePaystackSignature_ValidSignature_ShouldReturnTrue() {
    // Given
    String payload = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"REF-12345\"}}";
    String validSignature = calculateExpectedSignature(payload);

    // When
    boolean result = validator.validatePaystackSignature(payload, validSignature);

    // Then - This test should pass both before and after the fix
    assertTrue(result);
  }

  @Test
  void validatePaystackSignature_InvalidSignature_ShouldReturnFalse() {
    // Given
    String payload = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"REF-12345\"}}";
    String invalidSignature = "invalid_signature_12345";

    // When
    boolean result = validator.validatePaystackSignature(payload, invalidSignature);

    // Then - This test should pass both before and after the fix
    assertFalse(result);
  }

  @Test
  void validatePaystackSignature_DifferentPayload_ShouldReturnFalse() {
    // Given
    String payload1 = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"REF-12345\"}}";
    String payload2 = "{\"event\":\"charge.failed\",\"data\":{\"reference\":\"REF-67890\"}}";
    String signatureForPayload1 = calculateExpectedSignature(payload1);

    // When
    boolean result = validator.validatePaystackSignature(payload2, signatureForPayload1);

    // Then - This test should pass both before and after the fix
    assertFalse(result);
  }

  @Test
  void validatePaystackSignature_NullSignature_ShouldReturnFalse() {
    // Given
    String payload = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"REF-12345\"}}";

    // When
    boolean result = validator.validatePaystackSignature(payload, null);

    // Then - This test should pass both before and after the fix
    assertFalse(result);
  }

  @Test
  void validatePaystackSignature_EmptySignature_ShouldReturnFalse() {
    // Given
    String payload = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"REF-12345\"}}";
    String emptySignature = "";

    // When
    boolean result = validator.validatePaystackSignature(payload, emptySignature);

    // Then - This test should pass both before and after the fix
    assertFalse(result);
  }

  // Helper method to calculate expected signature (same logic as the validator)
  private String calculateExpectedSignature(String payload) {
    try {
      java.nio.charset.Charset utf8 = java.nio.charset.StandardCharsets.UTF_8;
      javax.crypto.Mac sha512Hmac = javax.crypto.Mac.getInstance("HmacSHA512");
      javax.crypto.spec.SecretKeySpec secretKey =
          new javax.crypto.spec.SecretKeySpec(SECRET_KEY.getBytes(utf8), "HmacSHA512");
      sha512Hmac.init(secretKey);
      byte[] hmacBytes = sha512Hmac.doFinal(payload.getBytes(utf8));
      return bytesToHex(hmacBytes);
    } catch (Exception e) {
      throw new RuntimeException("Failed to calculate signature", e);
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
