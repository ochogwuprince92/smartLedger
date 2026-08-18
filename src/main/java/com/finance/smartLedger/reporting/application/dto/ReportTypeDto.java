package com.finance.smartLedger.reporting.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Report type")
public enum ReportTypeDto {
  BALANCE_SHEET,
  INCOME_STATEMENT,
  CASH_FLOW_STATEMENT,
  TRIAL_BALANCE,
  CUSTOM_REPORT;

  @JsonCreator
  public static ReportTypeDto fromString(String value) {
    return ReportTypeDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.reporting.domain.ReportType toDomain() {
    return com.finance.smartLedger.reporting.domain.ReportType.valueOf(name());
  }
}
