package com.finance.smartLedger.reconciliation.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Match status")
public enum MatchStatusDto {
  UNMATCHED,
  MATCHED,
  SUSPENSE,
  VARIANCE;

  @JsonCreator
  public static MatchStatusDto fromString(String value) {
    return MatchStatusDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.reconciliation.domain.MatchStatus toDomain() {
    return com.finance.smartLedger.reconciliation.domain.MatchStatus.valueOf(name());
  }
}
