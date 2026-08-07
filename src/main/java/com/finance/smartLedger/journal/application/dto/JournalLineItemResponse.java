package com.finance.smartLedger.journal.application.dto;

import com.finance.smartLedger.shared.valueobject.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response for a journal line item")
public record JournalLineItemResponse(
    @Schema(description = "Line item ID") UUID id,
    @Schema(description = "Journal entry ID") UUID journalEntryId,
    @Schema(description = "Account ID") UUID accountId,
    @Schema(description = "Account number") String accountNumber,
    @Schema(description = "Account name") String accountName,
    @Schema(description = "Debit or Credit") DebitCreditDto debitCredit,
    @Schema(description = "Amount") Money amount,
    @Schema(description = "Description") String description,
    @Schema(description = "Sequence number") Integer sequenceNumber,
    @Schema(description = "Created at") LocalDateTime createdAt,
    @Schema(description = "Updated at") LocalDateTime updatedAt) {

  public static JournalLineItemResponse from(
      com.finance.smartLedger.journal.domain.JournalLineItem lineItem) {
    return new JournalLineItemResponse(
        lineItem.getId(),
        lineItem.getJournalEntryId(),
        lineItem.getAccountId(),
        lineItem.getAccountNumber(),
        lineItem.getAccountName(),
        DebitCreditDto.valueOf(lineItem.getDebitCredit().name()),
        lineItem.getAmount(),
        lineItem.getDescription(),
        lineItem.getSequenceNumber(),
        lineItem.getCreatedAt(),
        lineItem.getUpdatedAt());
  }
}
