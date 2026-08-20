package com.finance.smartLedger.ledger.application;

import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.domain.valueobject.AccountBalance;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import jakarta.transaction.Transactional;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BalanceService {

  private final AccountRepository accountRepository;

  public Money getCurrentBalance(UUID accountId) {
    Account account =
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

    return account.getBalance().getCurrentBalance();
  }

  public AccountBalance getBalanceDetails(UUID accountId) {
    Account account =
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

    return account.getBalance();
  }

  /** Totals per account type, split by currency so balances are never summed across currencies. */
  public Map<AccountType, Map<String, Money>> getBalancesByAccountType() {
    Map<AccountType, Map<String, Money>> balances = new EnumMap<>(AccountType.class);

    for (Account account : accountRepository.findAll()) {
      Money balance = account.getBalance().getCurrentBalance();
      balances
          .computeIfAbsent(account.getAccountType(), accountType -> new TreeMap<>())
          .merge(balance.getCurrencyCode(), balance, Money::add);
    }

    return balances;
  }

  /** Total balance of all accounts of the given type, per currency. */
  public Map<String, Money> getTotalBalanceByCurrency(AccountType accountType) {
    Map<String, Money> totals = new TreeMap<>();

    for (Account account : accountRepository.findByAccountType(accountType)) {
      Money balance = account.getBalance().getCurrentBalance();
      totals.merge(balance.getCurrencyCode(), balance, Money::add);
    }

    return totals;
  }

  public Money getTotalBalance(AccountType accountType, String currencyCode) {
    Money zero = Money.zero(currencyCode);
    return getTotalBalanceByCurrency(accountType).getOrDefault(currencyCode, zero);
  }

  /** Net income (revenue - expenses) per currency. */
  public Map<String, Money> getNetIncomeByCurrency() {
    Map<String, Money> revenues = getTotalBalanceByCurrency(AccountType.REVENUE);
    Map<String, Money> expenses = getTotalBalanceByCurrency(AccountType.EXPENSE);

    Map<String, Money> netIncome = new TreeMap<>();
    Stream.concat(revenues.keySet().stream(), expenses.keySet().stream())
        .distinct()
        .forEach(
            currencyCode -> {
              Money zero = Money.zero(currencyCode);
              netIncome.put(
                  currencyCode,
                  revenues
                      .getOrDefault(currencyCode, zero)
                      .subtract(expenses.getOrDefault(currencyCode, zero)));
            });

    return netIncome;
  }

  public Money getNetIncome(String currencyCode) {
    Money zero = Money.zero(currencyCode);
    return getNetIncomeByCurrency().getOrDefault(currencyCode, zero);
  }

  @Transactional
  public void adjustBalance(
      UUID accountId, Money adjustmentAmount, String reason, String updatedBy) {
    Account account =
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

    if (adjustmentAmount.isPositive()) {
      account.credit(adjustmentAmount, updatedBy);
    } else if (adjustmentAmount.isNegative()) {
      Money debitAmount =
          Money.of(adjustmentAmount.getAmount().abs(), adjustmentAmount.getCurrencyCode());
      if (!account.canDebit(debitAmount)) {
        throw new IllegalStateException("Insufficient balance for adjustment");
      }
      account.debit(debitAmount, updatedBy);
    }

    accountRepository.save(account);
  }

  @Transactional
  public Account adjustBalanceWithAccount(
      UUID accountId, Money adjustmentAmount, String reason, String updatedBy) {
    adjustBalance(accountId, adjustmentAmount, reason, updatedBy);
    return accountRepository
        .findById(accountId)
        .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
  }

  @Transactional
  public void transferBalance(
      UUID fromAccountId, UUID toAccountId, Money amount, String reference, String updatedBy) {

    Account fromAccount =
        accountRepository
            .findById(fromAccountId)
            .orElseThrow(
                () -> new IllegalArgumentException("Source account not found: " + fromAccountId));

    Account toAccount =
        accountRepository
            .findById(toAccountId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Destination account not found: " + toAccountId));

    if (!fromAccount.canDebit(amount)) {
      throw new IllegalStateException("Insufficient balance in source account");
    }

    if (!fromAccount
        .getBalance()
        .getCurrentBalance()
        .getCurrencyCode()
        .equals(toAccount.getBalance().getCurrentBalance().getCurrencyCode())) {
      throw new IllegalArgumentException(
          "Cannot transfer between accounts with different currencies");
    }

    fromAccount.debit(amount, updatedBy);
    toAccount.credit(amount, updatedBy);

    accountRepository.save(fromAccount);
    accountRepository.save(toAccount);
  }

  /**
   * Calculates the trial balance difference (debits - credits) for every currency in the ledger.
   * A trial balance only balances within a single currency, so balances are never summed across
   * currencies.
   */
  public Map<String, Money> calculateTrialBalanceByCurrency() {
    Map<String, Money> debits = new TreeMap<>();
    Map<String, Money> credits = new TreeMap<>();

    for (Account account : accountRepository.findAll()) {
      Money balance = account.getBalance().getCurrentBalance();
      Map<String, Money> target =
          account.getAccountType().isDebitAccount() ? debits : credits;
      target.merge(balance.getCurrencyCode(), balance, Money::add);
    }

    Map<String, Money> differences = new TreeMap<>();
    Stream.concat(debits.keySet().stream(), credits.keySet().stream())
        .distinct()
        .forEach(
            currencyCode -> {
              Money zero = Money.zero(currencyCode);
              differences.put(
                  currencyCode,
                  debits
                      .getOrDefault(currencyCode, zero)
                      .subtract(credits.getOrDefault(currencyCode, zero)));
            });

    return differences;
  }

  public Money calculateTrialBalance(String currencyCode) {
    Money zero = Money.zero(currencyCode);
    return calculateTrialBalanceByCurrency().getOrDefault(currencyCode, zero);
  }

  public boolean isTrialBalanceBalanced() {
    return calculateTrialBalanceByCurrency().values().stream().allMatch(Money::isZero);
  }

  public boolean isTrialBalanceBalanced(String currencyCode) {
    return calculateTrialBalance(currencyCode).isZero();
  }

  public List<Account> getAccountsWithNegativeBalance() {
    return accountRepository.findAll().stream()
        .filter(account -> account.getBalance().getCurrentBalance().isNegative())
        .collect(Collectors.toList());
  }

  public List<Account> getAccountsWithZeroBalance() {
    return accountRepository.findAll().stream()
        .filter(account -> account.getBalance().getCurrentBalance().isZero())
        .collect(Collectors.toList());
  }

  @Transactional
  public void reconcileBalance(
      UUID accountId, Money expectedBalance, String reason, String updatedBy) {
    Account account =
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

    Money currentBalance = account.getBalance().getCurrentBalance();
    Money difference = expectedBalance.subtract(currentBalance);

    if (!difference.isZero()) {
      adjustBalance(accountId, difference, "Reconciliation: " + reason, updatedBy);
    }
  }

  @Transactional
  public Account reconcileBalanceWithAccount(
      UUID accountId, Money expectedBalance, String reason, String updatedBy) {
    reconcileBalance(accountId, expectedBalance, reason, updatedBy);
    return accountRepository
        .findById(accountId)
        .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
  }
}
