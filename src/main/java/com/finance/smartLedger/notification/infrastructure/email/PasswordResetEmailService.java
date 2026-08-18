package com.finance.smartLedger.notification.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "true", matchIfMissing = false)
public class PasswordResetEmailService {

  private final EmailService emailService;

  @Value("${app.base-url:http://localhost:8081}")
  private String baseUrl;

  public void sendPasswordResetEmail(String to, String token) {
    String resetLink = baseUrl + "/reset-password?token=" + token;
    String subject = "Password Reset Request";
    String htmlContent =
        """
        <html>
        <body>
            <h2>Password Reset Request</h2>
            <p>You have requested to reset your password. Click the link below to reset it:</p>
            <p><a href="%s">Reset Password</a></p>
            <p>This link will expire in 1 hour.</p>
            <p>If you did not request this, please ignore this email.</p>
        </body>
        </html>
        """
            .formatted(resetLink);

    emailService.sendHtmlEmail(to, subject, htmlContent);
    log.info("Password reset email sent to: {}", to);
  }
}
