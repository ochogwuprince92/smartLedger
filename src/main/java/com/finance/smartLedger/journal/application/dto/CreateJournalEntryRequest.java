package com.finance.smartLedger.journal.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Request to create a journal entry")
public record CreateJournalEntryRequest(
    @Schema(description = "Journal entry number", example = "JE-2024-001", required = true)
        @NotBlank
        String entryNumber,
    @Schema(description = "Entry date", required = true) @NotNull LocalDateTime entryDate,
    @Schema(description = "Entry type", example = "MANUAL", required = true) @NotNull
        JournalEntryTypeDto entryType,
    @Schema(description = "Reference number", example = "INV-001") String referenceNumber,
    @Schema(description = "Description", example = "Payment received", required = true) @NotBlank
        String description,
    @Schema(description = "Line items for the journal entry", required = true) @NotNull
        List<JournalLineItemRequest> lineItems) {}
