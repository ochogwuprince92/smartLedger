package com.finance.smartLedger.reporting.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.smartLedger.journal.application.JournalEntryService;
import com.finance.smartLedger.journal.domain.DebitCredit;
import com.finance.smartLedger.journal.domain.JournalEntry;
import com.finance.smartLedger.journal.domain.JournalLineItem;
import com.finance.smartLedger.ledger.application.AccountService;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IncomeStatementGenerator {

  private final AccountService accountService;
  private final JournalEntryService journalEntryService;
  private final ObjectMapper objectMapper;

  public String generateIncomeStatement(
      LocalDateTime periodStartDate, LocalDateTime periodEndDate, String currencyCode)
      throws Exception {
    List<Account> allAccounts = accountService.findActiveAccounts();

    // Get journal entries within the period
    List<JournalEntry> journalEntriesInPeriod =
        journalEntryService.findByEntryDateBetween(periodStartDate, periodEndDate);

    // Filter to only posted entries
    List<JournalEntry> postedEntries =
        journalEntriesInPeriod.stream()
            .filter(JournalEntry::getPosted)
            .collect(Collectors.toList());

    // Collect all line items from posted entries
    List<JournalLineItem> allLineItems =
        postedEntries.stream()
            .flatMap(entry -> entry.getLineItems().stream())
            .collect(Collectors.toList());

    BigDecimal totalRevenue = BigDecimal.ZERO;
    BigDecimal totalExpenses = BigDecimal.ZERO;
    BigDecimal grossProfit = BigDecimal.ZERO;
    BigDecimal operatingIncome = BigDecimal.ZERO;
    BigDecimal netIncome = BigDecimal.ZERO;

    Map<String, Object> incomeStatement = new HashMap<>();
    incomeStatement.put("reportType", "INCOME_STATEMENT");
    incomeStatement.put("periodStartDate", periodStartDate);
    incomeStatement.put("periodEndDate", periodEndDate);
    incomeStatement.put("currencyCode", currencyCode);

    Map<String, Object> revenues = new HashMap<>();
    Map<String, Object> expenses = new HashMap<>();

    // Calculate period-specific balances by summing journal line items
    Map<UUID, BigDecimal> accountPeriodBalances = new HashMap<>();

    for (JournalLineItem lineItem : allLineItems) {
      UUID accountId = lineItem.getAccountId();
      BigDecimal amount = lineItem.getAmount().getAmount();

      // For revenue accounts: credits increase revenue, debits decrease
      // For expense accounts: debits increase expense, credits decrease
      Account account =
          allAccounts.stream()
              .filter(a -> a.getId().equals(accountId))
              .findFirst()
              .orElse(null);

      if (account == null) continue;
      if (!account.getBalance().getCurrentBalance().getCurrencyCode().equals(currencyCode)) {
        continue;
      }

      BigDecimal currentBalance = accountPeriodBalances.getOrDefault(accountId, BigDecimal.ZERO);

      if (account.getAccountType() == AccountType.REVENUE) {
        // Revenue is credit-normal: credit adds, debit subtracts
        if (lineItem.getDebitCredit() == DebitCredit.CREDIT) {
          currentBalance = currentBalance.add(amount);
        } else {
          currentBalance = currentBalance.subtract(amount);
        }
      } else if (account.getAccountType() == AccountType.EXPENSE) {
        // Expense is debit-normal: debit adds, credit subtracts
        if (lineItem.getDebitCredit() == DebitCredit.DEBIT) {
          currentBalance = currentBalance.add(amount);
        } else {
          currentBalance = currentBalance.subtract(amount);
        }
      }

      accountPeriodBalances.put(accountId, currentBalance);
    }

    // Build revenue and expense maps from period balances
    for (Account account : allAccounts) {
      if (!account.getBalance().getCurrentBalance().getCurrencyCode().equals(currencyCode)) {
        continue;
      }

      UUID accountId = account.getId();
      BigDecimal periodBalance = accountPeriodBalances.getOrDefault(accountId, BigDecimal.ZERO);

      if (account.getAccountType() == AccountType.REVENUE) {
        revenues.put(account.getAccountNumber().getValue(), accountBalanceMap(account, periodBalance));
        totalRevenue = totalRevenue.add(periodBalance);
      } else if (account.getAccountType() == AccountType.EXPENSE) {
        expenses.put(account.getAccountNumber().getValue(), accountBalanceMap(account, periodBalance));
        totalExpenses = totalExpenses.add(periodBalance);
      }
    }

    grossProfit = totalRevenue;
    operatingIncome = grossProfit.subtract(totalExpenses);
    netIncome = operatingIncome;

    incomeStatement.put("revenues", revenues);
    incomeStatement.put("totalRevenue", totalRevenue);
    incomeStatement.put("grossProfit", grossProfit);
    incomeStatement.put("expenses", expenses);
    incomeStatement.put("totalExpenses", totalExpenses);
    incomeStatement.put("operatingIncome", operatingIncome);
    incomeStatement.put("netIncome", netIncome);

    incomeStatement.put("profitMargin", calculateProfitMargin(netIncome, totalRevenue));

    return objectMapper.writeValueAsString(incomeStatement);
  }

  private Map<String, Object> accountBalanceMap(Account account, BigDecimal balance) {
    Map<String, Object> accountMap = new HashMap<>();
    accountMap.put("accountNumber", account.getAccountNumber().getValue());
    accountMap.put("accountName", account.getAccountName());
    accountMap.put("accountType", account.getAccountType().name());
    accountMap.put("balance", balance);
    return accountMap;
  }

  private BigDecimal calculateProfitMargin(BigDecimal netIncome, BigDecimal totalRevenue) {
    if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return netIncome
        .divide(totalRevenue, 4, java.math.RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }
}
