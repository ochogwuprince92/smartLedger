package com.finance.smartLedger.fees.domain;

import com.finance.smartLedger.shared.entity.BaseEntity;
import com.finance.smartLedger.shared.valueobject.Money;
import jakarta.persistence.*;
import jakarta.persistence.AttributeOverride;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fee_schedule_items")
@Data
@NoArgsConstructor
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"feeSchedule"})
public class FeeScheduleItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fee_schedule_id", nullable = false)
  private FeeSchedule feeSchedule;

  @Enumerated(EnumType.STRING)
  @Column(name = "fee_type", nullable = false)
  private FeeType feeType;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "amount"))
  @AttributeOverride(name = "currencyCode", column = @Column(name = "currency_code"))
  private Money amount;

  @Column(name = "mandatory", nullable = false)
  private Boolean mandatory = true;

  @Column(name = "description")
  private String description;

  @Column(name = "display_order")
  private Integer displayOrder = 0;

  @Column(name = "tax_rate")
  private BigDecimal taxRate = BigDecimal.ZERO;

  @Column(name = "discount_percentage")
  private BigDecimal discountPercentage = BigDecimal.ZERO;

  public FeeScheduleItem(
      FeeSchedule feeSchedule, FeeType feeType, Money amount, Boolean mandatory) {
    this.feeSchedule = feeSchedule;
    this.feeType = feeType;
    this.amount = amount;
    this.mandatory = mandatory;
    this.description = feeType.getDisplayName();
  }

  public Money calculateTaxAmount() {
    if (taxRate == null || taxRate.compareTo(BigDecimal.ZERO) == 0) {
      return Money.zero(amount.getCurrencyCode());
    }
    BigDecimal taxAmount = amount.getAmount().multiply(taxRate).divide(BigDecimal.valueOf(100));
    return Money.of(taxAmount, amount.getCurrencyCode());
  }

  public Money calculateDiscountAmount() {
    if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) == 0) {
      return Money.zero(amount.getCurrencyCode());
    }
    BigDecimal discountAmount =
        amount.getAmount().multiply(discountPercentage).divide(BigDecimal.valueOf(100));
    return Money.of(discountAmount, amount.getCurrencyCode());
  }

  public Money calculateFinalAmount() {
    Money taxAmount = calculateTaxAmount();
    Money discountAmount = calculateDiscountAmount();
    return amount.add(taxAmount).subtract(discountAmount);
  }

  public boolean isMandatory() {
    return mandatory != null && mandatory;
  }

  public boolean isOptional() {
    return !isMandatory();
  }
}
