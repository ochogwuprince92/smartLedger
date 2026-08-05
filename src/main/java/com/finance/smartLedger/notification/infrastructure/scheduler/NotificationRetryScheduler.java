package com.finance.smartLedger.notification.infrastructure.scheduler;

import com.finance.smartLedger.shared.util.ClockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduled.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationRetryScheduler {

  private static final Logger log = LoggerFactory.getLogger(NotificationRetryScheduler.class);

  private final ClockProvider clockProvider;

  @Value("${app.scheduled.notification-retry:0 */5 * * * ?}")
  private String notificationRetryCron;

  public NotificationRetryScheduler(ClockProvider clockProvider) {
    this.clockProvider = clockProvider;
  }

  @Scheduled(cron = "${app.scheduled.notification-retry:0 */5 * * * ?}")
  public void retryFailedNotifications() {
    log.info("Starting notification retry at: {}", clockProvider.now());
    // Notification retry logic will be implemented in Phase 8
    log.info("Notification retry completed at: {}", clockProvider.now());
  }
}
