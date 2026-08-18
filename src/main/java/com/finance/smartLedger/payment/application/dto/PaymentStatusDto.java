package com.finance.smartLedger.payment.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payment status")
public enum PaymentStatusDto {
  PENDING,
  PROCESSING,
  COMPLETED,
  FAILED,
  REFUNDED,
  CANCELLED;

  @JsonCreator
  public static PaymentStatusDto fromString(String value) {
    return PaymentStatusDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.payment.domain.PaymentStatus toDomain() {
    return com.finance.smartLedger.payment.domain.PaymentStatus.valueOf(name());
  }
}
