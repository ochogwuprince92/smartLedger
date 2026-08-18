package com.finance.smartLedger.reconciliation.presentation;

import com.finance.smartLedger.reconciliation.application.ReconciliationService;
import com.finance.smartLedger.reconciliation.application.dto.*;
import com.finance.smartLedger.reconciliation.domain.Reconciliation;
import com.finance.smartLedger.reconciliation.domain.ReconciliationStatus;
import com.finance.smartLedger.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reconciliation")
@RequiredArgsConstructor
@Tag(name = "Reconciliation", description = "Reconciliation management endpoints")
public class ReconciliationController {

  private final ReconciliationService reconciliationService;

  @PostMapping("/reconciliations")
  @Operation(summary = "Create reconciliation", description = "Creates a new reconciliation")
  @PreAuthorize("hasAuthority('RECONCILIATION:CREATE')")
  public ResponseEntity<ApiResponse<ReconciliationResponse>> createReconciliation(
      @RequestBody @Valid CreateReconciliationRequest request) {
    Reconciliation reconciliation =
        reconciliationService.createReconciliation(
            request.reconciliationNumber(),
            request.reconciliationDate(),
            request.sourceSystem(),
            request.sourceReference(),
            request.totalExpectedAmount(),
            request.description(),
            "system");

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Reconciliation created successfully",
                ReconciliationResponse.from(reconciliation)));
  }

  @PostMapping("/reconciliations/{id}/start")
  @Operation(summary = "Start reconciliation", description = "Starts a reconciliation process")
  @PreAuthorize("hasAuthority('RECONCILIATION:UPDATE')")
  public ResponseEntity<ApiResponse<ReconciliationResponse>> startReconciliation(
      @Parameter(description = "Reconciliation ID") @PathVariable UUID id,
      @RequestBody @Schema(description = "User starting the reconciliation")
          ActionRequest request) {
    Reconciliation reconciliation =
        reconciliationService.startReconciliation(id, request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Reconciliation started successfully", ReconciliationResponse.from(reconciliation)));
  }

  @PostMapping("/reconciliations/{id}/complete")
  @Operation(summary = "Complete reconciliation", description = "Completes a reconciliation")
  @PreAuthorize("hasAuthority('RECONCILIATION:UPDATE')")
  public ResponseEntity<ApiResponse<ReconciliationResponse>> completeReconciliation(
      @Parameter(description = "Reconciliation ID") @PathVariable UUID id,
      @RequestBody @Schema(description = "User completing the reconciliation")
          ActionRequest request) {
    Reconciliation reconciliation =
        reconciliationService.completeReconciliation(id, request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Reconciliation completed successfully", ReconciliationResponse.from(reconciliation)));
  }

  @PostMapping("/reconciliations/{id}/fail")
  @Operation(summary = "Fail reconciliation", description = "Marks a reconciliation as failed")
  @PreAuthorize("hasAuthority('RECONCILIATION:UPDATE')")
  public ResponseEntity<ApiResponse<ReconciliationResponse>> failReconciliation(
      @Parameter(description = "Reconciliation ID") @PathVariable UUID id,
      @RequestBody @Schema(description = "User failing the reconciliation") ActionRequest request) {
    Reconciliation reconciliation =
        reconciliationService.failReconciliation(id, request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Reconciliation marked as failed", ReconciliationResponse.from(reconciliation)));
  }

  @PostMapping("/reconciliations/{id}/items")
  @Operation(summary = "Add reconciliation item", description = "Adds an item to a reconciliation")
  @PreAuthorize("hasAuthority('RECONCILIATION:UPDATE')")
  public ResponseEntity<ApiResponse<ReconciliationItemResponse>> addItem(
      @Parameter(description = "Reconciliation ID") @PathVariable UUID id,
      @RequestBody @Valid ReconciliationItemRequest request) {
    var item =
        reconciliationService.addItem(
            id,
            request.itemReference(),
            request.itemType(),
            request.expectedAmount(),
            request.description(),
            "system");

    return ResponseEntity.ok(
        ApiResponse.success("Item added successfully", ReconciliationItemResponse.from(item)));
  }

  @PostMapping("/items/{itemId}/match")
  @Operation(
      summary = "Match reconciliation item",
      description = "Matches a reconciliation item with a transaction")
  @PreAuthorize("hasAuthority('RECONCILIATION:UPDATE')")
  public ResponseEntity<ApiResponse<ReconciliationItemResponse>> matchItem(
      @Parameter(description = "Item ID") @PathVariable UUID itemId,
      @RequestBody MatchItemRequest request) {
    var item =
        reconciliationService.matchItem(
            itemId, request.transactionId(), request.actualAmount(), request.updatedBy());

    return ResponseEntity.ok(
        ApiResponse.success("Item matched successfully", ReconciliationItemResponse.from(item)));
  }

  @PostMapping("/items/{itemId}/suspense")
  @Operation(
      summary = "Move item to suspense",
      description = "Moves an unmatched item to suspense account")
  @PreAuthorize("hasAuthority('RECONCILIATION:UPDATE')")
  public ResponseEntity<ApiResponse<ReconciliationItemResponse>> moveToSuspense(
      @Parameter(description = "Item ID") @PathVariable UUID itemId,
      @RequestBody SuspenseRequest request) {
    var item =
        reconciliationService.moveToSuspense(
            itemId, request.suspenseAccountId(), request.updatedBy());

    return ResponseEntity.ok(
        ApiResponse.success("Item moved to suspense", ReconciliationItemResponse.from(item)));
  }

  @GetMapping("/reconciliations/{id}")
  @Operation(
      summary = "Get reconciliation by ID",
      description = "Retrieves a reconciliation by its ID")
  @PreAuthorize("hasAuthority('RECONCILIATION:READ')")
  public ResponseEntity<ApiResponse<ReconciliationResponse>> getReconciliation(
      @Parameter(description = "Reconciliation ID") @PathVariable UUID id) {
    Reconciliation reconciliation =
        reconciliationService
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));
    return ResponseEntity.ok(
        ApiResponse.success(
            "Reconciliation retrieved successfully", ReconciliationResponse.from(reconciliation)));
  }

  @GetMapping("/reconciliations")
  @Operation(
      summary = "List reconciliations",
      description = "Lists all reconciliations with optional filters")
  @PreAuthorize("hasAuthority('RECONCILIATION:READ')")
  public ResponseEntity<ApiResponse<List<ReconciliationResponse>>> listReconciliations(
      @Parameter(description = "Filter by status") @RequestParam(required = false)
          ReconciliationStatus status,
      @Parameter(description = "Filter by source system") @RequestParam(required = false)
          String sourceSystem,
      @Parameter(description = "Filter by start date") @RequestParam(required = false)
          LocalDateTime startDate,
      @Parameter(description = "Filter by end date") @RequestParam(required = false)
          LocalDateTime endDate) {
    List<Reconciliation> reconciliations;

    if (status != null) {
      reconciliations = reconciliationService.findByStatus(status);
    } else if (sourceSystem != null) {
      reconciliations = reconciliationService.findBySourceSystem(sourceSystem);
    } else if (startDate != null && endDate != null) {
      reconciliations = reconciliationService.findByReconciliationDateBetween(startDate, endDate);
    } else {
      reconciliations = reconciliationService.findByStatus(ReconciliationStatus.PENDING);
    }

    List<ReconciliationResponse> responses =
        reconciliations.stream().map(ReconciliationResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/reconciliations/{id}/items")
  @Operation(
      summary = "Get reconciliation items",
      description = "Retrieves all items for a reconciliation")
  @PreAuthorize("hasAuthority('RECONCILIATION:READ')")
  public ResponseEntity<ApiResponse<List<ReconciliationItemResponse>>> getReconciliationItems(
      @Parameter(description = "Reconciliation ID") @PathVariable UUID id) {
    List<ReconciliationItemResponse> items =
        reconciliationService.findItemsByReconciliationId(id).stream()
            .map(ReconciliationItemResponse::from)
            .collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success(items));
  }

  @DeleteMapping("/reconciliations/{id}")
  @Operation(
      summary = "Delete reconciliation",
      description = "Deletes an uncompleted reconciliation")
  @PreAuthorize("hasAuthority('RECONCILIATION:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteReconciliation(
      @Parameter(description = "Reconciliation ID") @PathVariable UUID id) {
    reconciliationService.deleteReconciliation(id);
    return ResponseEntity.ok(ApiResponse.success("Reconciliation deleted successfully", null));
  }

  public record ActionRequest(
      @Schema(description = "User performing the action") String updatedBy) {}

  public record MatchItemRequest(
      @Schema(description = "Transaction ID") UUID transactionId,
      @Schema(description = "Actual amount") BigDecimal actualAmount,
      @Schema(description = "User matching the item") String updatedBy) {}

  public record SuspenseRequest(
      @Schema(description = "Suspense account ID") UUID suspenseAccountId,
      @Schema(description = "User moving to suspense") String updatedBy) {}
}
