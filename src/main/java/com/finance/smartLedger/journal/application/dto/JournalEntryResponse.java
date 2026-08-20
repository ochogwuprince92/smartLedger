package com.finance.smartLedger.journal.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Schema(description = "Response for a journal entry")
public record JournalEntryResponse(
    @Schema(description = "Journal entry ID") UUID id,
    @Schema(description = "Entry number") String entryNumber,
    @Schema(description = "Entry date") LocalDateTime entryDate,
    @Schema(description = "Entry type") JournalEntryTypeDto entryType,
    @Schema(description = "Reference number") String referenceNumber,
    @Schema(description = "Description") String description,
    @Schema(description = "Posted status") Boolean posted,
    @Schema(description = "Posted date") LocalDateTime postedDate,
    @Schema(description = "Posted by") String postedBy,
    @Schema(description = "Line items") List<JournalLineItemResponse> lineItems,
    @Schema(description = "Total debits") BigDecimal totalDebits,
    @Schema(description = "Total credits") BigDecimal totalCredits,
    @Schema(description = "Created at") LocalDateTime createdAt,
    @Schema(description = "Updated at") LocalDateTime updatedAt) {

  public static JournalEntryResponse from(
      com.finance.smartLedger.journal.domain.JournalEntry journalEntry) {
    List<JournalLineItemResponse> lineItemResponses =
        journalEntry.getLineItems() != null
            ? journalEntry.getLineItems().stream()
                .map(JournalLineItemResponse::from)
                .collect(Collectors.toList())
            : List.of();

    return new JournalEntryResponse(
        journalEntry.getId(),
        journalEntry.getEntryNumber(),
        journalEntry.getEntryDate(),
        JournalEntryTypeDto.valueOf(journalEntry.getEntryType().name()),
        journalEntry.getReferenceNumber(),
        journalEntry.getDescription(),
        journalEntry.getPosted(),
        journalEntry.getPostedDate(),
        journalEntry.getPostedBy(),
        lineItemResponses,
        journalEntry.getTotalDebits(),
        journalEntry.getTotalCredits(),
        journalEntry.getCreatedAt(),
        journalEntry.getUpdatedAt());
  }
}
