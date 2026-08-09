package com.finance.smartLedger.ai.infrastructure.scheduler;

import com.finance.smartLedger.ai.application.AIInsightService;
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
  private final AIInsightService aiInsightService;

  @Value("${app.scheduled.ai-insights:0 0 4 * * ?}")
  private String aiInsightsCron;

  @Value("${app.scheduled.ai-insights-retry:0 */30 * * * ?}")
  private String aiInsightsRetryCron;

  public AIInsightScheduler(ClockProvider clockProvider, AIInsightService aiInsightService) {
    this.clockProvider = clockProvider;
    this.aiInsightService = aiInsightService;
  }

  @Scheduled(cron = "${app.scheduled.ai-insights:0 0 4 * * ?}")
  public void generateAIInsights() {
    log.info("Starting AI insight generation at: {}", clockProvider.now());
    // AI insight generation is now triggered by ReconciliationCompleted event
    log.info("AI insight generation completed at: {}", clockProvider.now());
  }

  @Scheduled(cron = "${app.scheduled.ai-insights-retry:0 */30 * * * ?}")
  public void retryFailedAIInsights() {
    log.info("Starting failed AI insights retry at: {}", clockProvider.now());
    try {
      aiInsightService.retryFailedInsights();
      log.info("Failed AI insights retry completed at: {}", clockProvider.now());
    } catch (Exception e) {
      log.error("Failed to retry AI insights", e);
    }
  }
}
