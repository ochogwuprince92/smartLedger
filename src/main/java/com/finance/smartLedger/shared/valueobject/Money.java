package com.finance.smartLedger.shared.valueobject;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Money {

  private BigDecimal amount;
  private String currencyCode;

  private static void validate(BigDecimal amount, String currencyCode) {
    if (amount == null) {
      throw new IllegalArgumentException("Amount cannot be null");
    }
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }
    if (currencyCode == null || currencyCode.trim().isEmpty()) {
      throw new IllegalArgumentException("Currency code cannot be null or empty");
    }
    try {
      Currency.getInstance(currencyCode);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid currency code: " + currencyCode);
    }
  }

  public Money add(Money other) {
    if (!currencyCode.equals(other.currencyCode)) {
      throw new IllegalArgumentException("Cannot add money with different currencies");
    }
    return new Money(amount.add(other.amount), currencyCode);
  }

  public Money subtract(Money other) {
    if (!currencyCode.equals(other.currencyCode)) {
      throw new IllegalArgumentException("Cannot subtract money with different currencies");
    }
    return new Money(amount.subtract(other.amount), currencyCode);
  }

  public Money multiply(BigDecimal multiplier) {
    return new Money(amount.multiply(multiplier), currencyCode);
  }

  public Money divide(BigDecimal divisor) {
    return new Money(amount.divide(divisor, 2, RoundingMode.HALF_UP), currencyCode);
  }

  public boolean isGreaterThan(Money other) {
    if (!currencyCode.equals(other.currencyCode)) {
      throw new IllegalArgumentException("Cannot compare money with different currencies");
    }
    return amount.compareTo(other.amount) > 0;
  }

  public boolean isLessThan(Money other) {
    if (!currencyCode.equals(other.currencyCode)) {
      throw new IllegalArgumentException("Cannot compare money with different currencies");
    }
    return amount.compareTo(other.amount) < 0;
  }

  public boolean isZero() {
    return amount.compareTo(BigDecimal.ZERO) == 0;
  }

  public boolean isPositive() {
    return amount.compareTo(BigDecimal.ZERO) > 0;
  }

  public boolean isNegative() {
    return amount.compareTo(BigDecimal.ZERO) < 0;
  }

  public static Money zero(String currencyCode) {
    validate(BigDecimal.ZERO, currencyCode);
    return new Money(BigDecimal.ZERO, currencyCode);
  }

  public static Money of(BigDecimal amount, String currencyCode) {
    validate(amount, currencyCode);
    return new Money(amount, currencyCode);
  }

  public static Money of(double amount, String currencyCode) {
    BigDecimal bdAmount = BigDecimal.valueOf(amount);
    validate(bdAmount, currencyCode);
    return new Money(bdAmount, currencyCode);
  }

  @Override
  public String toString() {
    return amount + " " + currencyCode;
  }
}
