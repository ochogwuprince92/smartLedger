package com.finance.smartLedger.ai.application;

import com.finance.smartLedger.ai.domain.AIInsight;
import com.finance.smartLedger.ai.domain.InsightStatus;
import com.finance.smartLedger.ai.infrastructure.external.AIInsightClient;
import com.finance.smartLedger.ai.infrastructure.external.AIInsightResponse;
import com.finance.smartLedger.ai.infrastructure.persistence.AIInsightRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIInsightService {

  private final AIInsightRepository aiInsightRepository;
  private final AIInsightClient aiInsightClient;

  public AIInsight createInsight(
      String insightType,
      String title,
      String description,
      String severity,
      String recommendation,
      Double confidenceScore,
      String dataSource,
      LocalDate referenceDate,
      String metadata,
      Boolean isActionable,
      String createdBy) {

    AIInsight insight =
        AIInsight.builder()
            .insightType(insightType)
            .title(title)
            .description(description)
            .severity(severity)
            .recommendation(recommendation)
            .confidenceScore(confidenceScore)
            .dataSource(dataSource)
            .referenceDate(referenceDate)
            .metadata(metadata)
            .isActionable(isActionable)
            .isReviewed(false)
            .isResolved(false)
            .build();
    insight.setCreatedBy(createdBy);

    return aiInsightRepository.save(insight);
  }

  public Optional<AIInsight> findById(UUID id) {
    return aiInsightRepository.findById(id);
  }

  public List<AIInsight> findByInsightType(String insightType) {
    return aiInsightRepository.findByInsightType(insightType);
  }

  public List<AIInsight> findByStatus(InsightStatus status) {
    return aiInsightRepository.findByStatus(status);
  }

  public List<AIInsight> findBySeverity(String severity) {
    return aiInsightRepository.findBySeverity(severity);
  }

  public List<AIInsight> findByDataSource(String dataSource) {
    return aiInsightRepository.findByDataSource(dataSource);
  }

  public List<AIInsight> findByReferenceDateBetween(LocalDate startDate, LocalDate endDate) {
    return aiInsightRepository.findByReferenceDateBetween(startDate, endDate);
  }

  public List<AIInsight> findPendingInsights() {
    return aiInsightRepository.findByStatusAndIsReviewedFalse(InsightStatus.PENDING);
  }

  public List<AIInsight> findActionableInsights() {
    return aiInsightRepository.findByIsActionableTrueAndIsResolvedFalse();
  }

  public List<AIInsight> findAllInsights() {
    return aiInsightRepository.findAll();
  }

  @Transactional
  public AIInsight updateInsight(
      UUID id,
      String title,
      String description,
      String recommendation,
      String severity,
      Double confidenceScore,
      String updatedBy) {

    AIInsight insight =
        aiInsightRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("AI Insight not found: " + id));

    if (title != null) {
      insight.setTitle(title);
    }
    if (description != null) {
      insight.setDescription(description);
    }
    if (recommendation != null) {
      insight.setRecommendation(recommendation);
    }
    if (severity != null) {
      insight.setSeverity(severity);
    }
    if (confidenceScore != null) {
      insight.setConfidenceScore(confidenceScore);
    }

    insight.setUpdatedBy(updatedBy);
    return aiInsightRepository.save(insight);
  }

  @Transactional
  public void markAsReviewed(UUID id, String reviewedBy) {
    AIInsight insight =
        aiInsightRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("AI Insight not found: " + id));

    insight.markAsReviewed(reviewedBy);
    aiInsightRepository.save(insight);
  }

  @Transactional
  public void markAsResolved(UUID id, String resolvedBy) {
    AIInsight insight =
        aiInsightRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("AI Insight not found: " + id));

    insight.markAsResolved(resolvedBy);
    aiInsightRepository.save(insight);
  }

  @Transactional
  public void dismissInsight(UUID id, String dismissedBy) {
    AIInsight insight =
        aiInsightRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("AI Insight not found: " + id));

    insight.dismiss(dismissedBy);
    aiInsightRepository.save(insight);
  }

  @Transactional
  public void deleteInsight(UUID id) {
    AIInsight insight =
        aiInsightRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("AI Insight not found: " + id));

    aiInsightRepository.deleteById(id);
  }

  @Transactional
  public void generateAnomalyInsights(String generatedBy) {
    try {
      Map<String, Object> context = new HashMap<>();
      context.put("generated_by", generatedBy);
      context.put("timestamp", LocalDate.now().toString());
      context.put("analysis_type", "anomaly_detection");

      AIInsightResponse response = aiInsightClient.generateAnomalyInsight(context);

      if (response.isSuccess()) {
        AIInsight insight =
            createInsight(
                response.getInsightType(),
                response.getTitle(),
                response.getDescription(),
                response.getSeverity(),
                response.getRecommendation(),
                response.getConfidenceScore(),
                "AI_OR_RULE_BASED",
                LocalDate.now(),
                response.getMetadata() != null ? response.getMetadata().toString() : null,
                true,
                generatedBy);

        log.info("AI/Rule-based anomaly insight generated: {}", insight.getId());
      } else {
        log.warn("AI anomaly insight generation failed: {}", response.getError());
      }
    } catch (Exception e) {
      log.error("Error generating AI anomaly insights", e);
    }
  }

  @Transactional
  public void generateCashFlowForecastInsights(String generatedBy) {
    try {
      Map<String, Object> context = new HashMap<>();
      context.put("generated_by", generatedBy);
      context.put("timestamp", LocalDate.now().toString());
      context.put("analysis_type", "cash_flow_forecast");

      AIInsightResponse response = aiInsightClient.generateCashFlowForecastInsight(context);

      if (response.isSuccess()) {
        AIInsight insight =
            createInsight(
                response.getInsightType(),
                response.getTitle(),
                response.getDescription(),
                response.getSeverity(),
                response.getRecommendation(),
                response.getConfidenceScore(),
                "AI_OR_RULE_BASED",
                LocalDate.now(),
                response.getMetadata() != null ? response.getMetadata().toString() : null,
                true,
                generatedBy);

        log.info("AI/Rule-based cash flow forecast insight generated: {}", insight.getId());
      } else {
        log.warn("AI cash flow forecast insight generation failed: {}", response.getError());
      }
    } catch (Exception e) {
      log.error("Error generating AI cash flow forecast insights", e);
    }
  }

  @Transactional
  public void generateReconciliationInsights(String generatedBy) {
    try {
      Map<String, Object> context = new HashMap<>();
      context.put("generated_by", generatedBy);
      context.put("timestamp", LocalDate.now().toString());
      context.put("analysis_type", "reconciliation");

      AIInsightResponse response = aiInsightClient.generateReconciliationInsight(context);

      if (response.isSuccess()) {
        AIInsight insight =
            createInsight(
                response.getInsightType(),
                response.getTitle(),
                response.getDescription(),
                response.getSeverity(),
                response.getRecommendation(),
                response.getConfidenceScore(),
                "AI_OR_RULE_BASED",
                LocalDate.now(),
                response.getMetadata() != null ? response.getMetadata().toString() : null,
                true,
                generatedBy);

        log.info("AI/Rule-based reconciliation insight generated: {}", insight.getId());
      } else {
        log.warn("AI reconciliation insight generation failed: {}", response.getError());
      }
    } catch (Exception e) {
      log.error("Error generating AI reconciliation insights", e);
    }
  }
}
