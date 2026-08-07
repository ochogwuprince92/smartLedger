package com.finance.smartLedger.ledger.application;

import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.domain.valueobject.AccountBalance;
import com.finance.smartLedger.ledger.domain.valueobject.AccountCode;
import com.finance.smartLedger.ledger.domain.valueobject.AccountNumber;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final com.finance.smartLedger.shared.domain.EventPublisher eventPublisher;

  public Account createAccount(
      String accountNumber,
      String accountCode,
      String accountName,
      AccountType accountType,
      Money initialBalance,
      String createdBy) {

    if (accountRepository.existsByAccountNumber_Value(accountNumber)) {
      throw new IllegalArgumentException("Account number already exists: " + accountNumber);
    }

    if (accountRepository.existsByAccountCode_Value(accountCode)) {
      throw new IllegalArgumentException("Account code already exists: " + accountCode);
    }

    Account account =
        new Account(
            AccountNumber.of(accountNumber),
            AccountCode.of(accountCode),
            accountName,
            accountType,
            initialBalance,
            createdBy);

    Account savedAccount = accountRepository.save(account);
    eventPublisher.publish(savedAccount.toAccountCreatedEvent());
    return savedAccount;
  }

  public Optional<Account> findById(UUID id) {
    return accountRepository.findById(id);
  }

  public Optional<Account> findByAccountNumber(String accountNumber) {
    return accountRepository.findByAccountNumber_Value(accountNumber);
  }

  public Optional<Account> findByAccountCode(String accountCode) {
    return accountRepository.findByAccountCode_Value(accountCode);
  }

  public List<Account> findByAccountType(AccountType accountType) {
    return accountRepository.findByAccountType(accountType);
  }

  public List<Account> findActiveAccounts() {
    return accountRepository.findByIsActiveTrue();
  }

  public List<Account> findByParentAccountId(UUID parentAccountId) {
    return accountRepository.findByParentAccountId(parentAccountId);
  }

  public List<Account> findAllAccounts() {
    return accountRepository.findAll();
  }

  @Transactional
  public Account updateAccount(UUID id, String accountName, String description, String updatedBy) {

    Account account =
        accountRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

    if (accountName != null) {
      account.setAccountName(accountName);
    }
    if (description != null) {
      account.setDescription(description);
    }

    account.setUpdatedBy(updatedBy);
    return accountRepository.save(account);
  }

  @Transactional
  public Account patchAccount(
      UUID id, String accountName, String description, Boolean isActive, String updatedBy) {
    Account account =
        accountRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

    if (accountName != null) {
      account.setAccountName(accountName);
    }
    if (description != null) {
      account.setDescription(description);
    }
    if (isActive != null) {
      if (isActive) {
        account.activate(updatedBy);
      } else {
        account.deactivate(updatedBy);
      }
    }

    account.setUpdatedBy(updatedBy);
    return accountRepository.save(account);
  }

  @Transactional
  public void activateAccount(UUID id, String updatedBy) {
    Account account =
        accountRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

    account.activate(updatedBy);
    accountRepository.save(account);
  }

  @Transactional
  public void deactivateAccount(UUID id, String updatedBy) {
    Account account =
        accountRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

    account.deactivate(updatedBy);
    accountRepository.save(account);
  }

  @Transactional
  public void debitAccount(UUID id, Money amount, String updatedBy) {
    Account account =
        accountRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

    if (!account.canDebit(amount)) {
      throw new IllegalStateException("Insufficient balance or account cannot be debited");
    }

    Money previousBalance = account.getBalance().getCurrentBalance();
    account.debit(amount, updatedBy);
    accountRepository.save(account);
    eventPublisher.publish(account.toBalanceUpdatedEvent(updatedBy));
  }

  @Transactional
  public void creditAccount(UUID id, Money amount, String updatedBy) {
    Account account =
        accountRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

    Money previousBalance = account.getBalance().getCurrentBalance();
    account.credit(amount, updatedBy);
    accountRepository.save(account);
    eventPublisher.publish(account.toBalanceUpdatedEvent(updatedBy));
  }

  @Transactional
  public Account updateBalance(UUID id, String operation, Money amount, String updatedBy) {
    if ("DEBIT".equalsIgnoreCase(operation)) {
      debitAccount(id, amount, updatedBy);
    } else if ("CREDIT".equalsIgnoreCase(operation)) {
      creditAccount(id, amount, updatedBy);
    } else {
      throw new IllegalArgumentException("Invalid operation: " + operation);
    }
    return accountRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
  }

  @Transactional
  public void deleteAccount(UUID id) {
    Account account =
        accountRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

    // Check if account has children
    List<Account> childAccounts = accountRepository.findByParentAccountId(id);
    if (!childAccounts.isEmpty()) {
      throw new IllegalStateException("Cannot delete account with child accounts");
    }

    accountRepository.delete(account);
  }

  public Money getAccountBalance(UUID id) {
    Account account =
        accountRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

    return account.getBalance().getCurrentBalance();
  }

  public AccountBalance getAccountBalanceDetails(UUID id) {
    Account account =
        accountRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

    return account.getBalance();
  }
}
