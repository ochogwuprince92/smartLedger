package com.finance.smartLedger.reconciliation.infrastructure.scheduler;

import com.finance.smartLedger.reconciliation.application.ReconciliationService;
import com.finance.smartLedger.reconciliation.domain.ReconciliationStatus;
import com.finance.smartLedger.shared.util.ClockProvider;
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
public class ReconciliationScheduler {

  private static final Logger log = LoggerFactory.getLogger(ReconciliationScheduler.class);

  private final ClockProvider clockProvider;
  private final ReconciliationService reconciliationService;

  @Value("${app.scheduled.reconciliation:0 0 2 * * ?}")
  private String reconciliationCron;

  @Scheduled(cron = "${app.scheduled.reconciliation:0 0 2 * * ?}")
  public void performDailyReconciliation() {
    log.info("Starting daily reconciliation at: {}", clockProvider.now());
    try {
      List<com.finance.smartLedger.reconciliation.domain.Reconciliation> pendingReconciliations = 
          reconciliationService.findByStatus(ReconciliationStatus.PENDING);
      
      int foundCount = pendingReconciliations.size();
      int successCount = 0;
      int failureCount = 0;

      for (com.finance.smartLedger.reconciliation.domain.Reconciliation reconciliation : pendingReconciliations) {
        try {
          reconciliationService.startReconciliation(reconciliation.getId(), "scheduler");
          successCount++;
          log.info("Successfully started reconciliation: {}", reconciliation.getReconciliationNumber());
        } catch (Exception e) {
          failureCount++;
          log.error("Failed to start reconciliation: {}", reconciliation.getReconciliationNumber(), e);
        }
      }

      log.info("Daily reconciliation summary - Found: {}, Started successfully: {}, Failed: {}", 
          foundCount, successCount, failureCount);
      log.info("Daily reconciliation completed at: {}", clockProvider.now());
    } catch (Exception e) {
      log.error("Error during daily reconciliation", e);
    }
  }
}
