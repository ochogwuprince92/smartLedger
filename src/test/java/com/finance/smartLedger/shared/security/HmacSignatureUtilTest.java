package com.finance.smartLedger.shared.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HmacSignatureUtilTest {

  private HmacSignatureUtil hmacSignatureUtil;
  private static final String SECRET = "test-secret-key";

  @BeforeEach
  void setUp() {
    hmacSignatureUtil = new HmacSignatureUtil();
  }

  @Test
  void testCalculateSignature() {
    String payload = "{\"requestId\":\"test-123\",\"data\":\"sample\"}";
    String signature = hmacSignatureUtil.calculateSignature(payload, SECRET);

    assertNotNull(signature);
    assertFalse(signature.isEmpty());
    assertEquals(64, signature.length()); // SHA256 produces 64 hex characters
  }

  @Test
  void testVerifySignature_ValidSignature() {
    String payload = "{\"requestId\":\"test-123\",\"data\":\"sample\"}";
    String signature = hmacSignatureUtil.calculateSignature(payload, SECRET);

    assertTrue(hmacSignatureUtil.verifySignature(payload, signature, SECRET));
  }

  @Test
  void testVerifySignature_InvalidSignature() {
    String payload = "{\"requestId\":\"test-123\",\"data\":\"sample\"}";
    String wrongSignature = "invalid-signature";

    assertFalse(hmacSignatureUtil.verifySignature(payload, wrongSignature, SECRET));
  }

  @Test
  void testVerifySignature_DifferentPayload() {
    String payload1 = "{\"requestId\":\"test-123\"}";
    String payload2 = "{\"requestId\":\"test-456\"}";
    String signature1 = hmacSignatureUtil.calculateSignature(payload1, SECRET);

    assertFalse(hmacSignatureUtil.verifySignature(payload2, signature1, SECRET));
  }

  @Test
  void testVerifySignature_DifferentSecret() {
    String payload = "{\"requestId\":\"test-123\"}";
    String signature1 = hmacSignatureUtil.calculateSignature(payload, SECRET);
    String differentSecret = "different-secret";

    assertFalse(hmacSignatureUtil.verifySignature(payload, signature1, differentSecret));
  }

  @Test
  void testCalculateSignature_Consistency() {
    String payload = "{\"requestId\":\"test-123\"}";
    String signature1 = hmacSignatureUtil.calculateSignature(payload, SECRET);
    String signature2 = hmacSignatureUtil.calculateSignature(payload, SECRET);

    assertEquals(signature1, signature2);
  }

  @Test
  void testCalculateSignature_EmptyPayload() {
    String payload = "";
    String signature = hmacSignatureUtil.calculateSignature(payload, SECRET);

    assertNotNull(signature);
    assertEquals(64, signature.length());
  }
}
