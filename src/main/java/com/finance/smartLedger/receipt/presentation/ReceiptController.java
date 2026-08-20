package com.finance.smartLedger.receipt.presentation;

import com.finance.smartLedger.receipt.application.ReceiptPdfService;
import com.finance.smartLedger.receipt.application.ReceiptService;
import com.finance.smartLedger.receipt.application.dto.ReceiptResponse;
import com.finance.smartLedger.receipt.domain.Receipt;
import com.finance.smartLedger.receipt.domain.ReceiptStatus;
import com.finance.smartLedger.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipt")
@RequiredArgsConstructor
@Tag(name = "Receipt", description = "Receipt management endpoints")
public class ReceiptController {

  private final ReceiptService receiptService;
  private final ReceiptPdfService receiptPdfService;

  @PostMapping("/receipts")
  @Operation(
      summary = "Generate receipt",
      description = "Generates a receipt for a completed payment")
  @PreAuthorize("hasAuthority('RECEIPT:CREATE')")
  public ResponseEntity<ApiResponse<ReceiptResponse>> generateReceipt(
      @RequestBody @Valid GenerateReceiptRequest request) {
    Receipt receipt = receiptService.generateReceipt(request.paymentId(), request.createdBy());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Receipt generated successfully", ReceiptResponse.from(receipt)));
  }

  @PostMapping("/receipts/{id}/send")
  @Operation(summary = "Mark receipt as sent", description = "Marks a receipt as sent")
  @PreAuthorize("hasAuthority('RECEIPT:UPDATE')")
  public ResponseEntity<ApiResponse<ReceiptResponse>> markAsSent(
      @Parameter(description = "Receipt ID") @PathVariable UUID id,
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "User marking the receipt as sent",
          content = @Content(schema = @Schema(implementation = ActionRequest.class)))
      ActionRequest request) {
    Receipt receipt = receiptService.markAsSent(id);
    return ResponseEntity.ok(
        ApiResponse.success("Receipt marked as sent", ReceiptResponse.from(receipt)));
  }

  @PostMapping("/receipts/{id}/deliver")
  @Operation(summary = "Mark receipt as delivered", description = "Marks a receipt as delivered")
  @PreAuthorize("hasAuthority('RECEIPT:UPDATE')")
  public ResponseEntity<ApiResponse<ReceiptResponse>> markAsDelivered(
      @Parameter(description = "Receipt ID") @PathVariable UUID id,
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "User marking the receipt as delivered",
          content = @Content(schema = @Schema(implementation = ActionRequest.class)))
      ActionRequest request) {
    Receipt receipt = receiptService.markAsDelivered(id);
    return ResponseEntity.ok(
        ApiResponse.success("Receipt marked as delivered", ReceiptResponse.from(receipt)));
  }

  @PostMapping("/receipts/{id}/fail")
  @Operation(summary = "Mark receipt as failed", description = "Marks a receipt as failed")
  @PreAuthorize("hasAuthority('RECEIPT:UPDATE')")
  public ResponseEntity<ApiResponse<ReceiptResponse>> markAsFailed(
      @Parameter(description = "Receipt ID") @PathVariable UUID id,
      @RequestBody FailReceiptRequest request) {
    Receipt receipt = receiptService.markAsFailed(id, request.reason());
    return ResponseEntity.ok(
        ApiResponse.success("Receipt marked as failed", ReceiptResponse.from(receipt)));
  }

  @PostMapping("/receipts/{id}/cancel")
  @Operation(summary = "Cancel receipt", description = "Cancels a receipt")
  @PreAuthorize("hasAuthority('RECEIPT:UPDATE')")
  public ResponseEntity<ApiResponse<ReceiptResponse>> cancelReceipt(
      @Parameter(description = "Receipt ID") @PathVariable UUID id,
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "User cancelling the receipt",
          content = @Content(schema = @Schema(implementation = ActionRequest.class)))
      ActionRequest request) {
    Receipt receipt = receiptService.cancelReceipt(id);
    return ResponseEntity.ok(
        ApiResponse.success("Receipt cancelled successfully", ReceiptResponse.from(receipt)));
  }

  @GetMapping("/receipts/{id}")
  @Operation(summary = "Get receipt by ID", description = "Retrieves a receipt by its ID")
  @PreAuthorize("hasAuthority('RECEIPT:READ')")
  public ResponseEntity<ApiResponse<ReceiptResponse>> getReceipt(
      @Parameter(description = "Receipt ID") @PathVariable UUID id) {
    Receipt receipt =
        receiptService
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));
    return ResponseEntity.ok(
        ApiResponse.success("Receipt retrieved successfully", ReceiptResponse.from(receipt)));
  }

  @GetMapping("/receipts")
  @Operation(summary = "List receipts", description = "Lists all receipts with optional filters")
  @PreAuthorize("hasAuthority('RECEIPT:READ')")
  public ResponseEntity<ApiResponse<List<ReceiptResponse>>> listReceipts(
      @Parameter(description = "Filter by status") @RequestParam(required = false)
          ReceiptStatus status,
      @Parameter(description = "Filter by payer email") @RequestParam(required = false)
          String payerEmail,
      @Parameter(description = "Filter by start date") @RequestParam(required = false)
          LocalDateTime startDate,
      @Parameter(description = "Filter by end date") @RequestParam(required = false)
          LocalDateTime endDate) {
    List<Receipt> receipts;

    if (status != null) {
      receipts = receiptService.findByStatus(status);
    } else if (payerEmail != null) {
      receipts = receiptService.findByPayerEmail(payerEmail);
    } else if (startDate != null && endDate != null) {
      receipts = receiptService.findByReceiptDateBetween(startDate, endDate);
    } else {
      receipts = receiptService.findByStatus(ReceiptStatus.GENERATED);
    }

    List<ReceiptResponse> responses =
        receipts.stream().map(ReceiptResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/receipts/by-payment/{paymentId}")
  @Operation(
      summary = "Get receipt by payment ID",
      description = "Retrieves a receipt by payment ID")
  @PreAuthorize("hasAuthority('RECEIPT:READ')")
  public ResponseEntity<ApiResponse<ReceiptResponse>> getReceiptByPaymentId(
      @Parameter(description = "Payment ID") @PathVariable UUID paymentId) {
    Receipt receipt =
        receiptService
            .findByPaymentId(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Receipt not found for this payment"));
    return ResponseEntity.ok(
        ApiResponse.success("Receipt retrieved successfully", ReceiptResponse.from(receipt)));
  }

  @DeleteMapping("/receipts/{id}")
  @Operation(summary = "Delete receipt", description = "Deletes a receipt that is not delivered")
  @PreAuthorize("hasAuthority('RECEIPT:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteReceipt(
      @Parameter(description = "Receipt ID") @PathVariable UUID id) {
    receiptService.deleteReceipt(id);
    return ResponseEntity.ok(ApiResponse.success("Receipt deleted successfully", null));
  }

  @GetMapping("/receipts/{id}/pdf")
  @Operation(
      summary = "Generate receipt PDF",
      description = "Generates a PDF document for the receipt")
  @PreAuthorize("hasAuthority('RECEIPT:READ')")
  public ResponseEntity<byte[]> generateReceiptPdf(
      @Parameter(description = "Receipt ID") @PathVariable UUID id) {
    byte[] pdfBytes = receiptPdfService.generateReceiptPdf(id);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", "receipt_" + id + ".pdf");

    return ResponseEntity.ok().headers(headers).body(pdfBytes);
  }

  public record ActionRequest(
      @Schema(description = "User performing the action") String updatedBy) {}

  public record GenerateReceiptRequest(
      @Schema(description = "Payment ID") UUID paymentId,
      @Schema(description = "User generating the receipt") String createdBy) {}

  public record FailReceiptRequest(
      @Schema(description = "Reason for failure") String reason,
      @Schema(description = "User marking the receipt as failed") String updatedBy) {}
}
