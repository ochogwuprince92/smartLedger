package com.finance.smartLedger.ledger.application.dto;

import com.finance.smartLedger.shared.valueobject.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to reconcile account balance")
public record ReconcileBalanceRequest(
    @Schema(description = "Expected balance after reconciliation", required = true) @NotNull
        Money expectedBalance,
    @Schema(description = "Reason for reconciliation") String reason,
    @Schema(description = "User who reconciled the balance", required = true) @NotBlank
        String updatedBy) {}
