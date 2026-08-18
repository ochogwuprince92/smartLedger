package com.finance.smartLedger.payment.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response for a payment")
public record PaymentResponse(
    @Schema(description = "Payment ID") UUID id,
    @Schema(description = "Payment number") String paymentNumber,
    @Schema(description = "Payment date") LocalDateTime paymentDate,
    @Schema(description = "Payment method") PaymentMethodDto paymentMethod,
    @Schema(description = "Status") PaymentStatusDto status,
    @Schema(description = "Amount") BigDecimal amount,
    @Schema(description = "Currency code") String currencyCode,
    @Schema(description = "Payer name") String payerName,
    @Schema(description = "Payer email") String payerEmail,
    @Schema(description = "Payer phone") String payerPhone,
    @Schema(description = "Description") String description,
    @Schema(description = "Gateway transaction ID") String gatewayTransactionId,
    @Schema(description = "Gateway reference") String gatewayReference,
    @Schema(description = "Gateway response code") String gatewayResponseCode,
    @Schema(description = "Gateway response message") String gatewayResponseMessage,
    @Schema(description = "Authorization URL for payment redirect") String authorizationUrl,
    @Schema(description = "Callback URL") String callbackUrl,
    @Schema(description = "Processed at") LocalDateTime processedAt,
    @Schema(description = "Completed at") LocalDateTime completedAt,
    @Schema(description = "Failed at") LocalDateTime failedAt,
    @Schema(description = "Refunded at") LocalDateTime refundedAt,
    @Schema(description = "Metadata") String metadata,
    @Schema(description = "Created at") LocalDateTime createdAt,
    @Schema(description = "Updated at") LocalDateTime updatedAt) {

  public static PaymentResponse from(com.finance.smartLedger.payment.domain.Payment payment) {
    return new PaymentResponse(
        payment.getId(),
        payment.getPaymentNumber(),
        payment.getPaymentDate(),
        PaymentMethodDto.valueOf(payment.getPaymentMethod().name()),
        PaymentStatusDto.valueOf(payment.getStatus().name()),
        payment.getAmount(),
        payment.getCurrencyCode(),
        payment.getPayerName(),
        payment.getPayerEmail(),
        payment.getPayerPhone(),
        payment.getDescription(),
        payment.getGatewayTransactionId(),
        payment.getGatewayReference(),
        payment.getGatewayResponseCode(),
        payment.getGatewayResponseMessage(),
        payment.getAuthorizationUrl(),
        payment.getCallbackUrl(),
        payment.getProcessedAt(),
        payment.getCompletedAt(),
        payment.getFailedAt(),
        payment.getRefundedAt(),
        payment.getMetadata(),
        payment.getCreatedAt(),
        payment.getUpdatedAt());
  }
}
