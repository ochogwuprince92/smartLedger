package com.finance.smartLedger.receipt.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Receipt status")
public enum ReceiptStatusDto {
  GENERATED,
  SENT,
  DELIVERED,
  FAILED,
  CANCELLED;

  @JsonCreator
  public static ReceiptStatusDto fromString(String value) {
    return ReceiptStatusDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.receipt.domain.ReceiptStatus toDomain() {
    return com.finance.smartLedger.receipt.domain.ReceiptStatus.valueOf(name());
  }
}
