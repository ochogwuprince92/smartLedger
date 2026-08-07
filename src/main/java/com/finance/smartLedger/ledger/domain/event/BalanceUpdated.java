package com.finance.smartLedger.ledger.domain.event;

import com.finance.smartLedger.shared.domain.DomainEvent;
import com.finance.smartLedger.shared.valueobject.Money;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BalanceUpdated extends DomainEvent {

  private UUID accountId;
  private String accountNumber;
  private Money previousBalance;
  private Money newBalance;
  private Money changeAmount;
  private String updatedBy;

  public BalanceUpdated(
      UUID accountId,
      String accountNumber,
      Money previousBalance,
      Money newBalance,
      Money changeAmount,
      String updatedBy) {
    super("BalanceUpdated");
    this.accountId = accountId;
    this.accountNumber = accountNumber;
    this.previousBalance = previousBalance;
    this.newBalance = newBalance;
    this.changeAmount = changeAmount;
    this.updatedBy = updatedBy;
  }

  public static BalanceUpdated of(
      UUID accountId,
      String accountNumber,
      Money previousBalance,
      Money newBalance,
      String updatedBy) {
    Money changeAmount = newBalance.subtract(previousBalance);
    return new BalanceUpdated(
        accountId, accountNumber, previousBalance, newBalance, changeAmount, updatedBy);
  }
}
