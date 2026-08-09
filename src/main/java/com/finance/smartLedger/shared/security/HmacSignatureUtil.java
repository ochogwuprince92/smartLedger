package com.finance.smartLedger.shared.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HmacSignatureUtil {

  private static final String HMAC_SHA256 = "HmacSHA256";

  public String calculateSignature(String payload, String secret) {
    try {
      Mac mac = Mac.getInstance(HMAC_SHA256);
      SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
      mac.init(secretKeySpec);
      byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(hash);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      log.error("Failed to calculate HMAC signature", e);
      throw new RuntimeException("Failed to calculate HMAC signature", e);
    }
  }

  public boolean verifySignature(String payload, String signature, String secret) {
    String expectedSignature = calculateSignature(payload, secret);
    return expectedSignature.equals(signature);
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
