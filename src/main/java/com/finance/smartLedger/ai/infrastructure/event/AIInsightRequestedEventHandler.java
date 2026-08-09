package com.finance.smartLedger.ai.infrastructure.event;

import com.finance.smartLedger.ai.application.AIInsightService;
import com.finance.smartLedger.ai.domain.AIInsightRequested;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIInsightRequestedEventHandler {

  private final AIInsightService aiInsightService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleAIInsightRequested(AIInsightRequested event) {
    try {
      log.info("Handling AIInsightRequested event: requestId={}, reconciliationId={}", 
          event.getRequestId(), event.getReconciliationId());

      aiInsightService.createReconciliationInsight(
          event.getReconciliationId(),
          event.getReconciliationNumber(),
          event.getSourceSystem(),
          event.getDuplicatePayments(),
          event.getMissingSettlements(),
          event.getAmountMismatches(),
          event.getNegativeBalances(),
          event.getTransactionCount(),
          event.getReconciliationStatus(),
          "SYSTEM");

      log.info("AI insight created successfully: requestId={}", event.getRequestId());

    } catch (Exception e) {
      log.error("Failed to handle AIInsightRequested event: requestId={}", event.getRequestId(), e);
      // Don't throw - reconciliation should not fail due to AI issues
    }
  }
}
