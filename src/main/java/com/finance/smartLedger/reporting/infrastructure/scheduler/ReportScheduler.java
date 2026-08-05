package com.finance.smartLedger.reporting.infrastructure.scheduler;

import com.finance.smartLedger.shared.util.ClockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduled.enabled", havingValue = "true", matchIfMissing = true)
public class ReportScheduler {

  private static final Logger log = LoggerFactory.getLogger(ReportScheduler.class);

  private final ClockProvider clockProvider;

  @Value("${app.scheduled.report-generation:0 0 6 * * MON}")
  private String reportGenerationCron;

  public ReportScheduler(ClockProvider clockProvider) {
    this.clockProvider = clockProvider;
  }

  @Scheduled(cron = "${app.scheduled.report-generation:0 0 6 * * MON}")
  public void generateWeeklyReports() {
    log.info("Starting weekly report generation at: {}", clockProvider.now());
    // Report generation logic will be implemented in Phase 8
    log.info("Weekly report generation completed at: {}", clockProvider.now());
  }
}
