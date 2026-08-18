package com.finance.smartLedger.reporting.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Report status")
public enum ReportStatusDto {
  PENDING,
  GENERATING,
  COMPLETED,
  FAILED;

  @JsonCreator
  public static ReportStatusDto fromString(String value) {
    return ReportStatusDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.reporting.domain.ReportStatus toDomain() {
    return com.finance.smartLedger.reporting.domain.ReportStatus.valueOf(name());
  }
}
