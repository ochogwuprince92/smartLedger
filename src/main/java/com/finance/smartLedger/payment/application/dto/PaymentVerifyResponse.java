package com.finance.smartLedger.payment.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Response for payment verification from payment gateway")
public record PaymentVerifyResponse(
    @Schema(description = "Verification status") boolean status,
    @Schema(description = "Verification message") String message,
    @Schema(description = "Payment verification data") PaymentData data) {

  @Schema(description = "Payment verification data details")
  public record PaymentData(
      @Schema(description = "Payment reference") String reference,
      @Schema(description = "Gateway response") String gatewayResponse,
      @Schema(description = "Payment date and time") LocalDateTime paidAt,
      @Schema(description = "Transaction creation date and time") LocalDateTime createdAt,
      @Schema(description = "Payment channel") String channel,
      @Schema(description = "Currency code") String currency,
      @Schema(description = "Payment amount") String amount,
      @Schema(description = "Additional metadata") Map<String, Object> metadata,
      @Schema(description = "Customer information") Customer customer,
      @Schema(description = "Authorization information") Authorization authorization) {

    @Schema(description = "Customer details")
    public record Customer(
        @Schema(description = "Customer email") String email,
        @Schema(description = "Customer code") String customerCode) {}

    @Schema(description = "Authorization details")
    public record Authorization(
        @Schema(description = "Authorization code") String authorizationCode,
        @Schema(description = "Card BIN") String bin,
        @Schema(description = "Last 4 digits of card") String last4,
        @Schema(description = "Card expiry month") String expMonth,
        @Schema(description = "Card expiry year") String expYear,
        @Schema(description = "Card type") String cardType,
        @Schema(description = "Issuing bank") String bank) {}
  }
}
