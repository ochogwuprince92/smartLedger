package com.finance.smartLedger.reporting.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response for a report")
public record ReportResponse(
    @Schema(description = "Report ID") UUID id,
    @Schema(description = "Report number") String reportNumber,
    @Schema(description = "Report date") LocalDateTime reportDate,
    @Schema(description = "Report type") ReportTypeDto reportType,
    @Schema(description = "Status") ReportStatusDto status,
    @Schema(description = "Period start date") LocalDateTime periodStartDate,
    @Schema(description = "Period end date") LocalDateTime periodEndDate,
    @Schema(description = "Currency code") String currencyCode,
    @Schema(description = "Generated at") LocalDateTime generatedAt,
    @Schema(description = "File path") String filePath,
    @Schema(description = "Error message") String errorMessage,
    @Schema(description = "Description") String description,
    @Schema(description = "Report data") String reportData,
    @Schema(description = "Created at") LocalDateTime createdAt,
    @Schema(description = "Updated at") LocalDateTime updatedAt) {

  public static ReportResponse from(com.finance.smartLedger.reporting.domain.Report report) {
    return new ReportResponse(
        report.getId(),
        report.getReportNumber(),
        report.getReportDate(),
        ReportTypeDto.valueOf(report.getReportType().name()),
        ReportStatusDto.valueOf(report.getStatus().name()),
        report.getPeriodStartDate(),
        report.getPeriodEndDate(),
        report.getCurrencyCode(),
        report.getGeneratedAt(),
        report.getFilePath(),
        report.getErrorMessage(),
        report.getDescription(),
        report.getReportData(),
        report.getCreatedAt(),
        report.getUpdatedAt());
  }
}
