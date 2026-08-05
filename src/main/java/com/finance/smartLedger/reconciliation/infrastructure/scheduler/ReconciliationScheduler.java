package com.finance.smartLedger.reconciliation.infrastructure.scheduler;

import com.finance.smartLedger.shared.util.ClockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduled.enabled", havingValue = "true", matchIfMissing = true)
public class ReconciliationScheduler {

  private static final Logger log = LoggerFactory.getLogger(ReconciliationScheduler.class);

  private final ClockProvider clockProvider;

  @Value("${app.scheduled.reconciliation:0 0 2 * * ?}")
  private String reconciliationCron;

  public ReconciliationScheduler(ClockProvider clockProvider) {
    this.clockProvider = clockProvider;
  }

  @Scheduled(cron = "${app.scheduled.reconciliation:0 0 2 * * ?}")
  public void performDailyReconciliation() {
    log.info("Starting daily reconciliation at: {}", clockProvider.now());
    // Reconciliation logic will be implemented in Phase 8
    log.info("Daily reconciliation completed at: {}", clockProvider.now());
  }
}
