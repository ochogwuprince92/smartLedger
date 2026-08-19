package com.finance.smartLedger.shared.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@io.swagger.v3.oas.annotations.media.Schema(description = "Money value object representing amount and currency")
public class Money {

  @io.swagger.v3.oas.annotations.media.Schema(description = "Monetary amount", example = "1000.00", required = true)
  private BigDecimal amount;
  
  @io.swagger.v3.oas.annotations.media.Schema(description = "Currency code (ISO 4217)", example = "USD", required = true)
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

  @JsonCreator
  public static Money fromJson(
      @JsonProperty("amount") Object amount,
      @JsonProperty("currencyCode") String currencyCode) {
    if (amount == null && currencyCode == null) {
      return zero("USD");
    }
    
    BigDecimal bdAmount;
    if (amount instanceof Number) {
      bdAmount = BigDecimal.valueOf(((Number) amount).doubleValue());
    } else if (amount instanceof BigDecimal) {
      bdAmount = (BigDecimal) amount;
    } else if (amount instanceof String) {
      bdAmount = new BigDecimal((String) amount);
    } else {
      throw new IllegalArgumentException("Invalid amount type: " + amount.getClass());
    }
    
    if (currencyCode == null || currencyCode.trim().isEmpty()) {
      currencyCode = "USD";
    }
    
    return of(bdAmount, currencyCode);
  }

  @Override
  public String toString() {
    return amount + " " + currencyCode;
  }
}
