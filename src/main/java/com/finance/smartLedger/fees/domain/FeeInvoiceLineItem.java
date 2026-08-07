package com.finance.smartLedger.fees.domain;

import com.finance.smartLedger.shared.entity.BaseEntity;
import com.finance.smartLedger.shared.valueobject.Money;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fee_invoice_line_items")
@Data
@NoArgsConstructor
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"invoice"})
public class FeeInvoiceLineItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", nullable = false)
  private FeeInvoice invoice;

  @Enumerated(EnumType.STRING)
  @Column(name = "fee_type", nullable = false)
  private FeeType feeType;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "amount"))
  @AttributeOverride(name = "currencyCode", column = @Column(name = "currency_code"))
  private Money amount;

  @Column(name = "description")
  private String description;

  @Column(name = "quantity", nullable = false)
  private Integer quantity = 1;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "line_total"))
  @AttributeOverride(name = "currencyCode", column = @Column(name = "line_total_currency_code"))
  private Money lineTotal;

  public FeeInvoiceLineItem(FeeInvoice invoice, FeeType feeType, Money amount, String description) {
    this.invoice = invoice;
    this.feeType = feeType;
    this.amount = amount;
    this.description = description;
    this.quantity = 1;
    this.lineTotal = amount;
  }

  public FeeInvoiceLineItem(
      FeeInvoice invoice, FeeType feeType, Money amount, String description, Integer quantity) {
    this.invoice = invoice;
    this.feeType = feeType;
    this.amount = amount;
    this.description = description;
    this.quantity = quantity;
    this.lineTotal = amount.multiply(BigDecimal.valueOf(quantity));
  }

  public void updateQuantity(Integer newQuantity) {
    this.quantity = newQuantity;
    this.lineTotal = amount.multiply(BigDecimal.valueOf(newQuantity));
  }

  public void updateAmount(Money newAmount) {
    this.amount = newAmount;
    this.lineTotal = newAmount.multiply(BigDecimal.valueOf(quantity));
  }
}
