package com.finance.smartLedger.notification.infrastructure.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;

  @Mock private MimeMessage mimeMessage;

  @InjectMocks private EmailService emailService;

  private static final String FROM_EMAIL = "noreply@smartledger.com";
  private static final String FROM_NAME = "SmartLedger";

  @BeforeEach
  void setUp() {
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    ReflectionTestUtils.setField(emailService, "fromEmail", FROM_EMAIL);
    ReflectionTestUtils.setField(emailService, "fromName", FROM_NAME);
  }

  @Test
  void testSendEmail_Success() {
    String to = "test@example.com";
    String subject = "Test Subject";
    String content = "Test Content";

    emailService.sendEmail(to, subject, content);

    verify(mailSender).createMimeMessage();
    verify(mailSender).send(any(MimeMessage.class));
  }

  @Test
  void testSendEmail_WithHtml() {
    String to = "test@example.com";
    String subject = "Test Subject";
    String htmlContent = "<html><body>Test HTML Content</body></html>";

    emailService.sendHtmlEmail(to, subject, htmlContent);

    verify(mailSender).createMimeMessage();
    verify(mailSender).send(any(MimeMessage.class));
  }

  @Test
  void testSendEmail_Failure_ThrowsException() {
    String to = "test@example.com";
    String subject = "Test Subject";
    String content = "Test Content";

    doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> emailService.sendEmail(to, subject, content));

    assertEquals("Failed to send email", exception.getMessage());
    verify(mailSender).send(any(MimeMessage.class));
  }

  @Test
  void testSendEmail_VerifyParameters() {
    String to = "test@example.com";
    String subject = "Test Subject";
    String content = "Test Content";

    emailService.sendEmail(to, subject, content);

    verify(mailSender).send(any(MimeMessage.class));
  }
}
