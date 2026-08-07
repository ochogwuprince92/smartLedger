package com.finance.smartLedger.journal.application.dto;

import com.finance.smartLedger.shared.valueobject.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Request for a journal line item")
public record JournalLineItemRequest(
    @Schema(description = "Account ID", required = true) @NotNull UUID accountId,
    @Schema(description = "Debit or Credit", example = "DEBIT", required = true) @NotNull
        DebitCreditDto debitCredit,
    @Schema(description = "Amount", required = true) @NotNull Money amount,
    @Schema(description = "Description", example = "Cash payment") String description) {}
