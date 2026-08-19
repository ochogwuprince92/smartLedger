package com.finance.smartLedger.ledger.application.dto;

import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.shared.valueobject.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to create a new account")
public record CreateAccountRequest(
    @Schema(description = "Account number", example = "ACC001", required = true) @NotBlank
        String accountNumber,
    @Schema(description = "Account code", example = "1001", required = true) @NotBlank
        String accountCode,
    @Schema(description = "Account name", example = "Cash Account", required = true) @NotBlank
        String accountName,
    @Schema(description = "Account type", example = "ASSET", required = true, allowableValues = {"ASSET", "LIABILITY", "EQUITY", "REVENUE", "EXPENSE"}) @NotNull AccountType accountType,
    @Schema(description = "Initial balance", required = true) @NotNull Money initialBalance,
    @Schema(description = "User who created the account", example = "admin", required = true) @NotBlank
        String createdBy) {}
