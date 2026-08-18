package com.finance.smartLedger.notification.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.notification.domain.Notification;
import com.finance.smartLedger.notification.domain.NotificationChannel;
import com.finance.smartLedger.notification.domain.NotificationStatus;
import com.finance.smartLedger.notification.domain.NotificationType;
import com.finance.smartLedger.notification.infrastructure.email.EmailService;
import com.finance.smartLedger.notification.infrastructure.persistence.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;

  @Mock private com.finance.smartLedger.audit.application.AuditService auditService;

  @Mock private EmailService emailService;

  @InjectMocks
  private NotificationService notificationService;

  private UUID notificationId;
  private Notification emailNotification;

  @BeforeEach
  void setUp() {
    notificationId = UUID.randomUUID();
    emailNotification =
        new Notification(
            "test@example.com",
            null,
            NotificationType.PAYMENT_COMPLETED,
            NotificationChannel.EMAIL,
            "Test Subject",
            "Test Message",
            "Payment",
            UUID.randomUUID(),
            "system");
    emailNotification.setId(notificationId);
    emailNotification.setScheduledAt(LocalDateTime.now().minusMinutes(1));

    // Inject Optional<EmailService> with the mock
    java.util.Optional<EmailService> optionalEmailService = java.util.Optional.of(emailService);
    org.springframework.test.util.ReflectionTestUtils.setField(
        notificationService, "emailService", optionalEmailService);
  }

  @Test
  void sendNotification_EmailChannel_ShouldInvokeEmailServiceAndMarkSent() {
    // Given
    when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(emailNotification));
    when(notificationRepository.save(any(Notification.class))).thenReturn(emailNotification);
    doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

    // When
    Notification result = notificationService.sendNotification(notificationId);

    // Then
    assertNotNull(result);
    assertEquals(NotificationStatus.SENT, result.getStatus());
    verify(emailService).sendEmail("test@example.com", "Test Subject", "Test Message");
    verify(notificationRepository).save(any(Notification.class));
    verify(auditService).logStatusChange(
        eq("Notification"), eq(notificationId), any(), eq("PENDING"), eq("SENT"), eq("system"));
  }

  @Test
  void processScheduledNotifications_ShouldActuallySendNotJustMarkAsSent() {
    // Given
    when(notificationRepository.findScheduledNotifications(
            any(LocalDateTime.class), eq(NotificationStatus.PENDING)))
        .thenReturn(List.of(emailNotification));
    when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(emailNotification));
    when(notificationRepository.save(any(Notification.class))).thenReturn(emailNotification);
    doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

    // When
    List<Notification> result = notificationService.processScheduledNotifications();

    // Then
    assertEquals(1, result.size());
    // Verify emailService was actually called
    verify(emailService, atLeastOnce()).sendEmail(anyString(), anyString(), anyString());
  }

  @Test
  void retryNotification_ShouldAttemptActualResendNotJustMarkForRetry() {
    // Given
    emailNotification.markAsFailed("Previous failure");
    when(notificationRepository.findById(notificationId))
        .thenReturn(Optional.of(emailNotification), Optional.of(emailNotification));
    when(notificationRepository.save(any(Notification.class))).thenReturn(emailNotification);
    doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

    // When
    Notification result = notificationService.retryNotification(notificationId);

    // Then
    assertNotNull(result);
    // Verify emailService was actually called during retry
    verify(emailService, atLeastOnce()).sendEmail(anyString(), anyString(), anyString());
  }

  @Test
  void retryNotification_OnResendFailure_ShouldMarkFailedOrPositionForRetry() {
    // Given
    emailNotification.markAsFailed("Previous failure");
    when(notificationRepository.findById(notificationId))
        .thenReturn(Optional.of(emailNotification), Optional.of(emailNotification));
    when(notificationRepository.save(any(Notification.class))).thenReturn(emailNotification);
    doThrow(new RuntimeException("Email service failed"))
        .when(emailService)
        .sendEmail(anyString(), anyString(), anyString());

    // When
    Notification result = notificationService.retryNotification(notificationId);

    // Then
    assertNotNull(result);
    // Should either be FAILED or RETRYING (not falsely SENT)
    assertTrue(
        result.getStatus() == NotificationStatus.FAILED
            || result.getStatus() == NotificationStatus.RETRYING);
  }
}
