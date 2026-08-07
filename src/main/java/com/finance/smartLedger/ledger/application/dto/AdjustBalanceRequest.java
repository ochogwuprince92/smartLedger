package com.finance.smartLedger.ledger.application.dto;

import com.finance.smartLedger.shared.valueobject.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to adjust account balance")
public record AdjustBalanceRequest(
    @Schema(description = "Adjustment amount", required = true) @NotNull Money amount,
    @Schema(description = "Reason for adjustment") String reason,
    @Schema(description = "User who adjusted the balance", required = true) @NotBlank
        String updatedBy) {}
