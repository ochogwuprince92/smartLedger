package com.finance.smartLedger.ai.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_insights")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AIInsight extends AuditableEntity {

  @Column(name = "request_id", unique = true, nullable = false)
  private String requestId;

  @Column(name = "reconciliation_id")
  private UUID reconciliationId;

  @Enumerated(EnumType.STRING)
  @Column(name = "insight_type", nullable = false, length = 50)
  private AIInsightType insightType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  @Builder.Default
  private InsightStatus status = InsightStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(name = "risk_level", length = 20)
  private RiskLevel riskLevel;

  @Column(name = "summary", columnDefinition = "TEXT")
  private String summary;

  @Column(name = "root_cause", columnDefinition = "TEXT")
  private String rootCause;

  @Column(name = "recommendations", columnDefinition = "JSONB")
  private String recommendations;

  @Column(name = "metadata", columnDefinition = "JSONB")
  private String metadata;

  @Column(name = "anomaly_count")
  private Integer anomalyCount;

  @Column(name = "requested_at")
  private LocalDateTime requestedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "failure_reason", columnDefinition = "TEXT")
  private String failureReason;

  @Column(name = "retry_count")
  @Builder.Default
  private Integer retryCount = 0;

  @Column(name = "max_retries")
  @Builder.Default
  private Integer maxRetries = 3;

  public void markAsProcessing() {
    this.status = InsightStatus.PROCESSING;
    this.setUpdatedBy("SYSTEM");
  }

  public void markAsCompleted(RiskLevel riskLevel, String summary, String rootCause, String recommendations) {
    this.status = InsightStatus.COMPLETED;
    this.riskLevel = riskLevel;
    this.summary = summary;
    this.rootCause = rootCause;
    this.recommendations = recommendations;
    this.completedAt = LocalDateTime.now();
    this.setUpdatedBy("SYSTEM");
  }

  public void markAsFailed(String failureReason) {
    this.status = InsightStatus.FAILED;
    this.failureReason = failureReason;
    this.completedAt = LocalDateTime.now();
    this.setUpdatedBy("SYSTEM");
  }

  public void incrementRetryCount() {
    this.retryCount++;
    this.setUpdatedBy("SYSTEM");
  }

  public boolean canRetry() {
    return this.status == InsightStatus.FAILED && this.retryCount < this.maxRetries;
  }
}
