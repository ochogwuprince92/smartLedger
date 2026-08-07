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
public class CashFlowStatementGenerator {

  private final AccountService accountService;
  private final ObjectMapper objectMapper;

  public String generateCashFlowStatement(
      LocalDateTime periodStartDate, LocalDateTime periodEndDate, String currencyCode)
      throws Exception {
    List<Account> allAccounts = accountService.findActiveAccounts();

    BigDecimal totalCashFromOperations = BigDecimal.ZERO;
    BigDecimal totalCashFromInvesting = BigDecimal.ZERO;
    BigDecimal totalCashFromFinancing = BigDecimal.ZERO;
    BigDecimal netCashFlow = BigDecimal.ZERO;

    Map<String, Object> cashFlowStatement = new HashMap<>();
    cashFlowStatement.put("reportType", "CASH_FLOW_STATEMENT");
    cashFlowStatement.put("periodStartDate", periodStartDate);
    cashFlowStatement.put("periodEndDate", periodEndDate);
    cashFlowStatement.put("currencyCode", currencyCode);

    Map<String, Object> operatingActivities = new HashMap<>();
    Map<String, Object> investingActivities = new HashMap<>();
    Map<String, Object> financingActivities = new HashMap<>();

    for (Account account : allAccounts) {
      if (!account.getBalance().getCurrentBalance().getCurrencyCode().equals(currencyCode)) {
        continue;
      }

      BigDecimal balance = account.getBalance().getCurrentBalance().getAmount();

      if (account.getAccountType() == AccountType.ASSET
          && account.getAccountName().toLowerCase().contains("cash")) {
        operatingActivities.put(
            account.getAccountNumber().getValue(), accountBalanceMap(account, balance));
        totalCashFromOperations = totalCashFromOperations.add(balance);
      } else if (account.getAccountType() == AccountType.ASSET
          && (account.getAccountName().toLowerCase().contains("investment")
              || account.getAccountName().toLowerCase().contains("property"))) {
        investingActivities.put(
            account.getAccountNumber().getValue(), accountBalanceMap(account, balance));
        totalCashFromInvesting = totalCashFromInvesting.add(balance);
      } else if (account.getAccountType() == AccountType.LIABILITY
          || account.getAccountType() == AccountType.EQUITY) {
        financingActivities.put(
            account.getAccountNumber().getValue(), accountBalanceMap(account, balance));
        totalCashFromFinancing = totalCashFromFinancing.add(balance);
      }
    }

    netCashFlow = totalCashFromOperations.add(totalCashFromInvesting).add(totalCashFromFinancing);

    cashFlowStatement.put("operatingActivities", operatingActivities);
    cashFlowStatement.put("totalCashFromOperations", totalCashFromOperations);
    cashFlowStatement.put("investingActivities", investingActivities);
    cashFlowStatement.put("totalCashFromInvesting", totalCashFromInvesting);
    cashFlowStatement.put("financingActivities", financingActivities);
    cashFlowStatement.put("totalCashFromFinancing", totalCashFromFinancing);
    cashFlowStatement.put("netCashFlow", netCashFlow);

    return objectMapper.writeValueAsString(cashFlowStatement);
  }

  private Map<String, Object> accountBalanceMap(Account account, BigDecimal balance) {
    Map<String, Object> accountMap = new HashMap<>();
    accountMap.put("accountNumber", account.getAccountNumber().getValue());
    accountMap.put("accountName", account.getAccountName());
    accountMap.put("accountType", account.getAccountType().name());
    accountMap.put("balance", balance);
    return accountMap;
  }
}
