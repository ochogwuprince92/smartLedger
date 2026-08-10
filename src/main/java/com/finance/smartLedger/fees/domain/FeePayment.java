package com.finance.smartLedger.fees.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import com.finance.smartLedger.shared.valueobject.Money;
import jakarta.persistence.*;
import jakarta.persistence.AttributeOverride;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "fee_payments",
    indexes = {
      @Index(name = "idx_payment_student_id", columnList = "student_id"),
      @Index(name = "idx_payment_invoice_id", columnList = "invoice_id"),
      @Index(name = "idx_payment_date", columnList = "payment_date"),
      @Index(name = "idx_payment_status", columnList = "status")
    })
@Data
@NoArgsConstructor
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"invoice"})
public class FeePayment extends AuditableEntity {

  @Column(name = "student_id", nullable = false)
  private UUID studentId;

  @Column(name = "invoice_id")
  private UUID invoiceId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", insertable = false, updatable = false)
  private FeeInvoice invoice;

  @Column(name = "source_payment_id")
  private UUID sourcePaymentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "fee_type", nullable = false)
  private FeeType feeType;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "amount"))
  @AttributeOverride(name = "currencyCode", column = @Column(name = "currency_code"))
  private Money amount;

  @Column(name = "payment_date", nullable = false)
  private LocalDateTime paymentDate;

  @Column(name = "payment_method")
  private String paymentMethod;

  @Column(name = "reference_number")
  private String referenceNumber;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PaymentStatus status;

  @Column(name = "processed_by")
  private String processedBy;

  @Column(name = "receipt_number")
  private String receiptNumber;

  public FeePayment(UUID studentId, FeeType feeType, Money amount) {
    this.studentId = studentId;
    this.feeType = feeType;
    this.amount = amount;
    this.paymentDate = LocalDateTime.now();
    this.status = PaymentStatus.PENDING;
  }

  public FeePayment(
      UUID studentId,
      UUID invoiceId,
      FeeType feeType,
      Money amount,
      String paymentMethod,
      String referenceNumber) {
    this(studentId, feeType, amount);
    this.invoiceId = invoiceId;
    this.paymentMethod = paymentMethod;
    this.referenceNumber = referenceNumber;
  }

  public void markAsCompleted(String receiptNumber, String processedBy) {
    this.status = PaymentStatus.COMPLETED;
    this.receiptNumber = receiptNumber;
    this.processedBy = processedBy;
  }

  public void markAsFailed(String reason) {
    this.status = PaymentStatus.FAILED;
    this.description = reason;
  }

  public void markAsRefunded(String reason) {
    this.status = PaymentStatus.REFUNDED;
    this.description = reason;
  }

  public boolean isCompleted() {
    return status == PaymentStatus.COMPLETED;
  }

  public boolean isPending() {
    return status == PaymentStatus.PENDING;
  }

  public boolean isFailed() {
    return status == PaymentStatus.FAILED;
  }

  public boolean isRefunded() {
    return status == PaymentStatus.REFUNDED;
  }

  public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
  }
}
