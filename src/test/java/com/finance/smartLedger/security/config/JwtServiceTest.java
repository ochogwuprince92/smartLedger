package com.finance.smartLedger.security.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JwtService Tests")
class JwtServiceTest {

  private JwtService jwtService;
  private static final String SECRET =
      "Y29uZmlkZW50aWFsLXNlY3JldC1rZXktZm9yLWp3dC10b2tlbi1nZW5lcmF0aW9uLXNob3VsZC1iZS1hdC1sZWFzdC0yNTYtYml0cy1sb25nLWZvci1zZWN1cml0eQ==";
  private static final long EXPIRATION = 86400000; // 24 hours

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);
  }

  @Test
  @DisplayName("Should generate token with username and userId")
  void shouldGenerateTokenWithUsernameAndUserId() {
    String username = "john.doe";
    String userId = "user-123";

    String token = jwtService.generateToken(username, userId);

    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  @DisplayName("Should generate token with extra claims")
  void shouldGenerateTokenWithExtraClaims() {
    String username = "john.doe";
    String userId = "user-123";
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("role", "ADMIN");

    String token = jwtService.generateToken(extraClaims, username, userId);

    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  @DisplayName("Should extract username from token")
  void shouldExtractUsernameFromToken() {
    String username = "john.doe";
    String userId = "user-123";
    String token = jwtService.generateToken(username, userId);

    String extractedUsername = jwtService.extractUsername(token);

    assertEquals(username, extractedUsername);
  }

  @Test
  @DisplayName("Should extract userId from token")
  void shouldExtractUserIdFromToken() {
    String username = "john.doe";
    String userId = "user-123";
    String token = jwtService.generateToken(username, userId);

    String extractedUserId = jwtService.extractUserId(token);

    assertEquals(userId, extractedUserId);
  }

  @Test
  @DisplayName("Should validate token with correct username")
  void shouldValidateTokenWithCorrectUsername() {
    String username = "john.doe";
    String userId = "user-123";
    String token = jwtService.generateToken(username, userId);

    boolean isValid = jwtService.isTokenValid(token, username);

    assertTrue(isValid);
  }

  @Test
  @DisplayName("Should invalidate token with wrong username")
  void shouldInvalidateTokenWithWrongUsername() {
    String username = "john.doe";
    String userId = "user-123";
    String token = jwtService.generateToken(username, userId);

    boolean isValid = jwtService.isTokenValid(token, "wrong.username");

    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should invalidate expired token")
  void shouldInvalidateExpiredToken() {
    String username = "john.doe";
    String userId = "user-123";

    ReflectionTestUtils.setField(jwtService, "jwtExpiration", -86400000); // Expired 24 hours ago
    String token = jwtService.generateToken(username, userId);
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION); // Reset

    // The token should be invalid due to expiration
    assertThrows(Exception.class, () -> jwtService.isTokenValid(token, username));
  }

  @Test
  @DisplayName("Should extract expiration date from token")
  void shouldExtractExpirationDateFromToken() {
    String username = "john.doe";
    String userId = "user-123";
    String token = jwtService.generateToken(username, userId);

    Date expiration = jwtService.extractClaim(token, claims -> claims.getExpiration());

    assertNotNull(expiration);
    assertTrue(expiration.after(new Date()));
  }

  @Test
  @DisplayName("Should extract custom claim from token")
  void shouldExtractCustomClaimFromToken() {
    String username = "john.doe";
    String userId = "user-123";
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("customKey", "customValue");
    String token = jwtService.generateToken(extraClaims, username, userId);

    String customValue =
        jwtService.extractClaim(token, claims -> claims.get("customKey", String.class));

    assertEquals("customValue", customValue);
  }

  @Test
  @DisplayName("Should handle null token gracefully")
  void shouldHandleNullTokenGracefully() {
    assertThrows(Exception.class, () -> jwtService.extractUsername(null));
  }

  @Test
  @DisplayName("Should handle invalid token gracefully")
  void shouldHandleInvalidTokenGracefully() {
    String invalidToken = "invalid.token.string";

    assertThrows(Exception.class, () -> jwtService.extractUsername(invalidToken));
  }

  @Test
  @DisplayName("Should generate different tokens for different users")
  void shouldGenerateDifferentTokensForDifferentUsers() {
    String token1 = jwtService.generateToken("user1", "id1");
    String token2 = jwtService.generateToken("user2", "id2");

    assertNotEquals(token1, token2);
  }

  @Test
  @DisplayName("Should generate different tokens for same user at different times")
  void shouldGenerateDifferentTokensForSameUserAtDifferentTimes() {
    String username = "john.doe";
    String userId = "user-123";

    String token1 = jwtService.generateToken(username, userId);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    String token2 = jwtService.generateToken(username, userId);

    assertNotEquals(token1, token2);
  }
}
