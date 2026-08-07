package com.finance.smartLedger.reconciliation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Schema(description = "Response for a reconciliation")
public record ReconciliationResponse(
    @Schema(description = "Reconciliation ID") UUID id,
    @Schema(description = "Reconciliation number") String reconciliationNumber,
    @Schema(description = "Reconciliation date") LocalDateTime reconciliationDate,
    @Schema(description = "Source system") String sourceSystem,
    @Schema(description = "Source reference") String sourceReference,
    @Schema(description = "Status") ReconciliationStatusDto status,
    @Schema(description = "Total expected amount") BigDecimal totalExpectedAmount,
    @Schema(description = "Total actual amount") BigDecimal totalActualAmount,
    @Schema(description = "Variance amount") BigDecimal varianceAmount,
    @Schema(description = "Suspense account ID") UUID suspenseAccountId,
    @Schema(description = "Description") String description,
    @Schema(description = "Completed at") LocalDateTime completedAt,
    @Schema(description = "Items") List<ReconciliationItemResponse> items,
    @Schema(description = "Created at") LocalDateTime createdAt,
    @Schema(description = "Updated at") LocalDateTime updatedAt) {

  public static ReconciliationResponse from(
      com.finance.smartLedger.reconciliation.domain.Reconciliation reconciliation) {
    List<ReconciliationItemResponse> itemResponses =
        reconciliation.getItems().stream()
            .map(ReconciliationItemResponse::from)
            .collect(Collectors.toList());

    return new ReconciliationResponse(
        reconciliation.getId(),
        reconciliation.getReconciliationNumber(),
        reconciliation.getReconciliationDate(),
        reconciliation.getSourceSystem(),
        reconciliation.getSourceReference(),
        ReconciliationStatusDto.valueOf(reconciliation.getStatus().name()),
        reconciliation.getTotalExpectedAmount(),
        reconciliation.getTotalActualAmount(),
        reconciliation.getVarianceAmount(),
        reconciliation.getSuspenseAccountId(),
        reconciliation.getDescription(),
        reconciliation.getCompletedAt(),
        itemResponses,
        reconciliation.getCreatedAt(),
        reconciliation.getUpdatedAt());
  }
}
