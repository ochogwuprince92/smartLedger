package com.finance.smartLedger.ai.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.smartLedger.ai.application.dto.AICallbackRequest;
import com.finance.smartLedger.ai.application.dto.AIInsightRequest;
import com.finance.smartLedger.ai.domain.AIInsight;
import com.finance.smartLedger.ai.domain.AIInsightType;
import com.finance.smartLedger.ai.domain.InsightStatus;
import com.finance.smartLedger.ai.domain.RiskLevel;
import com.finance.smartLedger.ai.infrastructure.persistence.AIInsightRepository;
import com.finance.smartLedger.reconciliation.application.ReconciliationService;
import com.finance.smartLedger.reconciliation.domain.Reconciliation;
import com.finance.smartLedger.reconciliation.domain.ReconciliationStatus;
import com.finance.smartLedger.reconciliation.infrastructure.persistence.ReconciliationRepository;
import com.finance.smartLedger.shared.domain.EventPublisher;
import com.finance.smartLedger.test.configuration.TestDatabaseConfiguration;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Disabled;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.redis.enabled=false",
    "spring.cache.type=none",
    "app.scheduled.enabled=false",
    "app.data-loader.enabled=false"
})
@Disabled("Docker not available on this system")
@Transactional
class AIInsightIntegrationTest {

  @Autowired
  private AIInsightService aiInsightService;

  @Autowired
  private AIInsightRepository aiInsightRepository;

  @Autowired
  private ReconciliationService reconciliationService;

  @Autowired
  private ReconciliationRepository reconciliationRepository;

  @MockBean
  private AIInsightGateway aiInsightGateway;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    TestDatabaseConfiguration.configureDatabase(registry);
  }

  private UUID reconciliationId;
  private String reconciliationNumber;

  @BeforeEach
  void setUp() {
    // Clean up database
    aiInsightRepository.deleteAll();
    reconciliationRepository.deleteAll();

    reconciliationId = UUID.randomUUID();
    reconciliationNumber = "REC-" + System.currentTimeMillis();
  }

  @Test
  void testCompleteFlow_ReconciliationToAIInsight() {
    // Arrange - Create reconciliation
    Reconciliation reconciliation = reconciliationService.createReconciliation(
        reconciliationNumber,
        LocalDateTime.now(),
        "PAYSTACK",
        "REF-001",
        new BigDecimal("10000.00"),
        "Test reconciliation",
        "SYSTEM"
    );

    // Act - Complete reconciliation (this should trigger AI insight creation)
    doNothing().when(aiInsightGateway).requestInsight(any(AIInsightRequest.class));

    Reconciliation completedReconciliation = reconciliationService.completeReconciliation(
        reconciliation.getId(),
        "SYSTEM"
    );

    // Assert - Reconciliation completed
    assertNotNull(completedReconciliation);
    assertEquals(ReconciliationStatus.PARTIALLY_MATCHED, completedReconciliation.getStatus());

    // Note: AI insight creation is async via event handler, so we need to wait or trigger manually
    // For integration test, we'll verify the service method directly
  }

  @Test
  void testCreateReconciliationInsightAndCallback() {
    // Arrange - Create AI insight
    doNothing().when(aiInsightGateway).requestInsight(any(AIInsightRequest.class));

    AIInsight insight = aiInsightService.createReconciliationInsight(
        reconciliationId,
        reconciliationNumber,
        "PAYSTACK",
        2, // duplicatePayments
        1, // missingSettlements
        3, // amountMismatches
        0, // negativeBalances
        10, // transactionCount
        "PARTIALLY_MATCHED",
        "SYSTEM"
    );

    // Assert - Insight created and sent to n8n
    assertNotNull(insight);
    assertNotNull(insight.getId());
    assertEquals(reconciliationId, insight.getReconciliationId());
    assertEquals(AIInsightType.RECONCILIATION, insight.getInsightType());
    assertEquals(InsightStatus.PROCESSING, insight.getStatus());
    assertEquals(6, insight.getAnomalyCount()); // 2+1+3+0
    verify(aiInsightGateway).requestInsight(any(AIInsightRequest.class));

    // Act - Simulate callback from n8n
    List<String> recommendations = Arrays.asList(
        "Review duplicate payment references",
        "Compare gateway settlement records",
        "Re-run reconciliation after correction"
    );

    AICallbackRequest callback = AICallbackRequest.builder()
        .requestId(insight.getRequestId())
        .reconciliationId(reconciliationId)
        .riskLevel(RiskLevel.HIGH)
        .summary("Multiple reconciliation anomalies were detected")
        .rootCause("Duplicate gateway references and settlement mismatches")
        .recommendations(recommendations)
        .signature("test-signature")
        .build();

    aiInsightService.handleCallback(callback);

    // Assert - Insight completed
    AIInsight updatedInsight = aiInsightRepository.findById(insight.getId()).orElse(null);
    assertNotNull(updatedInsight);
    assertEquals(InsightStatus.COMPLETED, updatedInsight.getStatus());
    assertEquals(RiskLevel.HIGH, updatedInsight.getRiskLevel());
    assertEquals("Multiple reconciliation anomalies were detected", updatedInsight.getSummary());
    assertEquals("Duplicate gateway references and settlement mismatches", updatedInsight.getRootCause());
    assertNotNull(updatedInsight.getRecommendations());
    assertNotNull(updatedInsight.getCompletedAt());
  }

  @Test
  void testCreateReconciliationInsight_N8nFailure() {
    // Arrange
    doThrow(new RuntimeException("n8n service unavailable"))
        .when(aiInsightGateway).requestInsight(any(AIInsightRequest.class));

    // Act
    AIInsight insight = aiInsightService.createReconciliationInsight(
        reconciliationId,
        reconciliationNumber,
        "PAYSTACK",
        0, 0, 0, 0, 10, "COMPLETED", "SYSTEM"
    );

    // Assert - Insight created but marked as failed
    assertNotNull(insight);
    assertEquals(InsightStatus.FAILED, insight.getStatus());
    assertEquals("n8n service unavailable", insight.getFailureReason());
    assertNotNull(insight.getCompletedAt());
  }

  @Test
  void testRetryFailedInsights() {
    // Arrange - Create failed insight
    AIInsight failedInsight = aiInsightService.createReconciliationInsight(
        reconciliationId,
        reconciliationNumber,
        "PAYSTACK",
        0, 0, 0, 0, 10, "COMPLETED", "SYSTEM"
    );

    // Manually mark as failed (simulating n8n failure)
    failedInsight.markAsFailed("n8n timeout");
    aiInsightRepository.save(failedInsight);

    // Act
    aiInsightService.retryFailedInsights();

    // Assert
    AIInsight updatedInsight = aiInsightRepository.findById(failedInsight.getId()).orElse(null);
    assertNotNull(updatedInsight);
    assertEquals(1, updatedInsight.getRetryCount());
    // Note: Due to missing original request context, it will be marked as failed again
    assertEquals(InsightStatus.FAILED, updatedInsight.getStatus());
  }

  @Test
  void testFindByReconciliationId() {
    // Arrange - Create insights for same reconciliation
    doNothing().when(aiInsightGateway).requestInsight(any(AIInsightRequest.class));

    aiInsightService.createReconciliationInsight(
        reconciliationId,
        reconciliationNumber,
        "PAYSTACK",
        0, 0, 0, 0, 10, "COMPLETED", "SYSTEM"
    );

    // Act
    List<AIInsight> insights = aiInsightService.findByReconciliationId(reconciliationId);

    // Assert
    assertNotNull(insights);
    assertFalse(insights.isEmpty());
    assertEquals(reconciliationId, insights.get(0).getReconciliationId());
  }

  @Test
  void testFindByStatus() {
    // Arrange
    doNothing().when(aiInsightGateway).requestInsight(any(AIInsightRequest.class));

    AIInsight insight1 = aiInsightService.createReconciliationInsight(
        reconciliationId,
        reconciliationNumber,
        "PAYSTACK",
        0, 0, 0, 0, 10, "COMPLETED", "SYSTEM"
    );

    // Act
    List<AIInsight> processingInsights = aiInsightService.findByStatus(InsightStatus.PROCESSING);

    // Assert
    assertNotNull(processingInsights);
    assertFalse(processingInsights.isEmpty());
    assertEquals(InsightStatus.PROCESSING, processingInsights.get(0).getStatus());
  }

  @Test
  void testFindByRiskLevel() {
    // Arrange - Create insight and mark as completed with risk level
    doNothing().when(aiInsightGateway).requestInsight(any(AIInsightRequest.class));

    AIInsight insight = aiInsightService.createReconciliationInsight(
        reconciliationId,
        reconciliationNumber,
        "PAYSTACK",
        0, 0, 0, 0, 10, "COMPLETED", "SYSTEM"
    );

    insight.markAsCompleted(
        RiskLevel.HIGH,
        "Test summary",
        "Test root cause",
        "[\"action1\"]"
    );
    aiInsightRepository.save(insight);

    // Act
    List<AIInsight> highRiskInsights = aiInsightService.findByRiskLevel(RiskLevel.HIGH);

    // Assert
    assertNotNull(highRiskInsights);
    assertFalse(highRiskInsights.isEmpty());
    assertEquals(RiskLevel.HIGH, highRiskInsights.get(0).getRiskLevel());
  }

  @Test
  void testIdempotency_DuplicateCallback() {
    // Arrange - Create insight and complete via callback
    doNothing().when(aiInsightGateway).requestInsight(any(AIInsightRequest.class));

    AIInsight insight = aiInsightService.createReconciliationInsight(
        reconciliationId,
        reconciliationNumber,
        "PAYSTACK",
        0, 0, 0, 0, 10, "COMPLETED", "SYSTEM"
    );

    List<String> recommendations = Arrays.asList("action1", "action2");
    AICallbackRequest callback = AICallbackRequest.builder()
        .requestId(insight.getRequestId())
        .reconciliationId(reconciliationId)
        .riskLevel(RiskLevel.MEDIUM)
        .summary("First callback")
        .rootCause("First cause")
        .recommendations(recommendations)
        .signature("test-signature")
        .build();

    // Act - First callback
    aiInsightService.handleCallback(callback);

    // Try second callback with same requestId
    AICallbackRequest callback2 = AICallbackRequest.builder()
        .requestId(insight.getRequestId())
        .reconciliationId(reconciliationId)
        .riskLevel(RiskLevel.HIGH)
        .summary("Second callback")
        .rootCause("Second cause")
        .recommendations(Arrays.asList("action3"))
        .signature("test-signature")
        .build();

    aiInsightService.handleCallback(callback2);

    // Assert - Should update to latest callback (idempotency handled by requestId lookup)
    AIInsight updatedInsight = aiInsightRepository.findById(insight.getId()).orElse(null);
    assertNotNull(updatedInsight);
    assertEquals(InsightStatus.COMPLETED, updatedInsight.getStatus());
    assertEquals("Second callback", updatedInsight.getSummary()); // Updated by second callback
  }
}
