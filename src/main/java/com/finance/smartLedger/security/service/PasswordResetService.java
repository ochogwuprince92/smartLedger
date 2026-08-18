package com.finance.smartLedger.security.service;

import com.finance.smartLedger.notification.infrastructure.email.PasswordResetEmailService;
import com.finance.smartLedger.security.domain.PasswordResetToken;
import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.infrastructure.persistence.PasswordResetTokenRepository;
import com.finance.smartLedger.security.infrastructure.persistence.UserRepository;
import com.finance.smartLedger.shared.exception.BusinessException;
import com.finance.smartLedger.shared.exception.ErrorCodes;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired(required = false)
  private PasswordResetEmailService emailService;

  private static final int TOKEN_EXPIRATION_HOURS = 1;
  private static final int TOKEN_LENGTH = 32;

  @Transactional
  public void initiatePasswordReset(String email) {
    // Always process this, but only send email if user exists
    // This prevents account enumeration
    Optional<User> userOpt = userRepository.findByEmail(email);

    if (userOpt.isPresent()) {
      User user = userOpt.get();
      String token = generateSecureToken();
      String tokenHash = passwordEncoder.encode(token);

      PasswordResetToken resetToken =
          new PasswordResetToken(tokenHash, user, LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS));
      tokenRepository.save(resetToken);

      // Send email with the raw token
      if (emailService != null) {
        try {
          emailService.sendPasswordResetEmail(email, token);
        } catch (Exception e) {
          log.error("Failed to send password reset email: {}", e.getMessage());
          log.warn("Password reset token for development: {}", token);
        }
      } else {
        log.warn("No email service available for password reset");
        log.warn("Password reset token for development: {}", token);
      }

      log.info("Password reset initiated for user: {}", email);
    } else {
      // User doesn't exist, but we don't reveal this
      log.info("Password reset requested for non-existent email: {}", email);
    }
  }

  @Transactional
  public void resetPassword(String token, String newPassword) {
    // Find token by hash (need to hash the input token first)
    // Since we can't reverse the hash, we need to find by iterating through tokens
    // In production, you'd want a more efficient approach, but for now this works
    Optional<PasswordResetToken> tokenOpt = findToken(token);

    if (tokenOpt.isEmpty()) {
      throw new BusinessException(ErrorCodes.INVALID_PASSWORD_RESET_TOKEN);
    }

    PasswordResetToken resetToken = tokenOpt.get();

    if (resetToken.getUsed()) {
      throw new BusinessException(ErrorCodes.PASSWORD_RESET_TOKEN_USED);
    }

    if (resetToken.isExpired()) {
      throw new BusinessException(ErrorCodes.PASSWORD_RESET_TOKEN_EXPIRED);
    }

    // Update user password
    User user = resetToken.getUser();
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    // Mark token as used
    resetToken.setUsed(true);
    tokenRepository.save(resetToken);

    log.info("Password reset successfully for user: {}", user.getEmail());
  }

  private Optional<PasswordResetToken> findToken(String rawToken) {
    // Find all tokens and check which one matches
    // This is not the most efficient approach but works for the scope
    return tokenRepository.findAll().stream()
        .filter(token -> passwordEncoder.matches(rawToken, token.getTokenHash()))
        .findFirst();
  }

  private String generateSecureToken() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] tokenBytes = new byte[TOKEN_LENGTH];
    secureRandom.nextBytes(tokenBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
  }
}
