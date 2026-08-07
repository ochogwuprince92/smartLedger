package com.finance.smartLedger.ledger.domain;

import com.finance.smartLedger.ledger.domain.event.AccountCreated;
import com.finance.smartLedger.ledger.domain.event.BalanceUpdated;
import com.finance.smartLedger.ledger.domain.valueobject.AccountBalance;
import com.finance.smartLedger.ledger.domain.valueobject.AccountCode;
import com.finance.smartLedger.ledger.domain.valueobject.AccountNumber;
import com.finance.smartLedger.shared.entity.AuditableEntity;
import com.finance.smartLedger.shared.valueobject.Money;
import jakarta.persistence.*;
import jakarta.persistence.AttributeOverride;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "accounts",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = "account_number"),
      @UniqueConstraint(columnNames = "account_code")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Account extends AuditableEntity {

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "account_number"))
  private AccountNumber accountNumber;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "account_code"))
  private AccountCode accountCode;

  @Column(name = "account_name", nullable = false, length = 100)
  private String accountName;

  @Enumerated(EnumType.STRING)
  @Column(name = "account_type", nullable = false, length = 20)
  private AccountType accountType;

  @Embedded private AccountBalance balance;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "parent_account_id")
  private UUID parentAccountId;

  // TODO: Add Transaction relationship when Transaction entity is created
  // @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  // private Set<Transaction> transactions = new HashSet<>();

  public Account(
      AccountNumber accountNumber,
      AccountCode accountCode,
      String accountName,
      AccountType accountType,
      Money initialBalance,
      String createdBy) {
    this.accountNumber = accountNumber;
    this.accountCode = accountCode;
    this.accountName = accountName;
    this.accountType = accountType;
    this.balance = new AccountBalance(initialBalance);
    this.isActive = true;
    this.setCreatedBy(createdBy);
    this.setUpdatedBy(createdBy);
  }

  public void debit(Money amount, String updatedBy) {
    if (!isActive) {
      throw new IllegalStateException("Cannot debit from inactive account");
    }
    Money previousBalance = balance.getCurrentBalance();
    balance.debit(amount);
    this.setUpdatedBy(updatedBy);
    // Domain event would be published here
  }

  public void credit(Money amount, String updatedBy) {
    if (!isActive) {
      throw new IllegalStateException("Cannot credit inactive account");
    }
    Money previousBalance = balance.getCurrentBalance();
    balance.credit(amount);
    this.setUpdatedBy(updatedBy);
    // Domain event would be published here
  }

  public void activate(String updatedBy) {
    this.isActive = true;
    this.setUpdatedBy(updatedBy);
  }

  public void deactivate(String updatedBy) {
    this.isActive = false;
    this.setUpdatedBy(updatedBy);
  }

  public boolean canDebit(Money amount) {
    if (!isActive) return false;
    if (accountType.isCreditAccount()) {
      return balance.getCurrentBalance().isGreaterThan(amount);
    }
    return true;
  }

  public AccountCreated toAccountCreatedEvent() {
    return AccountCreated.of(
        getId(),
        accountNumber.getValue(),
        accountCode.getValue(),
        accountName,
        accountType.name(),
        getCreatedBy());
  }

  public BalanceUpdated toBalanceUpdatedEvent(String updatedBy) {
    return BalanceUpdated.of(
        getId(),
        accountNumber.getValue(),
        balance.getCurrentBalance(),
        balance.getCurrentBalance(),
        updatedBy);
  }
}
