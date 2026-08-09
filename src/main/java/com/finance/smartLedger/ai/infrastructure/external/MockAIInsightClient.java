package com.finance.smartLedger.ai.infrastructure.external;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock", matchIfMissing = true)
@Slf4j
public class MockAIInsightClient implements AIInsightClient {

  @Override
  public AIInsightResponse generateAnomalyInsight(Map<String, Object> context) {
    log.info("Using Mock AI client for anomaly detection - rule-based detection is primary");

    return AIInsightResponse.builder()
        .insightType("ANOMALY_DETECTION")
        .title("Rule-Based Anomaly Detection")
        .description(
            "Using rule-based anomaly detection. Configure AI provider for enhanced insights.")
        .severity("MEDIUM")
        .recommendation(
            "Review anomalies detected by rule-based system. Configure AI provider for enhanced insights.")
        .confidenceScore(0.7)
        .rootCause("Rule-based analysis")
        .suggestedActions(
            List.of(
                "Review rule-based anomaly detection results",
                "Configure AI provider for enhanced insights (optional)",
                "Adjust anomaly detection thresholds if needed"))
        .affectedEntities(context)
        .success(true)
        .build();
  }

  @Override
  public AIInsightResponse generateCashFlowForecastInsight(Map<String, Object> context) {
    log.info("Using Mock AI client for cash flow forecast - historical analysis is primary");

    return AIInsightResponse.builder()
        .insightType("CASH_FLOW_FORECAST")
        .title("Historical Cash Flow Analysis")
        .description(
            "Using historical data analysis. Configure AI provider for predictive forecasting.")
        .severity("LOW")
        .recommendation(
            "Review historical payment patterns. Configure AI provider for predictive forecasting.")
        .confidenceScore(0.6)
        .rootCause("Historical trend analysis")
        .suggestedActions(
            List.of(
                "Review historical payment trends",
                "Configure AI provider for predictive forecasting (optional)"))
        .affectedEntities(context)
        .success(true)
        .build();
  }

  @Override
  public AIInsightResponse generateReconciliationInsight(Map<String, Object> context) {
    log.info("Using Mock AI client for reconciliation - rule-based variance detection is primary");

    return AIInsightResponse.builder()
        .insightType("RECONCILIATION")
        .title("Rule-Based Reconciliation Analysis")
        .description(
            "Using rule-based variance detection. Configure AI provider for root cause analysis.")
        .severity("MEDIUM")
        .recommendation(
            "Review variances detected by rule-based system. Configure AI provider for root cause analysis.")
        .confidenceScore(0.7)
        .rootCause("Rule-based variance analysis")
        .suggestedActions(
            List.of(
                "Review rule-based reconciliation results",
                "Configure AI provider for root cause analysis (optional)"))
        .affectedEntities(context)
        .success(true)
        .build();
  }

  @Override
  public boolean isHealthy() {
    return true;
  }
}
