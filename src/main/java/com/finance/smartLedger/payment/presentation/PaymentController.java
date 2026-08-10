package com.finance.smartLedger.payment.presentation;

import com.finance.smartLedger.payment.application.PaymentService;
import com.finance.smartLedger.payment.application.PaymentWebhookHandler;
import com.finance.smartLedger.payment.application.dto.*;
import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import com.finance.smartLedger.payment.domain.PaymentStatus;
import com.finance.smartLedger.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment processing endpoints")
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentWebhookHandler webhookHandler;

  @PostMapping("/payments")
  @Operation(summary = "Create payment", description = "Creates a new payment")
  @PreAuthorize("hasAuthority('PAYMENT:CREATE')")
  public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
      @RequestBody @Valid CreatePaymentRequest request) {
    Payment payment =
        paymentService.createPayment(
            request.paymentNumber(),
            UUID.randomUUID().toString(),
            request.invoiceId(),
            request.paymentDate(),
            request.paymentMethod().toDomain(),
            request.amount(),
            request.currencyCode(),
            request.payerName(),
            request.payerEmail(),
            request.payerPhone(),
            request.description(),
            "system");

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Payment created successfully", PaymentResponse.from(payment)));
  }

  @PostMapping("/payments/{id}/process")
  @Operation(summary = "Process payment", description = "Starts payment processing")
  @PreAuthorize("hasAuthority('PAYMENT:UPDATE')")
  public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
      @Parameter(description = "Payment ID") @PathVariable UUID id,
      @RequestBody @Schema(description = "User processing the payment") ActionRequest request) {
    Payment payment = paymentService.processPayment(id, request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success("Payment processing started", PaymentResponse.from(payment)));
  }

  @PostMapping("/payments/{id}/complete")
  @Operation(summary = "Complete payment", description = "Marks a payment as completed")
  @PreAuthorize("hasAuthority('PAYMENT:UPDATE')")
  public ResponseEntity<ApiResponse<PaymentResponse>> completePayment(
      @Parameter(description = "Payment ID") @PathVariable UUID id,
      @RequestBody CompletePaymentRequest request) {
    Payment payment =
        paymentService.completePayment(
            id,
            request.gatewayTransactionId(),
            request.gatewayReference(),
            request.gatewayResponseCode(),
            request.gatewayResponseMessage(),
            request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success("Payment completed successfully", PaymentResponse.from(payment)));
  }

  @PostMapping("/payments/{id}/fail")
  @Operation(summary = "Fail payment", description = "Marks a payment as failed")
  @PreAuthorize("hasAuthority('PAYMENT:UPDATE')")
  public ResponseEntity<ApiResponse<PaymentResponse>> failPayment(
      @Parameter(description = "Payment ID") @PathVariable UUID id,
      @RequestBody FailPaymentRequest request) {
    Payment payment =
        paymentService.failPayment(
            id,
            request.gatewayResponseCode(),
            request.gatewayResponseMessage(),
            request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success("Payment marked as failed", PaymentResponse.from(payment)));
  }

  @PostMapping("/payments/{id}/refund")
  @Operation(summary = "Refund payment", description = "Refunds a completed payment")
  @PreAuthorize("hasAuthority('PAYMENT:UPDATE')")
  public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
      @Parameter(description = "Payment ID") @PathVariable UUID id,
      @RequestBody @Schema(description = "User refunding the payment") ActionRequest request) {
    Payment payment = paymentService.refundPayment(id, request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success("Payment refunded successfully", PaymentResponse.from(payment)));
  }

  @PostMapping("/payments/{id}/cancel")
  @Operation(summary = "Cancel payment", description = "Cancels a pending or processing payment")
  @PreAuthorize("hasAuthority('PAYMENT:UPDATE')")
  public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
      @Parameter(description = "Payment ID") @PathVariable UUID id,
      @RequestBody @Schema(description = "User cancelling the payment") ActionRequest request) {
    Payment payment = paymentService.cancelPayment(id, request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success("Payment cancelled successfully", PaymentResponse.from(payment)));
  }

  @PostMapping("/webhook/paystack")
  @Operation(summary = "Handle Paystack webhook", description = "Handles Paystack payment gateway webhooks")
  public ResponseEntity<ApiResponse<PaymentResponse>> handleWebhook(
      @RequestHeader(value = "X-Webhook-Signature", required = false) String signature,
      @RequestBody String payload) {
    Payment payment = webhookHandler.handleWebhook("paystack", payload, signature);
    if (payment != null) {
      return ResponseEntity.ok(
          ApiResponse.success("Webhook processed successfully", PaymentResponse.from(payment)));
    } else {
      return ResponseEntity.ok(ApiResponse.success("Webhook received but payment not found", null));
    }
  }

  @GetMapping("/payments/{id}")
  @Operation(summary = "Get payment by ID", description = "Retrieves a payment by its ID")
  @PreAuthorize("hasAuthority('PAYMENT:READ')")
  public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
      @Parameter(description = "Payment ID") @PathVariable UUID id) {
    Payment payment =
        paymentService
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    return ResponseEntity.ok(
        ApiResponse.success("Payment retrieved successfully", PaymentResponse.from(payment)));
  }

  @GetMapping("/payments")
  @Operation(summary = "List payments", description = "Lists all payments with optional filters")
  @PreAuthorize("hasAuthority('PAYMENT:READ')")
  public ResponseEntity<ApiResponse<List<PaymentResponse>>> listPayments(
      @Parameter(description = "Filter by status") @RequestParam(required = false)
          PaymentStatus status,
      @Parameter(description = "Filter by payment method") @RequestParam(required = false)
          PaymentMethod paymentMethod,
      @Parameter(description = "Filter by currency code") @RequestParam(required = false)
          String currencyCode,
      @Parameter(description = "Filter by payer email") @RequestParam(required = false)
          String payerEmail,
      @Parameter(description = "Filter by start date") @RequestParam(required = false)
          LocalDateTime startDate,
      @Parameter(description = "Filter by end date") @RequestParam(required = false)
          LocalDateTime endDate) {
    List<Payment> payments;

    if (status != null) {
      payments = paymentService.findByStatus(status);
    } else if (paymentMethod != null) {
      payments = paymentService.findByPaymentMethod(paymentMethod);
    } else if (currencyCode != null) {
      payments = paymentService.findByCurrencyCode(currencyCode);
    } else if (payerEmail != null) {
      payments = paymentService.findByPayerEmail(payerEmail);
    } else if (startDate != null && endDate != null) {
      payments = paymentService.findByPaymentDateBetween(startDate, endDate);
    } else {
      payments = paymentService.findByStatus(PaymentStatus.PENDING);
    }

    List<PaymentResponse> responses =
        payments.stream().map(PaymentResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @DeleteMapping("/payments/{id}")
  @Operation(
      summary = "Delete payment",
      description = "Deletes a payment that is not currently processing")
  @PreAuthorize("hasAuthority('PAYMENT:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deletePayment(
      @Parameter(description = "Payment ID") @PathVariable UUID id) {
    paymentService.deletePayment(id);
    return ResponseEntity.ok(ApiResponse.success("Payment deleted successfully", null));
  }

  public record ActionRequest(
      @Schema(description = "User performing the action") String updatedBy) {}

  public record CompletePaymentRequest(
      @Schema(description = "Gateway transaction ID") String gatewayTransactionId,
      @Schema(description = "Gateway reference") String gatewayReference,
      @Schema(description = "Gateway response code") String gatewayResponseCode,
      @Schema(description = "Gateway response message") String gatewayResponseMessage,
      @Schema(description = "User completing the payment") String updatedBy) {}

  public record FailPaymentRequest(
      @Schema(description = "Gateway response code") String gatewayResponseCode,
      @Schema(description = "Gateway response message") String gatewayResponseMessage,
      @Schema(description = "User failing the payment") String updatedBy) {}
}
