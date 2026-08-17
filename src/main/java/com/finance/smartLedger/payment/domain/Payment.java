package com.finance.smartLedger.payment.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "payments",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = "payment_number"),
      @UniqueConstraint(columnNames = "idempotency_key")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Payment extends AuditableEntity {

  @Column(name = "payment_number", nullable = false, unique = true, length = 50)
  private String paymentNumber;

  @Column(name = "idempotency_key", unique = true, length = 100)
  private String idempotencyKey;

  @Column(name = "invoice_id")
  private UUID invoiceId;

  @Column(name = "payment_date", nullable = false)
  private LocalDateTime paymentDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 30)
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private PaymentStatus status;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "currency_code", nullable = false, length = 3)
  private String currencyCode;

  @Column(name = "payer_name", length = 100)
  private String payerName;

  @Column(name = "payer_email", length = 100)
  private String payerEmail;

  @Column(name = "payer_phone", length = 20)
  private String payerPhone;

  @Column(name = "description", length = 500)
  private String description;

  @Column(name = "gateway_transaction_id", length = 100)
  private String gatewayTransactionId;

  @Column(name = "gateway_reference", length = 100)
  private String gatewayReference;

  @Column(name = "gateway_response_code", length = 50)
  private String gatewayResponseCode;

  @Column(name = "gateway_response_message", length = 500)
  private String gatewayResponseMessage;

  @Column(name = "processed_at")
  private LocalDateTime processedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "failed_at")
  private LocalDateTime failedAt;

  @Column(name = "refunded_at")
  private LocalDateTime refundedAt;

  @Column(name = "metadata")
  private String metadata;

  @Column(name = "authorization_url", length = 500)
  private String authorizationUrl;

  @Column(name = "callback_url", length = 500)
  private String callbackUrl;

  public Payment(
      String paymentNumber,
      String idempotencyKey,
      UUID invoiceId,
      LocalDateTime paymentDate,
      PaymentMethod paymentMethod,
      BigDecimal amount,
      String currencyCode,
      String payerName,
      String payerEmail,
      String description,
      String createdBy) {
    this.paymentNumber = paymentNumber;
    this.idempotencyKey = idempotencyKey;
    this.invoiceId = invoiceId;
    this.paymentDate = paymentDate;
    this.paymentMethod = paymentMethod;
    this.status = PaymentStatus.PENDING;
    this.amount = amount;
    this.currencyCode = currencyCode;
    this.payerName = payerName;
    this.payerEmail = payerEmail;
    this.description = description;
    this.setCreatedBy(createdBy);
    this.setUpdatedBy(createdBy);
  }

  public void startProcessing(String updatedBy) {
    if (status != PaymentStatus.PENDING) {
      throw new IllegalStateException("Can only start processing payment in PENDING status");
    }
    this.status = PaymentStatus.PROCESSING;
    this.processedAt = LocalDateTime.now();
    this.setUpdatedBy(updatedBy);
  }

  public void complete(
      String gatewayTransactionId,
      String gatewayReference,
      String gatewayResponseCode,
      String gatewayResponseMessage,
      String updatedBy) {
    if (status != PaymentStatus.PROCESSING) {
      throw new IllegalStateException("Can only complete payment in PROCESSING status");
    }
    this.status = PaymentStatus.COMPLETED;
    this.gatewayTransactionId = gatewayTransactionId;
    this.gatewayReference = gatewayReference;
    this.gatewayResponseCode = gatewayResponseCode;
    this.gatewayResponseMessage = gatewayResponseMessage;
    this.completedAt = LocalDateTime.now();
    this.setUpdatedBy(updatedBy);
  }

  public void fail(String gatewayResponseCode, String gatewayResponseMessage, String updatedBy) {
    this.status = PaymentStatus.FAILED;
    this.gatewayResponseCode = gatewayResponseCode;
    this.gatewayResponseMessage = gatewayResponseMessage;
    this.failedAt = LocalDateTime.now();
    this.setUpdatedBy(updatedBy);
  }

  public void refund(String updatedBy) {
    if (status != PaymentStatus.COMPLETED) {
      throw new IllegalStateException("Can only refund completed payments");
    }
    this.status = PaymentStatus.REFUNDED;
    this.refundedAt = LocalDateTime.now();
    this.setUpdatedBy(updatedBy);
  }

  public void cancel(String updatedBy) {
    if (status != PaymentStatus.PENDING && status != PaymentStatus.PROCESSING) {
      throw new IllegalStateException("Can only cancel pending or processing payments");
    }
    this.status = PaymentStatus.CANCELLED;
    this.setUpdatedBy(updatedBy);
  }

  public boolean isCompleted() {
    return status == PaymentStatus.COMPLETED;
  }

  public boolean isFailed() {
    return status == PaymentStatus.FAILED;
  }

  public boolean isRefunded() {
    return status == PaymentStatus.REFUNDED;
  }

  public boolean canProcess() {
    return status == PaymentStatus.PENDING;
  }

  public boolean canRefund() {
    return status == PaymentStatus.COMPLETED;
  }
}
