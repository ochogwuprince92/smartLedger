package com.finance.smartLedger.ai.infrastructure.n8n;

import com.finance.smartLedger.ai.application.dto.AIInsightRequest;

public interface N8nClient {

  void requestInsight(AIInsightRequest request);

  boolean isHealthy();
}
