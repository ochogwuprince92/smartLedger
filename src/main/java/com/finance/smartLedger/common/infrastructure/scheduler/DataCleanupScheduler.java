package com.finance.smartLedger.common.infrastructure.scheduler;

import com.finance.smartLedger.shared.util.ClockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduled.enabled", havingValue = "true", matchIfMissing = true)
public class DataCleanupScheduler {

  private static final Logger log = LoggerFactory.getLogger(DataCleanupScheduler.class);

  private final ClockProvider clockProvider;

  @Value("${app.scheduled.data-cleanup:0 0 3 * * ?}")
  private String dataCleanupCron;

  public DataCleanupScheduler(ClockProvider clockProvider) {
    this.clockProvider = clockProvider;
  }

  @Scheduled(cron = "${app.scheduled.data-cleanup:0 0 3 * * ?}")
  public void performDataCleanup() {
    log.info("Starting data cleanup at: {}", clockProvider.now());
    // Data cleanup logic will be implemented in Phase 7
    log.info("Data cleanup completed at: {}", clockProvider.now());
  }
}
