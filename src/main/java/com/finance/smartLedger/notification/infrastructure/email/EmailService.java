package com.finance.smartLedger.notification.infrastructure.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "true", matchIfMissing = false)
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${app.email.from:noreply@smartledger.com}")
  private String fromEmail;

  @Value("${app.email.from-name:SmartLedger}")
  private String fromName;

  public void sendEmail(String to, String subject, String content, boolean isHtml) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      helper.setFrom(fromEmail, fromName);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(content, isHtml);

      mailSender.send(mimeMessage);
      log.info("Email sent successfully to: {} with subject: {}", to, subject);

    } catch (Exception e) {
      log.error("Failed to send email to: {} with subject: {}", to, subject, e);
      throw new RuntimeException("Failed to send email", e);
    }
  }

  public void sendEmail(String to, String subject, String content) {
    sendEmail(to, subject, content, false);
  }

  public void sendHtmlEmail(String to, String subject, String htmlContent) {
    sendEmail(to, subject, htmlContent, true);
  }
}
