package com.finance.smartLedger.ai.infrastructure.scheduler;

import com.finance.smartLedger.shared.util.ClockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduled.enabled", havingValue = "true", matchIfMissing = true)
public class AIInsightScheduler {

  private static final Logger log = LoggerFactory.getLogger(AIInsightScheduler.class);

  private final ClockProvider clockProvider;

  @Value("${app.scheduled.ai-insights:0 0 4 * * ?}")
  private String aiInsightsCron;

  public AIInsightScheduler(ClockProvider clockProvider) {
    this.clockProvider = clockProvider;
  }

  @Scheduled(cron = "${app.scheduled.ai-insights:0 0 4 * * ?}")
  public void generateAIInsights() {
    log.info("Starting AI insight generation at: {}", clockProvider.now());
    // AI insight generation logic will be implemented in Phase 8
    log.info("AI insight generation completed at: {}", clockProvider.now());
  }
}
