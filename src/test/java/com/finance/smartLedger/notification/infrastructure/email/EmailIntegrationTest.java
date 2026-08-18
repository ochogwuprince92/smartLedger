package com.finance.smartLedger.notification.infrastructure.email;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test to verify Gmail SMTP configuration actually works.
 * 
 * This test validates that the Gmail SMTP settings in application-local.yml are correct
 * and that a real email can be sent without authentication failures.
 * 
 * NOTE: This test requires:
 * 1. application-local.yml to exist with valid Gmail SMTP configuration
 * 2. MAIL_USERNAME and MAIL_PASSWORD environment variables to be set
 * 3. MAIL_PASSWORD must be a Gmail App Password (NOT regular password)
 * 
 * To run this test: mvn test -Dtest=EmailIntegrationTest -Dspring-boot.run.profiles=local
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("local")
@TestPropertySource(properties = {
  "spring.jpa.hibernate.ddl-auto=none",
  "spring.jpa.hibernate.hbm2ddl.auto=none"
})
@EnabledIf("com.finance.smartLedger.notification.infrastructure.email.EmailIntegrationTest#isEmailConfigured")
class EmailIntegrationTest {

  @Autowired(required = false)
  private JavaMailSender javaMailSender;

  @Autowired(required = false)
  private EmailService emailService;

  @Value("${spring.mail.username:}")
  private String mailUsername;

  @Value("${spring.mail.password:}")
  private String mailPassword;

  @Value("${spring.mail.host:}")
  private String mailHost;

  @Value("${app.email.enabled:false}")
  private boolean emailEnabled;

  /**
   * Check if email configuration is properly set up for this test.
   * The test is only enabled if email is configured and enabled.
   */
  static boolean isEmailConfigured() {
    String mailUsername = System.getenv("MAIL_USERNAME");
    String mailPassword = System.getenv("MAIL_PASSWORD");
    return mailUsername != null && !mailUsername.isEmpty() 
        && mailPassword != null && !mailPassword.isEmpty();
  }

  @Test
  void testJavaMailSenderBeanExists() {
    // Verify that JavaMailSender bean is created when email is enabled
    assertNotNull(javaMailSender, 
        "JavaMailSender should be available when app.email.enabled=true");
  }

  @Test
  void testEmailServiceBeanExists() {
    // Verify that EmailService bean is created when email is enabled
    assertNotNull(emailService, 
        "EmailService should be available when app.email.enabled=true");
  }

  @Test
  void testGmailSmtpConfigurationIsValid() {
    // Verify Gmail SMTP settings match what JavaMailSender needs
    assertTrue(emailEnabled, "app.email.enabled should be true for email integration test");
    assertEquals("smtp.gmail.com", mailHost, "SMTP host should be smtp.gmail.com");
    assertNotNull(mailUsername, "spring.mail.username should be configured");
    assertNotNull(mailPassword, "spring.mail.password should be configured");
    
    // Verify password is 16 characters (Gmail App Password format)
    // App passwords are exactly 16 characters (including spaces in display, but without spaces in config)
    assertTrue(mailPassword.length() == 16, 
        "Gmail App Password should be 16 characters. Current length: " + mailPassword.length() + 
        ". If using regular password, it will fail authentication. Generate App Password at: " +
        "https://myaccount.google.com/apppasswords");
  }

  @Test
  void testSendRealEmail_VerifiesConfiguration() {
    // This test actually sends a real email to verify the configuration works end-to-end
    // It will fail if:
    // 1. The Gmail credentials are wrong
    // 2. A regular password is used instead of App Password
    // 3. Network/firewall issues prevent SMTP connection
    
    String testRecipient = mailUsername; // Send to the same email account
    String testSubject = "SmartLedger Email Configuration Test";
    String testContent = """
        <html>
        <body>
          <h2>Email Configuration Test</h2>
          <p>This is a test email from SmartLedger to verify Gmail SMTP configuration is working correctly.</p>
          <p>If you received this email, your Gmail SMTP configuration is valid!</p>
          <p><strong>Configuration verified:</strong></p>
          <ul>
            <li>SMTP Host: smtp.gmail.com</li>
            <li>SMTP Port: 587</li>
            <li>SMTP Auth: enabled</li>
            <li>STARTTLS: enabled</li>
            <li>From: %s</li>
          </ul>
          <p><em>Time sent: %s</em></p>
        </body>
        </html>
        """.formatted(mailUsername, java.time.LocalDateTime.now());

    try {
      emailService.sendHtmlEmail(testRecipient, testSubject, testContent);
      // If we get here, the email was sent successfully
      assertTrue(true, "Email sent successfully. Check inbox for confirmation.");
      
    } catch (MailSendException e) {
      // This specifically catches SMTP authentication failures
      fail("Email send failed with MailSendException. This usually means: " +
           "1. Using regular Gmail password instead of App Password, " +
           "2. App Password is incorrect, " +
           "3. 2FA is not enabled on the Google account. " +
           "Error: " + e.getMessage());
    } catch (Exception e) {
      fail("Email send failed with unexpected error: " + e.getClass().getName() + 
           " - " + e.getMessage());
    }
  }

  @Test
  void testEmailServiceErrorHandling_LogsErrors() {
    // Verify that EmailService properly logs errors and doesn't silently swallow exceptions
    // The current implementation throws RuntimeException, which is correct for visibility
    
    // Try to send to an invalid email to trigger a send error
    String invalidRecipient = "invalid-email-that-will-fail@.example.com";
    String testSubject = "Test Error Handling";
    String testContent = "This should fail";

    assertThrows(RuntimeException.class, () -> {
      emailService.sendEmail(invalidRecipient, testSubject, testContent);
    }, "EmailService should throw RuntimeException on send failure for error visibility");
  }
}
