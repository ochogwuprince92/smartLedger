package com.finance.smartLedger.payment.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payment method")
public enum PaymentMethodDto {
  CREDIT_CARD,
  DEBIT_CARD,
  BANK_TRANSFER,
  MOBILE_MONEY,
  CRYPTO,
  CHECK,
  CASH;

  @JsonCreator
  public static PaymentMethodDto fromString(String value) {
    return PaymentMethodDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.payment.domain.PaymentMethod toDomain() {
    return com.finance.smartLedger.payment.domain.PaymentMethod.valueOf(name());
  }
}
