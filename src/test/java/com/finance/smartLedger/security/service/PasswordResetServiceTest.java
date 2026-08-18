package com.finance.smartLedger.security.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.infrastructure.persistence.PasswordResetTokenRepository;
import com.finance.smartLedger.security.infrastructure.persistence.UserRepository;
import com.finance.smartLedger.shared.exception.BusinessException;
import com.finance.smartLedger.shared.exception.ErrorCodes;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordResetTokenRepository tokenRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private PasswordResetService passwordResetService;

  private User testUser;
  private String testEmail = "test@example.com";
  private String testPassword = "oldPassword123";

  @BeforeEach
  void setUp() {
    testUser = new User("testuser", testEmail, testPassword);
    testUser.setEnabled(true);
    testUser.setId(UUID.randomUUID());
  }

  @Test
  void forgotPassword_withKnownEmail_returnsGenericSuccessResponse_andCreatesToken() {
    when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode(anyString())).thenReturn("hashed-token");

    passwordResetService.initiatePasswordReset(testEmail);

    verify(userRepository).findByEmail(testEmail);
    verify(passwordEncoder).encode(anyString());
    verify(tokenRepository).save(any());
  }

  @Test
  void forgotPassword_withUnknownEmail_returnsNull_butDoesNotReveal() {
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    passwordResetService.initiatePasswordReset("unknown@example.com");

    verify(userRepository).findByEmail("unknown@example.com");
    verify(tokenRepository, never()).save(any());
  }

  @Test
  void resetPassword_withValidToken_succeeds() {
    String token = "valid-token-12345";
    String tokenHash = "hashed-token";
    
    com.finance.smartLedger.security.domain.PasswordResetToken resetToken =
        new com.finance.smartLedger.security.domain.PasswordResetToken(
            tokenHash, testUser, LocalDateTime.now().plusHours(1));
    
    when(passwordEncoder.matches(token, tokenHash)).thenReturn(true);
    when(tokenRepository.findAll()).thenReturn(java.util.List.of(resetToken));
    when(passwordEncoder.encode(anyString())).thenReturn("new-hashed-password");

    passwordResetService.resetPassword(token, "newPassword123");

    verify(passwordEncoder).matches(token, tokenHash);
    verify(passwordEncoder).encode("newPassword123");
    verify(userRepository).save(testUser);
    assertTrue(resetToken.getUsed());
  }

  @Test
  void resetPassword_withExpiredToken_throwsException() {
    String token = "expired-token-12345";
    String tokenHash = "hashed-token";
    
    com.finance.smartLedger.security.domain.PasswordResetToken expiredToken =
        new com.finance.smartLedger.security.domain.PasswordResetToken(
            tokenHash, testUser, LocalDateTime.now().minusHours(1));
    
    when(passwordEncoder.matches(token, tokenHash)).thenReturn(true);
    when(tokenRepository.findAll()).thenReturn(java.util.List.of(expiredToken));

    BusinessException exception = assertThrows(
        BusinessException.class, () -> passwordResetService.resetPassword(token, "newPassword123"));

    assertEquals(ErrorCodes.PASSWORD_RESET_TOKEN_EXPIRED, exception.getErrorCodes());
  }

  @Test
  void resetPassword_withAlreadyUsedToken_throwsException() {
    String token = "used-token-12345";
    String tokenHash = "hashed-token";
    
    com.finance.smartLedger.security.domain.PasswordResetToken usedToken =
        new com.finance.smartLedger.security.domain.PasswordResetToken(
            tokenHash, testUser, LocalDateTime.now().plusHours(1));
    usedToken.setUsed(true);
    
    when(passwordEncoder.matches(token, tokenHash)).thenReturn(true);
    when(tokenRepository.findAll()).thenReturn(java.util.List.of(usedToken));

    BusinessException exception = assertThrows(
        BusinessException.class, () -> passwordResetService.resetPassword(token, "newPassword123"));

    assertEquals(ErrorCodes.PASSWORD_RESET_TOKEN_USED, exception.getErrorCodes());
  }

  @Test
  void resetPassword_withInvalidToken_throwsException() {
    String token = "invalid-token-12345";
    
    when(tokenRepository.findAll()).thenReturn(java.util.List.of());

    BusinessException exception = assertThrows(
        BusinessException.class, () -> passwordResetService.resetPassword(token, "newPassword123"));

    assertEquals(ErrorCodes.INVALID_PASSWORD_RESET_TOKEN, exception.getErrorCodes());
  }
}