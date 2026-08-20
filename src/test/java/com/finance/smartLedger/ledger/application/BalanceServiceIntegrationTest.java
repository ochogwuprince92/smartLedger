package com.finance.smartLedger.ledger.application;

import static org.junit.jupiter.api.Assertions.*;

import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.finance.smartLedger.test.configuration.TestDatabaseConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.Disabled;

@SpringBootTest
@TestPropertySource(properties = {
    "app.scheduled.enabled=false",
    "app.data-loader.enabled=false"
})
@Disabled("Docker not available on this system")
@Testcontainers
class BalanceServiceIntegrationTest {

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    TestDatabaseConfiguration.configureDatabase(registry);
  }

  @Autowired private BalanceService balanceService;

  @Autowired private AccountService accountService;

  @Autowired private AccountRepository accountRepository;

  private UUID assetAccountId;
  private UUID liabilityAccountId;
  private UUID equityAccountId;
  private UUID revenueAccountId;
  private UUID expenseAccountId;

  @BeforeEach
  void setUp() {
    accountRepository.deleteAll();

    Account asset =
        accountService.createAccount(
            "1001",
            "1001",
            "Cash",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(10000.00), "USD"),
            "test-user");
    Account liability =
        accountService.createAccount(
            "2001",
            "2001",
            "Accounts Payable",
            AccountType.LIABILITY,
            Money.of(BigDecimal.valueOf(5000.00), "USD"),
            "test-user");
    Account equity =
        accountService.createAccount(
            "3001",
            "3001",
            "Capital",
            AccountType.EQUITY,
            Money.of(BigDecimal.valueOf(5000.00), "USD"),
            "test-user");
    Account revenue =
        accountService.createAccount(
            "4001",
            "4001",
            "Sales",
            AccountType.REVENUE,
            Money.of(BigDecimal.valueOf(15000.00), "USD"),
            "test-user");
    Account expense =
        accountService.createAccount(
            "5001",
            "5001",
            "Rent",
            AccountType.EXPENSE,
            Money.of(BigDecimal.valueOf(8000.00), "USD"),
            "test-user");

    assetAccountId = asset.getId();
    liabilityAccountId = liability.getId();
    equityAccountId = equity.getId();
    revenueAccountId = revenue.getId();
    expenseAccountId = expense.getId();
  }

  @AfterEach
  void tearDown() {
    accountRepository.deleteAll();
  }

  @Test
  void getCurrentBalance_Success() {
    Money balance = balanceService.getCurrentBalance(assetAccountId);

    assertEquals(BigDecimal.valueOf(10000.00), balance.getAmount());
    assertEquals("USD", balance.getCurrencyCode());
  }

  @Test
  void getBalanceDetails_Success() {
    var balanceDetails = balanceService.getBalanceDetails(assetAccountId);

    assertNotNull(balanceDetails);
    assertEquals(BigDecimal.valueOf(10000.00), balanceDetails.getCurrentBalance().getAmount());
  }

  @Test
  void getBalancesByAccountType_Success() {
    Map<AccountType, Map<String, Money>> balances = balanceService.getBalancesByAccountType();

    assertEquals(5, balances.size());
    assertEquals(
        BigDecimal.valueOf(10000.00), balances.get(AccountType.ASSET).get("USD").getAmount());
    assertEquals(
        BigDecimal.valueOf(5000.00), balances.get(AccountType.LIABILITY).get("USD").getAmount());
    assertEquals(
        BigDecimal.valueOf(5000.00), balances.get(AccountType.EQUITY).get("USD").getAmount());
    assertEquals(
        BigDecimal.valueOf(15000.00), balances.get(AccountType.REVENUE).get("USD").getAmount());
    assertEquals(
        BigDecimal.valueOf(8000.00), balances.get(AccountType.EXPENSE).get("USD").getAmount());
  }

  @Test
  void getTotalAssetBalance_Success() {
    Money totalAssets = balanceService.getTotalBalance(AccountType.ASSET, "USD");

    assertEquals(BigDecimal.valueOf(10000.00), totalAssets.getAmount());
  }

  @Test
  void getTotalLiabilityBalance_Success() {
    Money totalLiabilities = balanceService.getTotalBalance(AccountType.LIABILITY, "USD");

    assertEquals(BigDecimal.valueOf(5000.00), totalLiabilities.getAmount());
  }

  @Test
  void getTotalEquityBalance_Success() {
    Money totalEquity = balanceService.getTotalBalance(AccountType.EQUITY, "USD");

    assertEquals(BigDecimal.valueOf(5000.00), totalEquity.getAmount());
  }

  @Test
  void getTotalRevenueBalance_Success() {
    Money totalRevenue = balanceService.getTotalBalance(AccountType.REVENUE, "USD");

    assertEquals(BigDecimal.valueOf(15000.00), totalRevenue.getAmount());
  }

  @Test
  void getTotalExpenseBalance_Success() {
    Money totalExpenses = balanceService.getTotalBalance(AccountType.EXPENSE, "USD");

    assertEquals(BigDecimal.valueOf(8000.00), totalExpenses.getAmount());
  }

  @Test
  void getNetIncome_Success() {
    Money netIncome = balanceService.getNetIncome("USD");

    assertEquals(BigDecimal.valueOf(7000.00), netIncome.getAmount()); // 15000 - 8000
  }

  @Test
  void adjustBalance_Increase_Success() {
    balanceService.adjustBalance(
        assetAccountId, Money.of(BigDecimal.valueOf(1000.00), "USD"), "Adjustment", "test-user");

    Money newBalance = balanceService.getCurrentBalance(assetAccountId);
    assertEquals(BigDecimal.valueOf(11000.00), newBalance.getAmount());
  }

  @Test
  void adjustBalance_Decrease_Success() {
    balanceService.adjustBalance(
        assetAccountId, Money.of(BigDecimal.valueOf(-1000.00), "USD"), "Adjustment", "test-user");

    Money newBalance = balanceService.getCurrentBalance(assetAccountId);
    assertEquals(BigDecimal.valueOf(9000.00), newBalance.getAmount());
  }

  @Test
  void transferBalance_Success() {
    Money initialAssetBalance = balanceService.getCurrentBalance(assetAccountId);
    Money initialLiabilityBalance = balanceService.getCurrentBalance(liabilityAccountId);

    balanceService.transferBalance(
        assetAccountId,
        liabilityAccountId,
        Money.of(BigDecimal.valueOf(1000.00), "USD"),
        "Transfer",
        "test-user");

    Money newAssetBalance = balanceService.getCurrentBalance(assetAccountId);
    Money newLiabilityBalance = balanceService.getCurrentBalance(liabilityAccountId);

    assertEquals(
        initialAssetBalance.getAmount().subtract(BigDecimal.valueOf(1000.00)),
        newAssetBalance.getAmount());
    assertEquals(
        initialLiabilityBalance.getAmount().add(BigDecimal.valueOf(1000.00)),
        newLiabilityBalance.getAmount());
  }

  @Test
  void transferBalance_DifferentCurrencies_ThrowsException() {
    Account accountEUR =
        accountService.createAccount(
            "6001",
            "6001",
            "EUR Account",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(5000.00), "EUR"),
            "test-user");

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          balanceService.transferBalance(
              assetAccountId,
              accountEUR.getId(),
              Money.of(BigDecimal.valueOf(1000.00), "USD"),
              "Transfer",
              "test-user");
        });
  }

  @Test
  void calculateTrialBalance_Success() {
    Money difference = balanceService.calculateTrialBalance("USD");

    // In a balanced system, this should be zero
    assertEquals(BigDecimal.ZERO, difference.getAmount());
  }

  @Test
  void isTrialBalanceBalanced_Success() {
    boolean balanced = balanceService.isTrialBalanceBalanced();

    assertTrue(balanced);
  }

  @Test
  void getAccountsWithNegativeBalance_Success() {
    accountService.debitAccount(
        assetAccountId, Money.of(BigDecimal.valueOf(11000.00), "USD"), "test-user");

    List<Account> negativeAccounts = balanceService.getAccountsWithNegativeBalance();

    assertEquals(1, negativeAccounts.size());
    assertEquals(assetAccountId, negativeAccounts.get(0).getId());
  }

  @Test
  void getAccountsWithZeroBalance_Success() {
    accountService.debitAccount(
        assetAccountId, Money.of(BigDecimal.valueOf(10000.00), "USD"), "test-user");

    List<Account> zeroBalanceAccounts = balanceService.getAccountsWithZeroBalance();

    assertEquals(1, zeroBalanceAccounts.size());
    assertEquals(assetAccountId, zeroBalanceAccounts.get(0).getId());
  }

  @Test
  void reconcileBalance_Success() {
    balanceService.reconcileBalance(
        assetAccountId,
        Money.of(BigDecimal.valueOf(12000.00), "USD"),
        "Bank reconciliation",
        "test-user");

    Money newBalance = balanceService.getCurrentBalance(assetAccountId);
    assertEquals(BigDecimal.valueOf(12000.00), newBalance.getAmount());
  }

  @Test
  void reconcileBalance_NoChange_WhenBalanced() {
    Money initialBalance = balanceService.getCurrentBalance(assetAccountId);

    balanceService.reconcileBalance(assetAccountId, initialBalance, "No change", "test-user");

    Money newBalance = balanceService.getCurrentBalance(assetAccountId);
    assertEquals(initialBalance.getAmount(), newBalance.getAmount());
  }
}
