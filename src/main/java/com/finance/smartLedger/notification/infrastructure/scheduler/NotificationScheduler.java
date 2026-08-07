package com.finance.smartLedger.notification.infrastructure.scheduler;

import com.finance.smartLedger.notification.application.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

  private final NotificationService notificationService;

  @Scheduled(fixedRate = 300000) // Run every 5 minutes
  public void processRetryableNotifications() {
    try {
      log.info("Processing retryable notifications");
      var notifications = notificationService.processRetryableNotifications();
      log.info("Processed {} retryable notifications", notifications.size());
    } catch (Exception e) {
      log.error("Error processing retryable notifications", e);
    }
  }

  @Scheduled(fixedRate = 60000) // Run every 1 minute
  public void processScheduledNotifications() {
    try {
      log.info("Processing scheduled notifications");
      var notifications = notificationService.processScheduledNotifications();
      log.info("Processed {} scheduled notifications", notifications.size());
    } catch (Exception e) {
      log.error("Error processing scheduled notifications", e);
    }
  }
}
