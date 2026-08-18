package com.finance.smartLedger.reconciliation.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Reconciliation status")
public enum ReconciliationStatusDto {
  PENDING,
  IN_PROGRESS,
  COMPLETED,
  FAILED,
  PARTIALLY_MATCHED;

  @JsonCreator
  public static ReconciliationStatusDto fromString(String value) {
    return ReconciliationStatusDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.reconciliation.domain.ReconciliationStatus toDomain() {
    return com.finance.smartLedger.reconciliation.domain.ReconciliationStatus.valueOf(name());
  }
}
