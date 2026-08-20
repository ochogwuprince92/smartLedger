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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "Payment processing endpoints")
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentWebhookHandler webhookHandler;

  @PostMapping("/payments")
  @Operation(summary = "Create payment", description = "Creates a new payment")
  @PreAuthorize("hasAuthority('PAYMENT:CREATE')")
  public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @RequestBody @Valid CreatePaymentRequest request) {
    Payment payment =
        paymentService.createPayment(
            request.paymentNumber(),
            idempotencyKey,
            request.invoiceId(),
            request.paymentDate(),
            request.paymentMethod().toDomain(),
            request.amount(),
            request.currencyCode(),
            request.payerName(),
            request.payerEmail(),
            request.payerPhone(),
            request.description(),
            request.callbackUrl(),
            "system");

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Payment created successfully", PaymentResponse.from(payment)));
  }

  @PostMapping("/payments/{id}/process")
  @Operation(summary = "Process payment", description = "Starts payment processing")
  @PreAuthorize("hasAuthority('PAYMENT:UPDATE')")
  public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
      @Parameter(description = "Payment ID") @PathVariable UUID id,
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "User processing the payment",
          content = @Content(schema = @Schema(implementation = ActionRequest.class)))
      ActionRequest request) {
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
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "User refunding the payment",
          content = @Content(schema = @Schema(implementation = ActionRequest.class)))
      ActionRequest request) {
    Payment payment = paymentService.refundPayment(id, request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success("Payment refunded successfully", PaymentResponse.from(payment)));
  }

  @PostMapping("/payments/{id}/cancel")
  @Operation(summary = "Cancel payment", description = "Cancels a pending or processing payment")
  @PreAuthorize("hasAuthority('PAYMENT:UPDATE')")
  public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
      @Parameter(description = "Payment ID") @PathVariable UUID id,
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "User cancelling the payment",
          content = @Content(schema = @Schema(implementation = ActionRequest.class)))
      ActionRequest request) {
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

  @GetMapping("/callback/paystack")
  @Operation(summary = "Handle Paystack callback", description = "Handles Paystack payment callback after user completes payment and triggers verification")
  public ResponseEntity<ApiResponse<Map<String, Object>>> handleCallback(
      @RequestParam(required = false) String reference,
      @RequestParam(required = false) String trxref) {
    Map<String, Object> response = new HashMap<>();
    String paymentReference = reference != null ? reference : trxref;
    
    if (paymentReference != null) {
      try {
        // Trigger payment verification and completion
        paymentService.verifyPayment(paymentReference, "callback");
        
        response.put("reference", paymentReference);
        response.put("message", "Payment verified and processed successfully");
        response.put("status", "verified");
        return ResponseEntity.ok(ApiResponse.success("Payment verified", response));
      } catch (IllegalArgumentException e) {
        // Payment not found - still acknowledge callback
        response.put("reference", paymentReference);
        response.put("message", "Payment callback received but payment not found. Status will be updated via webhook.");
        response.put("status", "pending_webhook");
        return ResponseEntity.ok(ApiResponse.success("Callback received", response));
      } catch (Exception e) {
        log.error("Error processing payment callback for reference: {}", paymentReference, e);
        response.put("reference", paymentReference);
        response.put("message", "Payment callback received but verification failed. Status will be updated via webhook.");
        response.put("status", "verification_failed");
        return ResponseEntity.ok(ApiResponse.success("Callback received with error", response));
      }
    } else {
      response.put("message", "No payment reference provided");
      return ResponseEntity.ok(ApiResponse.success("Callback received without reference", response));
    }
  }

  @GetMapping("/verify/{reference}")
  @Operation(summary = "Verify payment", description = "Verifies a payment with the gateway using its reference and updates payment status")
  @PreAuthorize("hasAuthority('PAYMENT:READ')")
  public ResponseEntity<ApiResponse<com.finance.smartLedger.payment.application.dto.PaymentVerifyResponse>> verifyPayment(
      @Parameter(description = "Payment reference from gateway") @PathVariable String reference) {
    com.finance.smartLedger.payment.application.dto.PaymentVerifyResponse verificationResponse =
        paymentService.verifyPayment(reference, "api");
    return ResponseEntity.ok(ApiResponse.success("Payment verified", verificationResponse));
  }

  @PostMapping("/initiate-gateway-payment")
  @Operation(summary = "Initiate gateway payment", description = "Initiates a payment using payment gateway and returns authorization URL for redirect")
  @PreAuthorize("hasAuthority('PAYMENT:CREATE')")
  public ResponseEntity<ApiResponse<PaymentResponse>> initiateGatewayPayment(
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @RequestBody @Valid GatewayPaymentRequest request) {
    Payment payment =
        paymentService.createPayment(
            request.paymentNumber(),
            idempotencyKey,
            request.invoiceId(),
            request.paymentDate(),
            request.paymentMethod().toDomain(),
            request.amount(),
            request.currencyCode(),
            request.payerName(),
            request.payerEmail(),
            request.payerPhone(),
            request.description(),
            request.callbackUrl(),
            "api");

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Payment initiated successfully.Redirect to authorizationUrl to complete payment.", PaymentResponse.from(payment)));
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

  public record GatewayPaymentRequest(
      @Schema(description = "Payment number", example = "PAY-2024-001", required = true) @NotBlank
          String paymentNumber,
      @Schema(description = "Invoice ID (optional, for fee payments)") UUID invoiceId,
      @Schema(description = "Payment date", required = true) @NotNull LocalDateTime paymentDate,
      @Schema(description = "Payment method", required = true) @NotNull
          PaymentMethodDto paymentMethod,
      @Schema(description = "Amount", required = true) @NotNull @Positive BigDecimal amount,
      @Schema(description = "Currency code", example = "USD", required = true) @NotBlank
          String currencyCode,
      @Schema(description = "Payer name", example = "John Doe") String payerName,
      @Schema(description = "Payer email", example = "john@example.com") @NotBlank String payerEmail,
      @Schema(description = "Payer phone", example = "+1234567890") String payerPhone,
      @Schema(description = "Description", example = "Payment for invoice #123")
          String description,
      @Schema(description = "Callback URL for payment gateway redirect", example = "https://example.com/payment/callback")
          @NotBlank String callbackUrl) {}
}
