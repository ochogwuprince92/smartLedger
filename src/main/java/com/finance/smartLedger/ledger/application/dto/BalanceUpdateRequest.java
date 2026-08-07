package com.finance.smartLedger.ledger.application.dto;

import com.finance.smartLedger.shared.valueobject.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to update account balance")
public record BalanceUpdateRequest(
    @Schema(description = "Balance operation (DEBIT or CREDIT)", required = true) @NotNull
        BalanceOperation operation,
    @Schema(description = "Amount to debit or credit", required = true) @NotNull Money amount,
    @Schema(description = "User who updated the balance", required = true) @NotBlank
        String updatedBy) {

  @Schema(description = "Balance operation type")
  public enum BalanceOperation {
    DEBIT,
    CREDIT
  }
}
