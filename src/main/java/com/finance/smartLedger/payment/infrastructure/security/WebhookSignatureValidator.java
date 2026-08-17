package com.finance.smartLedger.payment.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebhookSignatureValidator {

  private final String paystackSecretKey;

  public WebhookSignatureValidator(
      @Value("${payment.gateway.paystack.secret-key}") String paystackSecretKey) {
    this.paystackSecretKey = paystackSecretKey;
  }

  public boolean validatePaystackSignature(String payload, String signature) {
    try {
      String expectedSignature = calculateHmacSha512(payload, paystackSecretKey);
      boolean isValid = java.security.MessageDigest.isEqual(
          expectedSignature.getBytes(StandardCharsets.UTF_8),
          signature != null ? signature.getBytes(StandardCharsets.UTF_8) : new byte[0]);

      if (!isValid) {
        log.warn("Invalid Paystack webhook signature");
      }

      return isValid;
    } catch (Exception e) {
      log.error("Error validating Paystack webhook signature", e);
      return false;
    }
  }

  private String calculateHmacSha512(String data, String secret)
      throws NoSuchAlgorithmException, InvalidKeyException {
    Mac sha512Hmac = Mac.getInstance("HmacSHA512");
    SecretKeySpec secretKey =
        new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
    sha512Hmac.init(secretKey);
    byte[] hmacBytes = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    return bytesToHex(hmacBytes);
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
