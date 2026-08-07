package com.finance.smartLedger.ledger.application.dto;

import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Account response model")
public record AccountResponse(
    @Schema(description = "Account ID") UUID id,
    @Schema(description = "Account number") String accountNumber,
    @Schema(description = "Account code") String accountCode,
    @Schema(description = "Account name") String accountName,
    @Schema(description = "Account type") AccountType accountType,
    @Schema(description = "Account balance") BalanceResponse balance,
    @Schema(description = "Account description") String description,
    @Schema(description = "Account active status") Boolean isActive,
    @Schema(description = "Parent account ID") UUID parentAccountId,
    @Schema(description = "User who created the account") String createdBy,
    @Schema(description = "User who last updated the account") String updatedBy) {

  public static AccountResponse from(Account account) {
    return new AccountResponse(
        account.getId(),
        account.getAccountNumber().getValue(),
        account.getAccountCode().getValue(),
        account.getAccountName(),
        account.getAccountType(),
        BalanceResponse.from(account.getBalance().getCurrentBalance()),
        account.getDescription(),
        account.getIsActive(),
        account.getParentAccountId(),
        account.getCreatedBy(),
        account.getUpdatedBy());
  }
}
