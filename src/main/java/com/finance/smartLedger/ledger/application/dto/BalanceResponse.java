package com.finance.smartLedger.ledger.application.dto;

import com.finance.smartLedger.shared.valueobject.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Balance response model")
public record BalanceResponse(
    @Schema(description = "Balance amount") BigDecimal amount,
    @Schema(description = "Currency code") String currencyCode) {

  public static BalanceResponse from(Money money) {
    return new BalanceResponse(money.getAmount(), money.getCurrencyCode());
  }
}
