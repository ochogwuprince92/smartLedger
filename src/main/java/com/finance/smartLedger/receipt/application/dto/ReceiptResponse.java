package com.finance.smartLedger.receipt.application.dto;

import com.finance.smartLedger.receipt.domain.Receipt;
import com.finance.smartLedger.receipt.domain.ReceiptStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response for a receipt")
public record ReceiptResponse(
    @Schema(description = "Receipt ID") UUID id,
    @Schema(description = "Receipt number") String receiptNumber,
    @Schema(description = "Payment ID") UUID paymentId,
    @Schema(description = "Receipt date") LocalDateTime receiptDate,
    @Schema(description = "Status") ReceiptStatus status,
    @Schema(description = "Amount") BigDecimal amount,
    @Schema(description = "Currency code") String currencyCode,
    @Schema(description = "Payer name") String payerName,
    @Schema(description = "Payer email") String payerEmail,
    @Schema(description = "Payer phone") String payerPhone,
    @Schema(description = "Description") String description,
    @Schema(description = "Payment method") String paymentMethod,
    @Schema(description = "Payment reference") String paymentReference,
    @Schema(description = "Sent at") LocalDateTime sentAt,
    @Schema(description = "Delivered at") LocalDateTime deliveredAt,
    @Schema(description = "Generated file path") String generatedFilePath,
    @Schema(description = "Metadata") String metadata,
    @Schema(description = "Created at") LocalDateTime createdAt,
    @Schema(description = "Updated at") LocalDateTime updatedAt) {

  public static ReceiptResponse from(Receipt receipt) {
    return new ReceiptResponse(
        receipt.getId(),
        receipt.getReceiptNumber(),
        receipt.getPaymentId(),
        receipt.getReceiptDate(),
        receipt.getStatus(),
        receipt.getAmount(),
        receipt.getCurrencyCode(),
        receipt.getPayerName(),
        receipt.getPayerEmail(),
        receipt.getPayerPhone(),
        receipt.getDescription(),
        receipt.getPaymentMethod(),
        receipt.getPaymentReference(),
        receipt.getSentAt(),
        receipt.getDeliveredAt(),
        receipt.getGeneratedFilePath(),
        receipt.getMetadata(),
        receipt.getCreatedAt(),
        receipt.getUpdatedAt());
  }
}
