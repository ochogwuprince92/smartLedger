package com.finance.smartLedger.ledger.domain.event;

import com.finance.smartLedger.shared.domain.DomainEvent;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AccountCreated extends DomainEvent {

  private UUID accountId;
  private String accountNumber;
  private String accountCode;
  private String accountName;
  private String accountType;
  private String createdBy;

  public AccountCreated(
      UUID accountId,
      String accountNumber,
      String accountCode,
      String accountName,
      String accountType,
      String createdBy) {
    super("AccountCreated");
    this.accountId = accountId;
    this.accountNumber = accountNumber;
    this.accountCode = accountCode;
    this.accountName = accountName;
    this.accountType = accountType;
    this.createdBy = createdBy;
  }

  public static AccountCreated of(
      UUID accountId,
      String accountNumber,
      String accountCode,
      String accountName,
      String accountType,
      String createdBy) {
    return new AccountCreated(
        accountId, accountNumber, accountCode, accountName, accountType, createdBy);
  }
}
