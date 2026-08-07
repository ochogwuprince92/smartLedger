package com.finance.smartLedger.reporting.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.smartLedger.ledger.application.AccountService;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IncomeStatementGenerator {

  private final AccountService accountService;
  private final ObjectMapper objectMapper;

  public String generateIncomeStatement(
      LocalDateTime periodStartDate, LocalDateTime periodEndDate, String currencyCode)
      throws Exception {
    List<Account> allAccounts = accountService.findActiveAccounts();

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

    for (Account account : allAccounts) {
      if (!account.getBalance().getCurrentBalance().getCurrencyCode().equals(currencyCode)) {
        continue;
      }

      BigDecimal balance = account.getBalance().getCurrentBalance().getAmount();

      if (account.getAccountType() == AccountType.REVENUE) {
        revenues.put(account.getAccountNumber().getValue(), accountBalanceMap(account, balance));
        totalRevenue = totalRevenue.add(balance);
      } else if (account.getAccountType() == AccountType.EXPENSE) {
        expenses.put(account.getAccountNumber().getValue(), accountBalanceMap(account, balance));
        totalExpenses = totalExpenses.add(balance);
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
