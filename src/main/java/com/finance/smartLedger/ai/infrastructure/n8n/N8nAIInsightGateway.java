package com.finance.smartLedger.ai.infrastructure.n8n;

import com.finance.smartLedger.ai.application.AIInsightGateway;
import com.finance.smartLedger.ai.application.dto.AIInsightRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class N8nAIInsightGateway implements AIInsightGateway {

  private final N8nClient n8nClient;

  @Override
  public void requestInsight(AIInsightRequest request) {
    n8nClient.requestInsight(request);
  }

  @Override
  public boolean isHealthy() {
    return n8nClient.isHealthy();
  }
}
