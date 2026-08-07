package com.finance.smartLedger.reporting.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Report extends AuditableEntity {

  @Column(name = "report_number", nullable = false, unique = true, length = 50)
  private String reportNumber;

  @Column(name = "report_date", nullable = false)
  private LocalDateTime reportDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "report_type", nullable = false, length = 30)
  private ReportType reportType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private ReportStatus status;

  @Column(name = "period_start_date")
  private LocalDateTime periodStartDate;

  @Column(name = "period_end_date")
  private LocalDateTime periodEndDate;

  @Column(name = "currency_code", nullable = false, length = 3)
  private String currencyCode;

  @Column(name = "generated_at")
  private LocalDateTime generatedAt;

  @Column(name = "file_path", length = 500)
  private String filePath;

  @Column(name = "error_message", length = 1000)
  private String errorMessage;

  @Column(name = "description", length = 500)
  private String description;

  @Lob
  @Column(name = "report_data")
  private String reportData;

  public Report(
      String reportNumber,
      LocalDateTime reportDate,
      ReportType reportType,
      LocalDateTime periodStartDate,
      LocalDateTime periodEndDate,
      String currencyCode,
      String description,
      String createdBy) {
    this.reportNumber = reportNumber;
    this.reportDate = reportDate;
    this.reportType = reportType;
    this.status = ReportStatus.PENDING;
    this.periodStartDate = periodStartDate;
    this.periodEndDate = periodEndDate;
    this.currencyCode = currencyCode;
    this.description = description;
    this.setCreatedBy(createdBy);
    this.setUpdatedBy(createdBy);
  }

  public void startGenerating(String updatedBy) {
    if (status != ReportStatus.PENDING) {
      throw new IllegalStateException("Can only start generating report in PENDING status");
    }
    this.status = ReportStatus.GENERATING;
    this.setUpdatedBy(updatedBy);
  }

  public void complete(String filePath, String reportData, String updatedBy) {
    if (status != ReportStatus.GENERATING) {
      throw new IllegalStateException("Can only complete report in GENERATING status");
    }
    this.status = ReportStatus.COMPLETED;
    this.generatedAt = LocalDateTime.now();
    this.filePath = filePath;
    this.reportData = reportData;
    this.setUpdatedBy(updatedBy);
  }

  public void fail(String errorMessage, String updatedBy) {
    this.status = ReportStatus.FAILED;
    this.errorMessage = errorMessage;
    this.setUpdatedBy(updatedBy);
  }

  public boolean isCompleted() {
    return status == ReportStatus.COMPLETED;
  }

  public boolean isFailed() {
    return status == ReportStatus.FAILED;
  }
}
