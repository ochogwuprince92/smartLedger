package com.finance.smartLedger.ai.infrastructure.external;

import java.util.Map;

public interface AIInsightClient {

  AIInsightResponse generateAnomalyInsight(Map<String, Object> context);

  AIInsightResponse generateCashFlowForecastInsight(Map<String, Object> context);

  AIInsightResponse generateReconciliationInsight(Map<String, Object> context);

  boolean isHealthy();
}
