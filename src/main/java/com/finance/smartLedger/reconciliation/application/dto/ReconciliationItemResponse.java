package com.finance.smartLedger.reconciliation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response for a reconciliation item")
public record ReconciliationItemResponse(
    @Schema(description = "Item ID") UUID id,
    @Schema(description = "Reconciliation ID") UUID reconciliationId,
    @Schema(description = "Item reference") String itemReference,
    @Schema(description = "Item type") String itemType,
    @Schema(description = "Expected amount") BigDecimal expectedAmount,
    @Schema(description = "Actual amount") BigDecimal actualAmount,
    @Schema(description = "Variance amount") BigDecimal varianceAmount,
    @Schema(description = "Match status") MatchStatusDto matchStatus,
    @Schema(description = "Matched transaction ID") UUID matchedTransactionId,
    @Schema(description = "Matched at") LocalDateTime matchedAt,
    @Schema(description = "Description") String description,
    @Schema(description = "Created at") LocalDateTime createdAt,
    @Schema(description = "Updated at") LocalDateTime updatedAt) {

  public static ReconciliationItemResponse from(
      com.finance.smartLedger.reconciliation.domain.ReconciliationItem item) {
    return new ReconciliationItemResponse(
        item.getId(),
        item.getReconciliationId(),
        item.getItemReference(),
        item.getItemType(),
        item.getExpectedAmount(),
        item.getActualAmount(),
        item.getVarianceAmount(),
        MatchStatusDto.valueOf(item.getMatchStatus().name()),
        item.getMatchedTransactionId(),
        item.getMatchedAt(),
        item.getDescription(),
        item.getCreatedAt(),
        item.getUpdatedAt());
  }
}
