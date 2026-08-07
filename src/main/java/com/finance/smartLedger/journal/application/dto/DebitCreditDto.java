package com.finance.smartLedger.journal.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Debit or Credit indicator")
public enum DebitCreditDto {
  DEBIT,
  CREDIT;

  @JsonCreator
  public static DebitCreditDto fromString(String value) {
    return DebitCreditDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.journal.domain.DebitCredit toDomain() {
    return com.finance.smartLedger.journal.domain.DebitCredit.valueOf(name());
  }
}
