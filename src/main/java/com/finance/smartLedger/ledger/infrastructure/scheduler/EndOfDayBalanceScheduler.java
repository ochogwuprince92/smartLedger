package com.finance.smartLedger.ledger.infrastructure.scheduler;

import com.finance.smartLedger.ledger.application.BalanceService;
import com.finance.smartLedger.shared.util.ClockProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduled.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class EndOfDayBalanceScheduler {

  private final BalanceService balanceService;
  private final ClockProvider clockProvider;

  @Value("${app.scheduled.end-of-day:0 0 23 * * ?}")
  private String endOfDayCron;

  @Scheduled(cron = "${app.scheduled.end-of-day:0 0 23 * * ?}")
  public void processEndOfDayBalances() {
    log.info("Starting end-of-day balance processing at: {}", clockProvider.now());
    try {
      // balanceService.reconcileAllBalances("scheduler");
      log.info("End-of-day balance processing completed at: {}", clockProvider.now());
    } catch (Exception e) {
      log.error("Error during end-of-day balance processing", e);
    }
  }
}
