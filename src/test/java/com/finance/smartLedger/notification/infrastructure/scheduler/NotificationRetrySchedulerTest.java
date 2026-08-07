package com.finance.smartLedger.notification.infrastructure.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.notification.application.NotificationService;
import com.finance.smartLedger.notification.domain.Notification;
import com.finance.smartLedger.notification.domain.NotificationChannel;
import com.finance.smartLedger.notification.domain.NotificationStatus;
import com.finance.smartLedger.notification.domain.NotificationType;
import com.finance.smartLedger.shared.util.ClockProvider;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationRetrySchedulerTest {

  @Mock private ClockProvider clockProvider;

  @Mock private NotificationService notificationService;

  @InjectMocks private NotificationRetryScheduler scheduler;

  private static final int MAX_RETRY_ATTEMPTS = 3;
  private static final long BACKOFF_SECONDS = 300;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(scheduler, "maxRetryAttempts", MAX_RETRY_ATTEMPTS);
    ReflectionTestUtils.setField(scheduler, "backoffSeconds", BACKOFF_SECONDS);
  }

  @Test
  void testRetryFailedNotifications_Success() {
    when(clockProvider.now()).thenReturn(LocalDateTime.now());

    List<Notification> failedNotifications = createFailedNotifications(2);
    when(notificationService.findByStatus(NotificationStatus.FAILED))
        .thenReturn(failedNotifications);

    scheduler.retryFailedNotifications();

    verify(notificationService).findByStatus(NotificationStatus.FAILED);
    verify(notificationService, times(2)).retryNotification(any(UUID.class));
  }

  @Test
  void testRetryFailedNotifications_MaxAttemptsReached() {
    when(clockProvider.now()).thenReturn(LocalDateTime.now());

    List<Notification> failedNotifications = new ArrayList<>();
    Notification notification = createFailedNotification();
    notification.setRetryCount(MAX_RETRY_ATTEMPTS);
    failedNotifications.add(notification);

    when(notificationService.findByStatus(NotificationStatus.FAILED))
        .thenReturn(failedNotifications);

    scheduler.retryFailedNotifications();

    verify(notificationService).findByStatus(NotificationStatus.FAILED);
    verify(notificationService, never()).retryNotification(any(UUID.class));
  }

  @Test
  void testRetryFailedNotifications_BackoffNotReached() {
    when(clockProvider.now()).thenReturn(LocalDateTime.now());

    List<Notification> failedNotifications = new ArrayList<>();
    Notification notification = createFailedNotification();
    notification.setRetryCount(1);
    notification.setLastRetryAt(LocalDateTime.now().minusSeconds(100)); // Less than backoff
    failedNotifications.add(notification);

    when(notificationService.findByStatus(NotificationStatus.FAILED))
        .thenReturn(failedNotifications);

    scheduler.retryFailedNotifications();

    verify(notificationService).findByStatus(NotificationStatus.FAILED);
    verify(notificationService, never()).retryNotification(any(UUID.class));
  }

  @Test
  void testRetryFailedNotifications_BackoffReached() {
    when(clockProvider.now()).thenReturn(LocalDateTime.now());

    List<Notification> failedNotifications = new ArrayList<>();
    Notification notification = createFailedNotification();
    notification.setRetryCount(1);
    notification.setLastRetryAt(
        LocalDateTime.now().minusSeconds(BACKOFF_SECONDS + 1)); // More than backoff
    failedNotifications.add(notification);

    when(notificationService.findByStatus(NotificationStatus.FAILED))
        .thenReturn(failedNotifications);

    scheduler.retryFailedNotifications();

    verify(notificationService).findByStatus(NotificationStatus.FAILED);
    verify(notificationService, times(1)).retryNotification(any(UUID.class));
  }

  @Test
  void testRetryFailedNotifications_EmptyList() {
    when(clockProvider.now()).thenReturn(LocalDateTime.now());
    when(notificationService.findByStatus(NotificationStatus.FAILED)).thenReturn(new ArrayList<>());

    scheduler.retryFailedNotifications();

    verify(notificationService).findByStatus(NotificationStatus.FAILED);
    verify(notificationService, never()).retryNotification(any(UUID.class));
  }

  @Test
  void testRetryFailedNotifications_ServiceException() {
    when(clockProvider.now()).thenReturn(LocalDateTime.now());

    List<Notification> failedNotifications = createFailedNotifications(1);
    when(notificationService.findByStatus(NotificationStatus.FAILED))
        .thenReturn(failedNotifications);
    doThrow(new RuntimeException("Service error"))
        .when(notificationService)
        .retryNotification(any(UUID.class));

    scheduler.retryFailedNotifications();

    verify(notificationService).findByStatus(NotificationStatus.FAILED);
    verify(notificationService).retryNotification(any(UUID.class));
  }

  private List<Notification> createFailedNotifications(int count) {
    List<Notification> notifications = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      notifications.add(createFailedNotification());
    }
    return notifications;
  }

  private Notification createFailedNotification() {
    return Notification.builder()
        .id(UUID.randomUUID())
        .recipientEmail("test@example.com")
        .recipientPhone(null)
        .notificationType(NotificationType.PAYMENT_COMPLETED)
        .channel(NotificationChannel.EMAIL)
        .status(NotificationStatus.FAILED)
        .subject("Test Subject")
        .message("Test Message")
        .relatedEntityType("Payment")
        .relatedEntityId(UUID.randomUUID())
        .retryCount(0)
        .maxRetries(MAX_RETRY_ATTEMPTS)
        .lastRetryAt(null)
        .build();
  }
}
