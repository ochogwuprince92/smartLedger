package com.finance.smartLedger.payment.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Request to create a payment")
public record CreatePaymentRequest(
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
    @Schema(description = "Payer email", example = "john@example.com") String payerEmail,
    @Schema(description = "Payer phone", example = "+1234567890") String payerPhone,
    @Schema(description = "Description", example = "Payment for invoice #123")
        String description,
    @Schema(description = "Callback URL for payment gateway redirect", example = "https://example.com/payment/callback")
        String callbackUrl) {}
