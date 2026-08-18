package com.finance.smartLedger.ai.application;

import com.finance.smartLedger.ai.application.dto.AIInsightRequest;

public interface AIInsightGateway {

  void requestInsight(AIInsightRequest request);

  boolean isHealthy();
}
