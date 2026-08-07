package com.finance.smartLedger.receipt.domain;

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
@Table(name = "receipts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Receipt extends AuditableEntity {

  @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
  private String receiptNumber;

  @Column(name = "payment_id", nullable = false)
  private UUID paymentId;

  @Column(name = "receipt_date", nullable = false)
  private LocalDateTime receiptDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private ReceiptStatus status;

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

  @Column(name = "payment_method", length = 30)
  private String paymentMethod;

  @Column(name = "payment_reference", length = 100)
  private String paymentReference;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  @Column(name = "delivered_at")
  private LocalDateTime deliveredAt;

  @Column(name = "generated_file_path", length = 500)
  private String generatedFilePath;

  @Column(name = "metadata")
  private String metadata;

  public Receipt(
      String receiptNumber,
      UUID paymentId,
      LocalDateTime receiptDate,
      BigDecimal amount,
      String currencyCode,
      String payerName,
      String payerEmail,
      String payerPhone,
      String description,
      String paymentMethod,
      String paymentReference,
      String createdBy) {
    this.receiptNumber = receiptNumber;
    this.paymentId = paymentId;
    this.receiptDate = receiptDate;
    this.status = ReceiptStatus.GENERATED;
    this.amount = amount;
    this.currencyCode = currencyCode;
    this.payerName = payerName;
    this.payerEmail = payerEmail;
    this.payerPhone = payerPhone;
    this.description = description;
    this.paymentMethod = paymentMethod;
    this.paymentReference = paymentReference;
    this.setCreatedBy(createdBy);
    this.setUpdatedBy(createdBy);
  }

  public void markAsSent() {
    if (status != ReceiptStatus.GENERATED) {
      throw new IllegalStateException("Can only mark generated receipts as sent");
    }
    this.status = ReceiptStatus.SENT;
    this.sentAt = LocalDateTime.now();
  }

  public void markAsDelivered() {
    if (status != ReceiptStatus.SENT) {
      throw new IllegalStateException("Can only mark sent receipts as delivered");
    }
    this.status = ReceiptStatus.DELIVERED;
    this.deliveredAt = LocalDateTime.now();
  }

  public void markAsFailed(String reason) {
    this.status = ReceiptStatus.FAILED;
    this.description =
        (this.description != null ? this.description + " - " : "") + "Failed: " + reason;
  }

  public void cancel() {
    if (status == ReceiptStatus.DELIVERED) {
      throw new IllegalStateException("Cannot cancel a delivered receipt");
    }
    this.status = ReceiptStatus.CANCELLED;
  }
}
