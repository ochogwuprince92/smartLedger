package com.finance.smartLedger.ai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.smartLedger.ai.application.dto.AIInsightRequest;
import com.finance.smartLedger.ai.application.dto.AICallbackRequest;
import com.finance.smartLedger.ai.domain.AIInsight;
import com.finance.smartLedger.ai.domain.AIInsightType;
import com.finance.smartLedger.ai.domain.InsightStatus;
import com.finance.smartLedger.ai.domain.RiskLevel;
import com.finance.smartLedger.ai.infrastructure.persistence.AIInsightRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIInsightService {

  private final AIInsightRepository aiInsightRepository;
  private final AIInsightGateway aiInsightGateway;
  private final ObjectMapper objectMapper;

  @Value("${app.base-url:http://localhost:8081}")
  private String appBaseUrl;

  @Transactional
  public AIInsight createReconciliationInsight(
      UUID reconciliationId,
      String reconciliationNumber,
      String sourceSystem,
      Integer duplicatePayments,
      Integer missingSettlements,
      Integer amountMismatches,
      Integer negativeBalances,
      Integer transactionCount,
      String reconciliationStatus,
      String createdBy) {

    String requestId = UUID.randomUUID().toString();

    AIInsight insight =
        AIInsight.builder()
            .requestId(requestId)
            .reconciliationId(reconciliationId)
            .insightType(AIInsightType.RECONCILIATION)
            .status(InsightStatus.PENDING)
            .requestedAt(LocalDateTime.now())
            .anomalyCount(duplicatePayments + missingSettlements + amountMismatches + negativeBalances)
            .retryCount(0)
            .maxRetries(3)
            .build();
    insight.setCreatedBy(createdBy);

    AIInsight savedInsight = aiInsightRepository.save(insight);

    try {
      sendToN8n(savedInsight, reconciliationNumber, sourceSystem, duplicatePayments,
          missingSettlements, amountMismatches, negativeBalances, transactionCount, reconciliationStatus);
    } catch (Exception e) {
      log.error("Failed to send AI insight request to n8n: requestId={}", requestId, e);
      savedInsight.markAsFailed(e.getMessage());
      aiInsightRepository.save(savedInsight);
    }

    return savedInsight;
  }

  public Optional<AIInsight> findById(UUID id) {
    return aiInsightRepository.findById(id);
  }

  public Optional<AIInsight> findByRequestId(String requestId) {
    return aiInsightRepository.findByRequestId(requestId);
  }

  public List<AIInsight> findByReconciliationId(UUID reconciliationId) {
    return aiInsightRepository.findByReconciliationId(reconciliationId);
  }

  public List<AIInsight> findByStatus(InsightStatus status) {
    return aiInsightRepository.findByStatus(status);
  }

  public List<AIInsight> findByInsightType(AIInsightType insightType) {
    return aiInsightRepository.findByInsightType(insightType);
  }

  public List<AIInsight> findByRiskLevel(RiskLevel riskLevel) {
    return aiInsightRepository.findByRiskLevel(riskLevel);
  }

  public List<AIInsight> findFailedInsights() {
    return aiInsightRepository.findByStatus(InsightStatus.FAILED);
  }

  public List<AIInsight> findRetryableInsights() {
    return aiInsightRepository.findByStatusAndRetryCountLessThanMaxRetries(InsightStatus.FAILED);
  }

  @Transactional
  public void handleCallback(AICallbackRequest callback) {
    AIInsight insight =
        aiInsightRepository
            .findByRequestId(callback.getRequestId())
            .orElseThrow(() -> new IllegalArgumentException("AI Insight not found for requestId: " + callback.getRequestId()));

    try {
      String recommendationsJson = objectMapper.writeValueAsString(callback.getRecommendations());
      insight.markAsCompleted(
          callback.getRiskLevel(),
          callback.getSummary(),
          callback.getRootCause(),
          recommendationsJson);
      aiInsightRepository.save(insight);
      log.info("AI insight completed via callback: requestId={}", callback.getRequestId());
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize recommendations for callback: requestId={}", callback.getRequestId(), e);
      insight.markAsFailed("Failed to process recommendations");
      aiInsightRepository.save(insight);
    }
  }

  @Transactional
  public void retryFailedInsights() {
    List<AIInsight> retryableInsights = findRetryableInsights();
    
    for (AIInsight insight : retryableInsights) {
      try {
        insight.incrementRetryCount();
        insight.setStatus(InsightStatus.PENDING);
        insight.setFailureReason(null);
        aiInsightRepository.save(insight);
        
        log.info("Retrying AI insight: requestId={}, retryCount={}", insight.getRequestId(), insight.getRetryCount());
        
        // Re-send to n8n would happen here if we stored the original request context
        // For now, we'll mark it as failed again since we don't have the original context
        insight.markAsFailed("Retry not implemented - missing original request context");
        aiInsightRepository.save(insight);
        
      } catch (Exception e) {
        log.error("Failed to retry AI insight: requestId={}", insight.getRequestId(), e);
      }
    }
  }

  private void sendToN8n(
      AIInsight insight,
      String reconciliationNumber,
      String sourceSystem,
      Integer duplicatePayments,
      Integer missingSettlements,
      Integer amountMismatches,
      Integer negativeBalances,
      Integer transactionCount,
      String reconciliationStatus) {

    insight.markAsProcessing();
    aiInsightRepository.save(insight);

    String callbackUrl = appBaseUrl + "/api/v1/ai-insights/callback";

    AIInsightRequest request =
        AIInsightRequest.builder()
            .requestId(insight.getRequestId())
            .reconciliationId(insight.getReconciliationId())
            .reconciliationNumber(reconciliationNumber)
            .sourceSystem(sourceSystem)
            .duplicatePayments(duplicatePayments)
            .missingSettlements(missingSettlements)
            .amountMismatches(amountMismatches)
            .negativeBalances(negativeBalances)
            .transactionCount(transactionCount)
            .reconciliationStatus(reconciliationStatus)
            .callbackUrl(callbackUrl)
            .build();

    aiInsightGateway.requestInsight(request);
  }
}
