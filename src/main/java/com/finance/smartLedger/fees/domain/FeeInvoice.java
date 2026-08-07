package com.finance.smartLedger.fees.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import com.finance.smartLedger.shared.valueobject.Money;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "fee_invoices",
    indexes = {
      @Index(name = "idx_invoice_student_id", columnList = "student_id"),
      @Index(name = "idx_invoice_term", columnList = "academic_term"),
      @Index(name = "idx_invoice_status", columnList = "status"),
      @Index(name = "idx_invoice_due_date", columnList = "due_date")
    })
@Data
@NoArgsConstructor
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"lineItems", "payments"})
public class FeeInvoice extends AuditableEntity {

  @Column(name = "invoice_number", nullable = false, unique = true)
  private String invoiceNumber;

  @Column(name = "student_id", nullable = false)
  private UUID studentId;

  @Column(name = "academic_year")
  private String academicYear;

  @Column(name = "academic_term")
  private String academicTerm;

  @Column(name = "issue_date", nullable = false)
  private LocalDate issueDate;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "subtotal"))
  @AttributeOverride(name = "currencyCode", column = @Column(name = "subtotal_currency_code"))
  private Money subtotal;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "tax_amount"))
  @AttributeOverride(name = "currencyCode", column = @Column(name = "tax_amount_currency_code"))
  private Money taxAmount;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "discount_amount"))
  @AttributeOverride(
      name = "currencyCode",
      column = @Column(name = "discount_amount_currency_code"))
  private Money discountAmount;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "total_amount"))
  @AttributeOverride(name = "currencyCode", column = @Column(name = "total_amount_currency_code"))
  private Money totalAmount;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "paid_amount"))
  @AttributeOverride(name = "currencyCode", column = @Column(name = "paid_amount_currency_code"))
  private Money paidAmount;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "balance_amount"))
  @AttributeOverride(name = "currencyCode", column = @Column(name = "balance_amount_currency_code"))
  private Money balanceAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private InvoiceStatus status;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "generated_by")
  private String generatedBy;

  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<FeeInvoiceLineItem> lineItems = new HashSet<>();

  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<FeePayment> payments = new HashSet<>();

  public FeeInvoice(UUID studentId, String invoiceNumber, LocalDate dueDate) {
    this.studentId = studentId;
    this.invoiceNumber = invoiceNumber;
    this.issueDate = LocalDate.now();
    this.dueDate = dueDate;
    this.status = InvoiceStatus.DRAFT;
    this.subtotal = Money.zero("USD");
    this.taxAmount = Money.zero("USD");
    this.discountAmount = Money.zero("USD");
    this.totalAmount = Money.zero("USD");
    this.paidAmount = Money.zero("USD");
    this.balanceAmount = Money.zero("USD");
  }

  public void addLineItem(FeeType feeType, Money amount, String description) {
    FeeInvoiceLineItem lineItem = new FeeInvoiceLineItem(this, feeType, amount, description);
    lineItems.add(lineItem);
    recalculateTotals();
  }

  public void removeLineItem(FeeInvoiceLineItem lineItem) {
    lineItems.remove(lineItem);
    recalculateTotals();
  }

  public void applyDiscount(Money discountAmount, String reason) {
    this.discountAmount = discountAmount;
    this.notes = (this.notes != null ? this.notes + "\n" : "") + "Discount: " + reason;
    recalculateTotals();
  }

  public void recalculateTotals() {
    Money newSubtotal = Money.zero("USD");
    for (FeeInvoiceLineItem item : lineItems) {
      newSubtotal = newSubtotal.add(item.getAmount());
    }
    this.subtotal = newSubtotal;

    Money newTotal = subtotal.subtract(discountAmount).add(taxAmount);
    this.totalAmount = newTotal;

    Money newPaidAmount = Money.zero("USD");
    for (FeePayment payment : payments) {
      if (payment.isCompleted()) {
        newPaidAmount = newPaidAmount.add(payment.getAmount());
      }
    }
    this.paidAmount = newPaidAmount;

    this.balanceAmount = totalAmount.subtract(paidAmount);

    updateStatus();
  }

  private void updateStatus() {
    if (balanceAmount.isZero()
        || balanceAmount.getAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
      this.status = InvoiceStatus.PAID;
    } else if (paidAmount.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
      this.status = InvoiceStatus.PARTIALLY_PAID;
    } else if (LocalDate.now().isAfter(dueDate)) {
      this.status = InvoiceStatus.OVERDUE;
    } else {
      this.status = InvoiceStatus.ISSUED;
    }
  }

  public void markAsIssued() {
    if (status == InvoiceStatus.DRAFT) {
      this.status = InvoiceStatus.ISSUED;
    }
  }

  public void markAsCancelled(String reason) {
    this.status = InvoiceStatus.CANCELLED;
    this.notes = (this.notes != null ? this.notes + "\n" : "") + "Cancelled: " + reason;
  }

  public boolean isPaid() {
    return status == InvoiceStatus.PAID;
  }

  public boolean isOverdue() {
    return status == InvoiceStatus.OVERDUE;
  }

  public boolean isPartiallyPaid() {
    return status == InvoiceStatus.PARTIALLY_PAID;
  }

  public boolean isDraft() {
    return status == InvoiceStatus.DRAFT;
  }

  public boolean isIssued() {
    return status == InvoiceStatus.ISSUED;
  }

  public boolean isCancelled() {
    return status == InvoiceStatus.CANCELLED;
  }

  public enum InvoiceStatus {
    DRAFT,
    ISSUED,
    PARTIALLY_PAID,
    PAID,
    OVERDUE,
    CANCELLED,
    WRITTEN_OFF
  }
}
