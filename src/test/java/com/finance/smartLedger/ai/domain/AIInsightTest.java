package com.finance.smartLedger.ai.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AIInsightTest {

  @Test
  void testMarkAsProcessing() {
    AIInsight insight = AIInsight.builder()
        .requestId("test-request-id")
        .status(InsightStatus.PENDING)
        .build();

    insight.markAsProcessing();

    assertEquals(InsightStatus.PROCESSING, insight.getStatus());
    assertEquals("SYSTEM", insight.getUpdatedBy());
  }

  @Test
  void testMarkAsCompleted() {
    AIInsight insight = AIInsight.builder()
        .requestId("test-request-id")
        .status(InsightStatus.PROCESSING)
        .build();

    String recommendations = "[\"Review duplicate payments\", \"Check settlement records\"]";

    insight.markAsCompleted(
        RiskLevel.HIGH,
        "Multiple reconciliation anomalies detected",
        "Duplicate gateway references and settlement mismatches",
        recommendations
    );

    assertEquals(InsightStatus.COMPLETED, insight.getStatus());
    assertEquals(RiskLevel.HIGH, insight.getRiskLevel());
    assertEquals("Multiple reconciliation anomalies detected", insight.getSummary());
    assertEquals("Duplicate gateway references and settlement mismatches", insight.getRootCause());
    assertEquals(recommendations, insight.getRecommendations());
    assertNotNull(insight.getCompletedAt());
    assertEquals("SYSTEM", insight.getUpdatedBy());
  }

  @Test
  void testMarkAsFailed() {
    AIInsight insight = AIInsight.builder()
        .requestId("test-request-id")
        .status(InsightStatus.PROCESSING)
        .build();

    insight.markAsFailed("n8n service unavailable");

    assertEquals(InsightStatus.FAILED, insight.getStatus());
    assertEquals("n8n service unavailable", insight.getFailureReason());
    assertNotNull(insight.getCompletedAt());
    assertEquals("SYSTEM", insight.getUpdatedBy());
  }

  @Test
  void testIncrementRetryCount() {
    AIInsight insight = AIInsight.builder()
        .requestId("test-request-id")
        .retryCount(0)
        .build();

    insight.incrementRetryCount();

    assertEquals(1, insight.getRetryCount());
    assertEquals("SYSTEM", insight.getUpdatedBy());
  }

  @Test
  void testCanRetry_WhenFailedAndUnderLimit() {
    AIInsight insight = AIInsight.builder()
        .requestId("test-request-id")
        .status(InsightStatus.FAILED)
        .retryCount(1)
        .maxRetries(3)
        .build();

    assertTrue(insight.canRetry());
  }

  @Test
  void testCanRetry_WhenFailedAndAtLimit() {
    AIInsight insight = AIInsight.builder()
        .requestId("test-request-id")
        .status(InsightStatus.FAILED)
        .retryCount(3)
        .maxRetries(3)
        .build();

    assertFalse(insight.canRetry());
  }

  @Test
  void testCanRetry_WhenNotFailed() {
    AIInsight insight = AIInsight.builder()
        .requestId("test-request-id")
        .status(InsightStatus.PENDING)
        .retryCount(0)
        .maxRetries(3)
        .build();

    assertFalse(insight.canRetry());
  }

  @Test
  void testBuilder() {
    UUID reconciliationId = UUID.randomUUID();

    AIInsight insight = AIInsight.builder()
        .requestId("test-request-id")
        .reconciliationId(reconciliationId)
        .insightType(AIInsightType.RECONCILIATION)
        .status(InsightStatus.PENDING)
        .riskLevel(RiskLevel.MEDIUM)
        .summary("Test summary")
        .rootCause("Test root cause")
        .recommendations("[\"action1\"]")
        .anomalyCount(5)
        .requestedAt(LocalDateTime.now())
        .retryCount(0)
        .maxRetries(3)
        .build();

    assertEquals("test-request-id", insight.getRequestId());
    assertEquals(reconciliationId, insight.getReconciliationId());
    assertEquals(AIInsightType.RECONCILIATION, insight.getInsightType());
    assertEquals(InsightStatus.PENDING, insight.getStatus());
    assertEquals(RiskLevel.MEDIUM, insight.getRiskLevel());
    assertEquals("Test summary", insight.getSummary());
    assertEquals("Test root cause", insight.getRootCause());
    assertEquals("[\"action1\"]", insight.getRecommendations());
    assertEquals(5, insight.getAnomalyCount());
    assertEquals(0, insight.getRetryCount());
    assertEquals(3, insight.getMaxRetries());
  }
}
