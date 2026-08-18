package com.finance.smartLedger.journal.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Account balance in trial balance")
public record TrialBalanceAccountDto(
    @Schema(description = "Account ID") UUID accountId,
    @Schema(description = "Account number") String accountNumber,
    @Schema(description = "Account name") String accountName,
    @Schema(description = "Account type") String accountType,
    @Schema(description = "Debit balance") BigDecimal debitBalance,
    @Schema(description = "Credit balance") BigDecimal creditBalance,
    @Schema(description = "Currency") String currency) {}
