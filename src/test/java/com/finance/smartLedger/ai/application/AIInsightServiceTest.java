package com.finance.smartLedger.ai.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.smartLedger.ai.application.dto.AICallbackRequest;
import com.finance.smartLedger.ai.application.dto.AIInsightRequest;
import com.finance.smartLedger.ai.domain.AIInsight;
import com.finance.smartLedger.ai.domain.AIInsightType;
import com.finance.smartLedger.ai.domain.InsightStatus;
import com.finance.smartLedger.ai.domain.RiskLevel;
import com.finance.smartLedger.ai.infrastructure.persistence.AIInsightRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AIInsightServiceTest {

  @Mock
  private AIInsightRepository aiInsightRepository;

  @Mock
  private AIInsightGateway aiInsightGateway;

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private AIInsightService aiInsightService;

  private UUID reconciliationId;
  private String reconciliationNumber;
  private String sourceSystem;

  @BeforeEach
  void setUp() {
    reconciliationId = UUID.randomUUID();
    reconciliationNumber = "REC-001";
    sourceSystem = "PAYSTACK";
    ReflectionTestUtils.setField(aiInsightService, "appBaseUrl", "http://localhost:8081");
  }

  @Test
  void testCreateReconciliationInsight_Success() {
    // Arrange
    AIInsight savedInsight = AIInsight.builder()
        .requestId("test-request-id")
        .reconciliationId(reconciliationId)
        .insightType(AIInsightType.RECONCILIATION)
        .status(InsightStatus.PROCESSING)
        .build();

    when(aiInsightRepository.save(any(AIInsight.class))).thenReturn(savedInsight);

    // Act
    AIInsight result = aiInsightService.createReconciliationInsight(
        reconciliationId,
        reconciliationNumber,
        sourceSystem,
        2, // duplicatePayments
        1, // missingSettlements
        3, // amountMismatches
        0, // negativeBalances
        10, // transactionCount
        "PARTIALLY_MATCHED",
        "SYSTEM"
    );

    // Assert
    assertNotNull(result);
    assertEquals("test-request-id", result.getRequestId());
    assertEquals(reconciliationId, result.getReconciliationId());
    assertEquals(AIInsightType.RECONCILIATION, result.getInsightType());
    verify(aiInsightRepository, times(2)).save(any(AIInsight.class)); // Initial save + save after marking as processing
    verify(aiInsightGateway).requestInsight(any(AIInsightRequest.class));
  }

  @Test
  void testCreateReconciliationInsight_N8nFailure() {
    // Arrange
    AIInsight savedInsight = AIInsight.builder()
        .requestId("test-request-id")
        .reconciliationId(reconciliationId)
        .insightType(AIInsightType.RECONCILIATION)
        .status(InsightStatus.PROCESSING)
        .build();

    when(aiInsightRepository.save(any(AIInsight.class))).thenReturn(savedInsight);
    doThrow(new RuntimeException("n8n unavailable")).when(aiInsightGateway).requestInsight(any(AIInsightRequest.class));

    // Act
    AIInsight result = aiInsightService.createReconciliationInsight(
        reconciliationId,
        reconciliationNumber,
        sourceSystem,
        0, 0, 0, 0, 10, "COMPLETED", "SYSTEM"
    );

    // Assert
    assertNotNull(result);
    assertEquals(InsightStatus.FAILED, result.getStatus());
    verify(aiInsightRepository, times(3)).save(any(AIInsight.class)); // Initial save + save after marking as processing + save after marking as failed
  }

  @Test
  void testFindById() {
    // Arrange
    UUID insightId = UUID.randomUUID();
    AIInsight insight = AIInsight.builder()
        .requestId("test-request-id")
        .build();
    insight.setId(insightId);

    when(aiInsightRepository.findById(insightId)).thenReturn(Optional.of(insight));

    // Act
    Optional<AIInsight> result = aiInsightService.findById(insightId);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(insightId, result.get().getId());
    verify(aiInsightRepository).findById(insightId);
  }

  @Test
  void testFindByRequestId() {
    // Arrange
    String requestId = "test-request-id";
    AIInsight insight = AIInsight.builder()
        .requestId(requestId)
        .build();

    when(aiInsightRepository.findByRequestId(requestId)).thenReturn(Optional.of(insight));

    // Act
    Optional<AIInsight> result = aiInsightService.findByRequestId(requestId);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(requestId, result.get().getRequestId());
    verify(aiInsightRepository).findByRequestId(requestId);
  }

  @Test
  void testFindByReconciliationId() {
    // Arrange
    AIInsight insight1 = AIInsight.builder()
        .reconciliationId(reconciliationId)
        .build();
    insight1.setId(UUID.randomUUID());
    AIInsight insight2 = AIInsight.builder()
        .reconciliationId(reconciliationId)
        .build();
    insight2.setId(UUID.randomUUID());

    when(aiInsightRepository.findByReconciliationId(reconciliationId))
        .thenReturn(Arrays.asList(insight1, insight2));

    // Act
    List<AIInsight> result = aiInsightService.findByReconciliationId(reconciliationId);

    // Assert
    assertEquals(2, result.size());
    verify(aiInsightRepository).findByReconciliationId(reconciliationId);
  }

  @Test
  void testFindByStatus() {
    // Arrange
    AIInsight insight = AIInsight.builder()
        .status(InsightStatus.PENDING)
        .build();
    insight.setId(UUID.randomUUID());

    when(aiInsightRepository.findByStatus(InsightStatus.PENDING))
        .thenReturn(Arrays.asList(insight));

    // Act
    List<AIInsight> result = aiInsightService.findByStatus(InsightStatus.PENDING);

    // Assert
    assertEquals(1, result.size());
    verify(aiInsightRepository).findByStatus(InsightStatus.PENDING);
  }

  @Test
  void testFindByRiskLevel() {
    // Arrange
    AIInsight insight = AIInsight.builder()
        .riskLevel(RiskLevel.HIGH)
        .build();
    insight.setId(UUID.randomUUID());

    when(aiInsightRepository.findByRiskLevel(RiskLevel.HIGH))
        .thenReturn(Arrays.asList(insight));

    // Act
    List<AIInsight> result = aiInsightService.findByRiskLevel(RiskLevel.HIGH);

    // Assert
    assertEquals(1, result.size());
    verify(aiInsightRepository).findByRiskLevel(RiskLevel.HIGH);
  }

  @Test
  void testHandleCallback_Success() throws Exception {
    // Arrange
    String requestId = "test-request-id";
    AIInsight insight = AIInsight.builder()
        .requestId(requestId)
        .status(InsightStatus.PROCESSING)
        .build();
    insight.setId(UUID.randomUUID());

    List<String> recommendations = Arrays.asList("Review duplicate payments", "Check settlement records");
    String recommendationsJson = "[\"Review duplicate payments\",\"Check settlement records\"]";

    AICallbackRequest callback = AICallbackRequest.builder()
        .requestId(requestId)
        .reconciliationId(reconciliationId)
        .riskLevel(RiskLevel.HIGH)
        .summary("Test summary")
        .rootCause("Test root cause")
        .recommendations(recommendations)
        .signature("test-signature")
        .build();

    when(aiInsightRepository.findByRequestId(requestId)).thenReturn(Optional.of(insight));
    when(objectMapper.writeValueAsString(recommendations)).thenReturn(recommendationsJson);
    when(aiInsightRepository.save(any(AIInsight.class))).thenReturn(insight);

    // Act
    aiInsightService.handleCallback(callback);

    // Assert
    assertEquals(InsightStatus.COMPLETED, insight.getStatus());
    assertEquals(RiskLevel.HIGH, insight.getRiskLevel());
    assertEquals("Test summary", insight.getSummary());
    assertEquals("Test root cause", insight.getRootCause());
    assertEquals(recommendationsJson, insight.getRecommendations());
    verify(aiInsightRepository).save(insight);
  }

  @Test
  void testHandleCallback_InsightNotFound() {
    // Arrange
    String requestId = "non-existent-request-id";
    AICallbackRequest callback = AICallbackRequest.builder()
        .requestId(requestId)
        .build();

    when(aiInsightRepository.findByRequestId(requestId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> aiInsightService.handleCallback(callback));
  }

  @Test
  void testRetryFailedInsights() {
    // Arrange
    AIInsight failedInsight = AIInsight.builder()
        .requestId("failed-request-id")
        .status(InsightStatus.FAILED)
        .retryCount(0)
        .maxRetries(3)
        .build();
    failedInsight.setId(UUID.randomUUID());

    when(aiInsightRepository.findByStatusAndRetryCountLessThanMaxRetries(InsightStatus.FAILED))
        .thenReturn(Arrays.asList(failedInsight));
    when(aiInsightRepository.save(any(AIInsight.class))).thenReturn(failedInsight);

    // Act
    aiInsightService.retryFailedInsights();

    // Assert
    assertEquals(1, failedInsight.getRetryCount());
    assertEquals(InsightStatus.FAILED, failedInsight.getStatus()); // Marked as failed again due to missing context
    verify(aiInsightRepository, times(2)).save(failedInsight); // Save after incrementing retry + save after marking as failed
  }

  @Test
  void testFindFailedInsights() {
    // Arrange
    AIInsight failedInsight = AIInsight.builder()
        .status(InsightStatus.FAILED)
        .build();
    failedInsight.setId(UUID.randomUUID());

    when(aiInsightRepository.findByStatus(InsightStatus.FAILED))
        .thenReturn(Arrays.asList(failedInsight));

    // Act
    List<AIInsight> result = aiInsightService.findFailedInsights();

    // Assert
    assertEquals(1, result.size());
    verify(aiInsightRepository).findByStatus(InsightStatus.FAILED);
  }

  @Test
  void testFindRetryableInsights() {
    // Arrange
    AIInsight retryableInsight = AIInsight.builder()
        .status(InsightStatus.FAILED)
        .retryCount(1)
        .maxRetries(3)
        .build();
    retryableInsight.setId(UUID.randomUUID());

    when(aiInsightRepository.findByStatusAndRetryCountLessThanMaxRetries(InsightStatus.FAILED))
        .thenReturn(Arrays.asList(retryableInsight));

    // Act
    List<AIInsight> result = aiInsightService.findRetryableInsights();

    // Assert
    assertEquals(1, result.size());
    verify(aiInsightRepository).findByStatusAndRetryCountLessThanMaxRetries(InsightStatus.FAILED);
  }
}
