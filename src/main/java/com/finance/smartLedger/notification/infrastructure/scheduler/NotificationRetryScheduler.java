package com.finance.smartLedger.notification.infrastructure.scheduler;

import com.finance.smartLedger.notification.application.NotificationService;
import com.finance.smartLedger.notification.domain.Notification;
import com.finance.smartLedger.notification.domain.NotificationStatus;
import com.finance.smartLedger.shared.util.ClockProvider;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduled.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class NotificationRetryScheduler {

  private static final Logger log = LoggerFactory.getLogger(NotificationRetryScheduler.class);

  private final ClockProvider clockProvider;
  private final NotificationService notificationService;

  @Value("${app.scheduled.notification-retry:0 */5 * * * ?}")
  private String notificationRetryCron;

  @Value("${app.scheduled.notification-retry.max-attempts:3}")
  private int maxRetryAttempts;

  @Value("${app.scheduled.notification-retry.backoff-seconds:300}")
  private long backoffSeconds;

  @Scheduled(cron = "${app.scheduled.notification-retry:0 */5 * * * ?}")
  public void retryFailedNotifications() {
    log.info("Starting notification retry at: {}", clockProvider.now());
    
    try {
      List<Notification> failedNotifications = notificationService.findByStatus(NotificationStatus.FAILED);
      
      for (Notification notification : failedNotifications) {
        if (shouldRetryNotification(notification)) {
          try {
            notificationService.retryNotification(notification.getId());
            log.info("Retried notification: {}", notification.getId());
          } catch (Exception e) {
            log.error("Failed to retry notification: {}", notification.getId(), e);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error during notification retry", e);
    }
    
    log.info("Notification retry completed at: {}", clockProvider.now());
  }

  private boolean shouldRetryNotification(Notification notification) {
    if (notification.getRetryCount() >= maxRetryAttempts) {
      log.debug("Notification {} reached max retry attempts", notification.getId());
      return false;
    }

    if (notification.getLastRetryAt() != null) {
      long secondsSinceLastRetry = 
          ChronoUnit.SECONDS.between(notification.getLastRetryAt(), clockProvider.now());
      if (secondsSinceLastRetry < backoffSeconds) {
        log.debug("Notification {} backoff period not reached", notification.getId());
        return false;
      }
    }

    return true;
  }
}
