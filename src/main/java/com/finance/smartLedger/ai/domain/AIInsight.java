package com.finance.smartLedger.ai.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
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

  @Column(name = "insight_type", nullable = false, length = 50)
  private String insightType;

  @Column(name = "title", nullable = false, length = 200)
  private String title;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "severity", length = 20)
  private String severity;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  @Builder.Default
  private InsightStatus status = InsightStatus.PENDING;

  @Column(name = "recommendation", columnDefinition = "TEXT")
  private String recommendation;

  @Column(name = "confidence_score")
  private Double confidenceScore;

  @Column(name = "data_source", length = 100)
  private String dataSource;

  @Column(name = "reference_date")
  private LocalDate referenceDate;

  @Column(name = "metadata", columnDefinition = "JSONB")
  private String metadata;

  @Column(name = "is_actionable", nullable = false)
  @Builder.Default
  private Boolean isActionable = true;

  @Column(name = "is_reviewed", nullable = false)
  @Builder.Default
  private Boolean isReviewed = false;

  @Column(name = "reviewed_by")
  private String reviewedBy;

  @Column(name = "reviewed_at")
  private LocalDate reviewedAt;

  @Column(name = "is_resolved", nullable = false)
  @Builder.Default
  private Boolean isResolved = false;

  @Column(name = "resolved_by")
  private String resolvedBy;

  @Column(name = "resolved_at")
  private LocalDate resolvedAt;

  public void markAsReviewed(String reviewedBy) {
    this.isReviewed = true;
    this.reviewedBy = reviewedBy;
    this.reviewedAt = LocalDate.now();
    this.setUpdatedBy(reviewedBy);
  }

  public void markAsResolved(String resolvedBy) {
    this.isResolved = true;
    this.resolvedBy = resolvedBy;
    this.resolvedAt = LocalDate.now();
    this.status = InsightStatus.RESOLVED;
    this.setUpdatedBy(resolvedBy);
  }

  public void dismiss(String dismissedBy) {
    this.status = InsightStatus.DISMISSED;
    this.isReviewed = true;
    this.reviewedBy = dismissedBy;
    this.reviewedAt = LocalDate.now();
    this.setUpdatedBy(dismissedBy);
  }
}
