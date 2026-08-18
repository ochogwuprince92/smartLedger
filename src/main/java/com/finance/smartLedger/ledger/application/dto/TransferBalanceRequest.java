package com.finance.smartLedger.ledger.application.dto;

import com.finance.smartLedger.shared.valueobject.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Request to transfer balance between accounts")
public record TransferBalanceRequest(
    @Schema(description = "Source account ID", required = true) @NotNull UUID fromAccountId,
    @Schema(description = "Destination account ID", required = true) @NotNull UUID toAccountId,
    @Schema(description = "Amount to transfer", required = true) @NotNull Money amount,
    @Schema(description = "Reference for the transfer") String reference,
    @Schema(description = "User who initiated the transfer", required = true) @NotBlank
        String updatedBy) {}
