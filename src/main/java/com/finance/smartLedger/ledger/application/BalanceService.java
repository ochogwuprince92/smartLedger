package com.finance.smartLedger.ledger.application;

import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.domain.valueobject.AccountBalance;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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

  public Map<AccountType, Money> getBalancesByAccountType() {
    List<Account> accounts = accountRepository.findAll();

    return accounts.stream()
        .collect(
            Collectors.groupingBy(
                Account::getAccountType,
                Collectors.reducing(
                    Money.zero("USD"),
                    account -> account.getBalance().getCurrentBalance(),
                    Money::add)));
  }

  public Money getTotalAssetBalance() {
    List<Account> assetAccounts = accountRepository.findByAccountType(AccountType.ASSET);

    return assetAccounts.stream()
        .map(account -> account.getBalance().getCurrentBalance())
        .reduce(Money.zero("USD"), Money::add);
  }

  public Money getTotalLiabilityBalance() {
    List<Account> liabilityAccounts = accountRepository.findByAccountType(AccountType.LIABILITY);

    return liabilityAccounts.stream()
        .map(account -> account.getBalance().getCurrentBalance())
        .reduce(Money.zero("USD"), Money::add);
  }

  public Money getTotalEquityBalance() {
    List<Account> equityAccounts = accountRepository.findByAccountType(AccountType.EQUITY);

    return equityAccounts.stream()
        .map(account -> account.getBalance().getCurrentBalance())
        .reduce(Money.zero("USD"), Money::add);
  }

  public Money getTotalRevenueBalance() {
    List<Account> revenueAccounts = accountRepository.findByAccountType(AccountType.REVENUE);

    return revenueAccounts.stream()
        .map(account -> account.getBalance().getCurrentBalance())
        .reduce(Money.zero("USD"), Money::add);
  }

  public Money getTotalExpenseBalance() {
    List<Account> expenseAccounts = accountRepository.findByAccountType(AccountType.EXPENSE);

    return expenseAccounts.stream()
        .map(account -> account.getBalance().getCurrentBalance())
        .reduce(Money.zero("USD"), Money::add);
  }

  public Money getNetIncome() {
    Money totalRevenue = getTotalRevenueBalance();
    Money totalExpense = getTotalExpenseBalance();

    return totalRevenue.subtract(totalExpense);
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

  public Money calculateTrialBalance() {
    Money totalDebits = Money.zero("USD");
    Money totalCredits = Money.zero("USD");

    List<Account> allAccounts = accountRepository.findAll();

    for (Account account : allAccounts) {
      Money balance = account.getBalance().getCurrentBalance();

      if (account.getAccountType().isDebitAccount()) {
        totalDebits = totalDebits.add(balance);
      } else {
        totalCredits = totalCredits.add(balance);
      }
    }

    // Trial balance should be zero (debits = credits)
    return totalDebits.subtract(totalCredits);
  }

  public boolean isTrialBalanceBalanced() {
    Money difference = calculateTrialBalance();
    return difference.getAmount().compareTo(BigDecimal.ZERO) == 0;
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
