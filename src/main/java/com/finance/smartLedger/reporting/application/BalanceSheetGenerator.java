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
public class BalanceSheetGenerator {

  private final AccountService accountService;
  private final ObjectMapper objectMapper;

  public String generateBalanceSheet(LocalDateTime asOfDate, String currencyCode) throws Exception {
    List<Account> allAccounts = accountService.findActiveAccounts();

    BigDecimal totalAssets = BigDecimal.ZERO;
    BigDecimal totalLiabilities = BigDecimal.ZERO;
    BigDecimal totalEquity = BigDecimal.ZERO;

    Map<String, Object> balanceSheet = new HashMap<>();
    balanceSheet.put("reportType", "BALANCE_SHEET");
    balanceSheet.put("asOfDate", asOfDate);
    balanceSheet.put("currencyCode", currencyCode);

    Map<String, Object> assets = new HashMap<>();
    Map<String, Object> liabilities = new HashMap<>();
    Map<String, Object> equity = new HashMap<>();

    for (Account account : allAccounts) {
      if (!account.getBalance().getCurrentBalance().getCurrencyCode().equals(currencyCode)) {
        continue;
      }

      BigDecimal balance = account.getBalance().getCurrentBalance().getAmount();

      if (account.getAccountType() == AccountType.ASSET) {
        assets.put(account.getAccountNumber().getValue(), accountBalanceMap(account, balance));
        totalAssets = totalAssets.add(balance);
      } else if (account.getAccountType() == AccountType.LIABILITY) {
        liabilities.put(account.getAccountNumber().getValue(), accountBalanceMap(account, balance));
        totalLiabilities = totalLiabilities.add(balance);
      } else if (account.getAccountType() == AccountType.EQUITY) {
        equity.put(account.getAccountNumber().getValue(), accountBalanceMap(account, balance));
        totalEquity = totalEquity.add(balance);
      }
    }

    balanceSheet.put("assets", assets);
    balanceSheet.put("totalAssets", totalAssets);
    balanceSheet.put("liabilities", liabilities);
    balanceSheet.put("totalLiabilities", totalLiabilities);
    balanceSheet.put("equity", equity);
    balanceSheet.put("totalEquity", totalEquity);

    BigDecimal totalLiabilitiesAndEquity = totalLiabilities.add(totalEquity);
    balanceSheet.put("totalLiabilitiesAndEquity", totalLiabilitiesAndEquity);
    balanceSheet.put("isBalanced", totalAssets.compareTo(totalLiabilitiesAndEquity) == 0);

    return objectMapper.writeValueAsString(balanceSheet);
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
