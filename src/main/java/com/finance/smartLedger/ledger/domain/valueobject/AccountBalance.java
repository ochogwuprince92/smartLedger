package com.finance.smartLedger.ledger.domain.valueobject;

import com.finance.smartLedger.shared.valueobject.Money;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalance {

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "current_balance")),
    @AttributeOverride(name = "currencyCode", column = @Column(name = "current_balance_currency"))
  })
  private Money currentBalance;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "debit_balance")),
    @AttributeOverride(name = "currencyCode", column = @Column(name = "debit_balance_currency"))
  })
  private Money debitBalance;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "credit_balance")),
    @AttributeOverride(name = "currencyCode", column = @Column(name = "credit_balance_currency"))
  })
  private Money creditBalance;

  @Column(name = "balance_last_updated")
  private LocalDateTime lastUpdated;

  public AccountBalance(Money currentBalance) {
    this.currentBalance = currentBalance;
    this.debitBalance = Money.zero(currentBalance.getCurrencyCode());
    this.creditBalance = Money.zero(currentBalance.getCurrencyCode());
    this.lastUpdated = LocalDateTime.now();
  }

  public static AccountBalance zero(String currencyCode) {
    return new AccountBalance(Money.zero(currencyCode));
  }

  public void debit(Money amount) {
    if (!amount.getCurrencyCode().equals(currentBalance.getCurrencyCode())) {
      throw new IllegalArgumentException("Currency mismatch for debit operation");
    }
    this.debitBalance = debitBalance.add(amount);
    this.currentBalance = currentBalance.add(amount);
    this.lastUpdated = LocalDateTime.now();
  }

  public void credit(Money amount) {
    if (!amount.getCurrencyCode().equals(currentBalance.getCurrencyCode())) {
      throw new IllegalArgumentException("Currency mismatch for credit operation");
    }
    this.creditBalance = creditBalance.add(amount);
    this.currentBalance = currentBalance.subtract(amount);
    this.lastUpdated = LocalDateTime.now();
  }

  public boolean isPositive() {
    return currentBalance.isPositive();
  }

  public boolean isNegative() {
    return currentBalance.isNegative();
  }

  public boolean isZero() {
    return currentBalance.isZero();
  }

  public Money getNetChange() {
    return debitBalance.subtract(creditBalance);
  }
}
