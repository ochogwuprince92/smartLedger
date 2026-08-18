package com.finance.smartLedger.reporting.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Request to create a report")
public record CreateReportRequest(
    @Schema(description = "Report number", example = "RPT-2024-001", required = true) @NotBlank
        String reportNumber,
    @Schema(description = "Report date", required = true) @NotNull LocalDateTime reportDate,
    @Schema(description = "Report type", required = true) @NotNull ReportTypeDto reportType,
    @Schema(description = "Period start date") LocalDateTime periodStartDate,
    @Schema(description = "Period end date") LocalDateTime periodEndDate,
    @Schema(description = "Currency code", example = "USD", required = true) @NotBlank
        String currencyCode,
    @Schema(description = "Description", example = "Monthly balance sheet") String description) {}
