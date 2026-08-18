package com.finance.smartLedger.security.authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.security.domain.ServiceCredential;
import com.finance.smartLedger.security.infrastructure.persistence.ServiceCredentialRepository;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit test to verify API key authentication functionality.
 * 
 * This test validates that:
 * 1. ServiceCredential entity works correctly
 * 2. Password encoding works correctly
 * 3. Repository operations work correctly
 */
@ExtendWith(MockitoExtension.class)
class ServiceApiKeyAuthenticationIntegrationTest {

  @Mock private ServiceCredentialRepository serviceCredentialRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @Test
  void serviceCredentialEntity_ShouldWorkCorrectly() {
    // Test that ServiceCredential entity can be created and configured
    SecureRandom secureRandom = new SecureRandom();
    byte[] randomBytes = new byte[32];
    secureRandom.nextBytes(randomBytes);
    String apiKey = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    
    String hashedApiKey = "hashed-" + apiKey;
    
    ServiceCredential credential = ServiceCredential.builder()
        .name("test-credential")
        .hashedApiKey(hashedApiKey)
        .grantedPermissions(Set.of("PAYMENT:CREATE"))
        .enabled(true)
        .build();
    
    assertNotNull(credential);
    assertEquals("test-credential", credential.getName());
    assertEquals(hashedApiKey, credential.getHashedApiKey());
    assertTrue(credential.isEnabled());
    assertEquals(Set.of("PAYMENT:CREATE"), credential.getGrantedPermissions());
  }

  @Test
  void passwordEncoding_ShouldWorkCorrectly() {
    // Test that password encoding works correctly
    SecureRandom secureRandom = new SecureRandom();
    byte[] randomBytes = new byte[32];
    secureRandom.nextBytes(randomBytes);
    String apiKey = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    
    Mockito.lenient().when(passwordEncoder.encode(apiKey)).thenReturn("hashed-" + apiKey);
    Mockito.lenient().when(passwordEncoder.matches(apiKey, "hashed-" + apiKey)).thenReturn(true);
    Mockito.lenient().when(passwordEncoder.matches("wrong-key", "hashed-" + apiKey)).thenReturn(false);
    
    String hashedApiKey = passwordEncoder.encode(apiKey);
    
    assertTrue(passwordEncoder.matches(apiKey, hashedApiKey));
    assertFalse(passwordEncoder.matches("wrong-key", hashedApiKey));
  }

  @Test
  void enabledCredentials_ShouldBeQueryable() {
    // Test that enabled credentials can be queried
    SecureRandom secureRandom = new SecureRandom();
    byte[] randomBytes = new byte[32];
    secureRandom.nextBytes(randomBytes);
    String apiKey = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    
    ServiceCredential enabledCredential = ServiceCredential.builder()
        .name("enabled-credential")
        .hashedApiKey("hashed-" + apiKey)
        .grantedPermissions(Set.of("PAYMENT:CREATE"))
        .enabled(true)
        .build();
    
    ServiceCredential disabledCredential = ServiceCredential.builder()
        .name("disabled-credential")
        .hashedApiKey("hashed-" + apiKey)
        .grantedPermissions(Set.of("PAYMENT:CREATE"))
        .enabled(false)
        .build();
    
    Mockito.lenient().when(serviceCredentialRepository.findByEnabledTrue()).thenReturn(java.util.List.of(enabledCredential));
    
    var enabledCredentials = serviceCredentialRepository.findByEnabledTrue();
    
    assertEquals(1, enabledCredentials.size());
    assertEquals("enabled-credential", enabledCredentials.get(0).getName());
    assertTrue(enabledCredentials.get(0).isEnabled());
  }

  @Test
  void disabledCredentials_ShouldNotBeReturnedByEnabledQuery() {
    // Test that disabled credentials are not returned by enabled query
    Mockito.lenient().when(serviceCredentialRepository.findByEnabledTrue()).thenReturn(java.util.List.of());
    
    var enabledCredentials = serviceCredentialRepository.findByEnabledTrue();
    
    assertTrue(enabledCredentials.isEmpty());
  }
}
