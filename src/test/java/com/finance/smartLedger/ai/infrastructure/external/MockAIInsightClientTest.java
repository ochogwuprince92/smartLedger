package com.finance.smartLedger.ai.infrastructure.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MockAIInsightClientTest {

  private MockAIInsightClient mockAIInsightClient;

  @BeforeEach
  void setUp() {
    mockAIInsightClient = new MockAIInsightClient();
  }

  @Test
  void generateAnomalyInsight_ShouldReturnRuleBasedInsight() {
    // Arrange
    Map<String, Object> context = new HashMap<>();
    context.put("generated_by", "system");
    context.put("timestamp", "2024-01-01");
    context.put("analysis_type", "anomaly_detection");

    // Act
    AIInsightResponse response = mockAIInsightClient.generateAnomalyInsight(context);

    // Assert
    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals("ANOMALY_DETECTION", response.getInsightType());
    assertEquals("Rule-Based Anomaly Detection", response.getTitle());
    assertEquals("MEDIUM", response.getSeverity());
    assertNotNull(response.getRecommendation());
    assertEquals(0.7, response.getConfidenceScore());
    assertNotNull(response.getSuggestedActions());
    assertTrue(response.getSuggestedActions().size() > 0);
  }

  @Test
  void generateCashFlowForecastInsight_ShouldReturnRuleBasedInsight() {
    // Arrange
    Map<String, Object> context = new HashMap<>();
    context.put("generated_by", "system");
    context.put("timestamp", "2024-01-01");
    context.put("analysis_type", "cash_flow_forecast");

    // Act
    AIInsightResponse response = mockAIInsightClient.generateCashFlowForecastInsight(context);

    // Assert
    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals("CASH_FLOW_FORECAST", response.getInsightType());
    assertEquals("Historical Cash Flow Analysis", response.getTitle());
    assertEquals("LOW", response.getSeverity());
    assertNotNull(response.getRecommendation());
    assertEquals(0.6, response.getConfidenceScore());
    assertNotNull(response.getSuggestedActions());
  }

  @Test
  void generateReconciliationInsight_ShouldReturnRuleBasedInsight() {
    // Arrange
    Map<String, Object> context = new HashMap<>();
    context.put("generated_by", "system");
    context.put("timestamp", "2024-01-01");
    context.put("analysis_type", "reconciliation");

    // Act
    AIInsightResponse response = mockAIInsightClient.generateReconciliationInsight(context);

    // Assert
    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals("RECONCILIATION", response.getInsightType());
    assertEquals("Rule-Based Reconciliation Analysis", response.getTitle());
    assertEquals("MEDIUM", response.getSeverity());
    assertNotNull(response.getRecommendation());
    assertEquals(0.7, response.getConfidenceScore());
    assertNotNull(response.getSuggestedActions());
  }

  @Test
  void isHealthy_ShouldReturnTrue() {
    // Act
    boolean healthy = mockAIInsightClient.isHealthy();

    // Assert
    assertTrue(healthy);
  }

  @Test
  void generateAnomalyInsight_ShouldIncludeContextInAffectedEntities() {
    // Arrange
    Map<String, Object> context = new HashMap<>();
    context.put("test_key", "test_value");
    context.put("number", 123);

    // Act
    AIInsightResponse response = mockAIInsightClient.generateAnomalyInsight(context);

    // Assert
    assertNotNull(response.getAffectedEntities());
    assertEquals(context, response.getAffectedEntities());
  }

  @Test
  void generateCashFlowForecastInsight_ShouldIncludeContextInAffectedEntities() {
    // Arrange
    Map<String, Object> context = new HashMap<>();
    context.put("cash_flow_data", "sample_data");

    // Act
    AIInsightResponse response = mockAIInsightClient.generateCashFlowForecastInsight(context);

    // Assert
    assertNotNull(response.getAffectedEntities());
    assertEquals(context, response.getAffectedEntities());
  }

  @Test
  void generateReconciliationInsight_ShouldIncludeContextInAffectedEntities() {
    // Arrange
    Map<String, Object> context = new HashMap<>();
    context.put("reconciliation_id", "REC001");

    // Act
    AIInsightResponse response = mockAIInsightClient.generateReconciliationInsight(context);

    // Assert
    assertNotNull(response.getAffectedEntities());
    assertEquals(context, response.getAffectedEntities());
  }

  @Test
  void generateAnomalyInsight_ShouldHaveRootCause() {
    // Arrange
    Map<String, Object> context = new HashMap<>();

    // Act
    AIInsightResponse response = mockAIInsightClient.generateAnomalyInsight(context);

    // Assert
    assertNotNull(response.getRootCause());
    assertEquals("Rule-based analysis", response.getRootCause());
  }

  @Test
  void generateCashFlowForecastInsight_ShouldHaveRootCause() {
    // Arrange
    Map<String, Object> context = new HashMap<>();

    // Act
    AIInsightResponse response = mockAIInsightClient.generateCashFlowForecastInsight(context);

    // Assert
    assertNotNull(response.getRootCause());
    assertEquals("Historical trend analysis", response.getRootCause());
  }

  @Test
  void generateReconciliationInsight_ShouldHaveRootCause() {
    // Arrange
    Map<String, Object> context = new HashMap<>();

    // Act
    AIInsightResponse response = mockAIInsightClient.generateReconciliationInsight(context);

    // Assert
    assertNotNull(response.getRootCause());
    assertEquals("Rule-based variance analysis", response.getRootCause());
  }

  @Test
  void generateAnomalyInsight_ShouldSuggestConfiguringAI() {
    // Arrange
    Map<String, Object> context = new HashMap<>();

    // Act
    AIInsightResponse response = mockAIInsightClient.generateAnomalyInsight(context);

    // Assert
    assertTrue(
        response.getRecommendation().contains("Configure AI provider for enhanced insights"));
  }

  @Test
  void generateCashFlowForecastInsight_ShouldSuggestConfiguringAI() {
    // Arrange
    Map<String, Object> context = new HashMap<>();

    // Act
    AIInsightResponse response = mockAIInsightClient.generateCashFlowForecastInsight(context);

    // Assert
    assertTrue(
        response.getRecommendation().contains("Configure AI provider for predictive forecasting"));
  }

  @Test
  void generateReconciliationInsight_ShouldSuggestConfiguringAI() {
    // Arrange
    Map<String, Object> context = new HashMap<>();

    // Act
    AIInsightResponse response = mockAIInsightClient.generateReconciliationInsight(context);

    // Assert
    assertTrue(
        response.getRecommendation().contains("Configure AI provider for root cause analysis"));
  }
}
