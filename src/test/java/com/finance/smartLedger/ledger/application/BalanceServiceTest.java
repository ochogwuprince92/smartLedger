package com.finance.smartLedger.ledger.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

  @Mock private AccountRepository accountRepository;

  @InjectMocks private BalanceService balanceService;

  private Account testAccount;
  private UUID testId;

  @BeforeEach
  void setUp() {
    testId = UUID.randomUUID();
    testAccount =
        Account.builder()
            .accountName("Test Account")
            .accountType(AccountType.ASSET)
            .isActive(true)
            .build();
    testAccount.setId(testId);
    testAccount.setBalance(
        new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
            com.finance.smartLedger.shared.valueobject.Money.of(
                java.math.BigDecimal.valueOf(1000.00), "USD")));
  }

  @Test
  void getCurrentBalance_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));

    Money balance = balanceService.getCurrentBalance(testId);

    assertEquals(Money.of(BigDecimal.valueOf(1000.00), "USD"), balance);
    verify(accountRepository).findById(testId);
  }

  @Test
  void getCurrentBalance_NotFound_ThrowsException() {
    when(accountRepository.findById(testId)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          balanceService.getCurrentBalance(testId);
        });
  }

  @Test
  void getBalanceDetails_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));

    var balance = balanceService.getBalanceDetails(testId);

    assertEquals(Money.of(BigDecimal.valueOf(1000.00), "USD"), balance.getCurrentBalance());
    verify(accountRepository).findById(testId);
  }

  @Test
  void getBalancesByAccountType_Success() {
    when(accountRepository.findAll()).thenReturn(List.of(testAccount));

    Map<AccountType, Map<String, Money>> balances = balanceService.getBalancesByAccountType();

    assertNotNull(balances);
    assertFalse(balances.isEmpty());
    assertEquals(
        Money.of(BigDecimal.valueOf(1000.00), "USD"), balances.get(AccountType.ASSET).get("USD"));
    verify(accountRepository).findAll();
  }

  @Test
  void getTotalAssetBalance_Success() {
    when(accountRepository.findByAccountType(AccountType.ASSET)).thenReturn(List.of(testAccount));

    Money total = balanceService.getTotalBalance(AccountType.ASSET, "USD");

    assertEquals(Money.of(BigDecimal.valueOf(1000.00), "USD"), total);
    verify(accountRepository).findByAccountType(AccountType.ASSET);
  }

  @Test
  void getTotalLiabilityBalance_Success() {
    when(accountRepository.findByAccountType(AccountType.LIABILITY))
        .thenReturn(List.of(testAccount));

    Money total = balanceService.getTotalBalance(AccountType.LIABILITY, "USD");

    assertEquals(Money.of(BigDecimal.valueOf(1000.00), "USD"), total);
    verify(accountRepository).findByAccountType(AccountType.LIABILITY);
  }

  @Test
  void getTotalEquityBalance_Success() {
    when(accountRepository.findByAccountType(AccountType.EQUITY)).thenReturn(List.of(testAccount));

    Money total = balanceService.getTotalBalance(AccountType.EQUITY, "USD");

    assertEquals(Money.of(BigDecimal.valueOf(1000.00), "USD"), total);
    verify(accountRepository).findByAccountType(AccountType.EQUITY);
  }

  @Test
  void getTotalRevenueBalance_Success() {
    when(accountRepository.findByAccountType(AccountType.REVENUE)).thenReturn(List.of(testAccount));

    Money total = balanceService.getTotalBalance(AccountType.REVENUE, "USD");

    assertEquals(Money.of(BigDecimal.valueOf(1000.00), "USD"), total);
    verify(accountRepository).findByAccountType(AccountType.REVENUE);
  }

  @Test
  void getTotalExpenseBalance_Success() {
    when(accountRepository.findByAccountType(AccountType.EXPENSE)).thenReturn(List.of(testAccount));

    Money total = balanceService.getTotalBalance(AccountType.EXPENSE, "USD");

    assertEquals(Money.of(BigDecimal.valueOf(1000.00), "USD"), total);
    verify(accountRepository).findByAccountType(AccountType.EXPENSE);
  }

  @Test
  void getNetIncome_Success() {
    when(accountRepository.findByAccountType(AccountType.REVENUE)).thenReturn(List.of(testAccount));
    when(accountRepository.findByAccountType(AccountType.EXPENSE)).thenReturn(List.of());

    Money netIncome = balanceService.getNetIncome("USD");

    assertEquals(Money.of(BigDecimal.valueOf(1000.00), "USD"), netIncome);
  }

  @Test
  void getTotalBalanceByCurrency_SeparatesCurrencies() {
    Account ngnAsset = Account.builder()
        .accountName("NGN Asset Account")
        .accountType(AccountType.ASSET)
        .isActive(true)
        .build();
    ngnAsset.setBalance(new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
        Money.of(BigDecimal.valueOf(100000.00), "NGN")));

    when(accountRepository.findByAccountType(AccountType.ASSET))
        .thenReturn(List.of(testAccount, ngnAsset));

    Map<String, Money> totals = balanceService.getTotalBalanceByCurrency(AccountType.ASSET);

    assertEquals(java.util.Set.of("NGN", "USD"), totals.keySet());
    assertEquals(Money.of(BigDecimal.valueOf(1000.00), "USD"), totals.get("USD"));
    assertEquals(Money.of(BigDecimal.valueOf(100000.00), "NGN"), totals.get("NGN"));
  }

  @Test
  void adjustBalance_PositiveAmount_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

    balanceService.adjustBalance(
        testId, Money.of(BigDecimal.valueOf(100.00), "USD"), "Adjustment", "test-user");

    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void adjustBalance_DecreaseBalance_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

    balanceService.adjustBalance(
        testId, Money.of(BigDecimal.valueOf(100.00), "USD"), "Adjustment", "test-user");

    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void adjustBalance_NotFound_ThrowsException() {
    when(accountRepository.findById(testId)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          balanceService.adjustBalance(
              testId, Money.of(BigDecimal.valueOf(100.00), "USD"), "Adjustment", "test-user");
        });

    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void transferBalance_Success() {
    UUID toAccountId = UUID.randomUUID();
    Account toAccount =
        Account.builder()
            .accountName("To Account")
            .accountType(AccountType.ASSET)
            .isActive(true)
            .build();
    toAccount.setId(toAccountId);
    toAccount.setBalance(
        new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
            com.finance.smartLedger.shared.valueobject.Money.of(
                java.math.BigDecimal.valueOf(1000.00), "USD")));

    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

    balanceService.transferBalance(
        testId, toAccountId, Money.of(BigDecimal.valueOf(100.00), "USD"), "Transfer", "test-user");

    verify(accountRepository, times(2)).save(any(Account.class));
  }

  @Test
  void transferBalance_SourceNotFound_ThrowsException() {
    UUID toAccountId = UUID.randomUUID();

    when(accountRepository.findById(testId)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          balanceService.transferBalance(
              testId,
              toAccountId,
              Money.of(BigDecimal.valueOf(100.00), "USD"),
              "Transfer",
              "test-user");
        });

    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void transferBalance_DestinationNotFound_ThrowsException() {
    UUID toAccountId = UUID.randomUUID();

    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.findById(toAccountId)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          balanceService.transferBalance(
              testId,
              toAccountId,
              Money.of(BigDecimal.valueOf(100.00), "USD"),
              "Transfer",
              "test-user");
        });

    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void calculateTrialBalance_Success() {
    // Create accounts with known balances
    Account assetAccount = Account.builder()
        .accountName("Asset Account")
        .accountType(AccountType.ASSET)
        .isActive(true)
        .build();
    assetAccount.setBalance(new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
        com.finance.smartLedger.shared.valueobject.Money.of(java.math.BigDecimal.valueOf(1000.00), "USD")));

    Account liabilityAccount = Account.builder()
        .accountName("Liability Account")
        .accountType(AccountType.LIABILITY)
        .isActive(true)
        .build();
    liabilityAccount.setBalance(new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
        com.finance.smartLedger.shared.valueobject.Money.of(java.math.BigDecimal.valueOf(1000.00), "USD")));

    when(accountRepository.findAll()).thenReturn(List.of(assetAccount, liabilityAccount));

    Money difference = balanceService.calculateTrialBalance("USD");

    // Trial balance should be zero when debits equal credits
    assertTrue(difference.isZero());
    verify(accountRepository).findAll();
  }

  @Test
  void calculateTrialBalanceByCurrency_SeparatesCurrencies() {
    Account usdAsset = Account.builder()
        .accountName("USD Asset Account")
        .accountType(AccountType.ASSET)
        .isActive(true)
        .build();
    usdAsset.setBalance(new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
        com.finance.smartLedger.shared.valueobject.Money.of(java.math.BigDecimal.valueOf(1000.00), "USD")));

    Account usdLiability = Account.builder()
        .accountName("USD Liability Account")
        .accountType(AccountType.LIABILITY)
        .isActive(true)
        .build();
    usdLiability.setBalance(new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
        com.finance.smartLedger.shared.valueobject.Money.of(java.math.BigDecimal.valueOf(1000.00), "USD")));

    Account ngnAsset = Account.builder()
        .accountName("NGN Asset Account")
        .accountType(AccountType.ASSET)
        .isActive(true)
        .build();
    ngnAsset.setBalance(new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
        com.finance.smartLedger.shared.valueobject.Money.of(java.math.BigDecimal.valueOf(100000.00), "NGN")));

    when(accountRepository.findAll()).thenReturn(List.of(usdAsset, usdLiability, ngnAsset));

    java.util.Map<String, Money> differences = balanceService.calculateTrialBalanceByCurrency();

    assertEquals(java.util.Set.of("NGN", "USD"), differences.keySet());
    assertTrue(differences.get("USD").isZero());
    assertEquals(0, differences.get("NGN").getAmount().compareTo(java.math.BigDecimal.valueOf(100000.00)));
    assertEquals("NGN", differences.get("NGN").getCurrencyCode());
    assertFalse(balanceService.isTrialBalanceBalanced());
    assertTrue(balanceService.isTrialBalanceBalanced("USD"));
  }

  @Test
  void calculateTrialBalance_UnknownCurrency_ReturnsZero() {
    when(accountRepository.findAll()).thenReturn(List.of());

    Money difference = balanceService.calculateTrialBalance("EUR");

    assertTrue(difference.isZero());
    assertEquals("EUR", difference.getCurrencyCode());
  }

  @Test
  void isTrialBalanceBalanced_Success() {
    // Create balanced accounts
    Account assetAccount = Account.builder()
        .accountName("Asset Account")
        .accountType(AccountType.ASSET)
        .isActive(true)
        .build();
    assetAccount.setBalance(new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
        com.finance.smartLedger.shared.valueobject.Money.of(java.math.BigDecimal.valueOf(1000.00), "USD")));

    Account liabilityAccount = Account.builder()
        .accountName("Liability Account")
        .accountType(AccountType.LIABILITY)
        .isActive(true)
        .build();
    liabilityAccount.setBalance(new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
        com.finance.smartLedger.shared.valueobject.Money.of(java.math.BigDecimal.valueOf(1000.00), "USD")));

    when(accountRepository.findAll()).thenReturn(List.of(assetAccount, liabilityAccount));

    boolean balanced = balanceService.isTrialBalanceBalanced();

    assertTrue(balanced);
    verify(accountRepository).findAll();
  }

  @Test
  void isTrialBalanceBalanced_Unbalanced_ReturnsFalse() {
    // Create unbalanced accounts
    Account assetAccount = Account.builder()
        .accountName("Asset Account")
        .accountType(AccountType.ASSET)
        .isActive(true)
        .build();
    assetAccount.setBalance(new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
        com.finance.smartLedger.shared.valueobject.Money.of(java.math.BigDecimal.valueOf(1000.00), "USD")));

    Account liabilityAccount = Account.builder()
        .accountName("Liability Account")
        .accountType(AccountType.LIABILITY)
        .isActive(true)
        .build();
    liabilityAccount.setBalance(new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
        com.finance.smartLedger.shared.valueobject.Money.of(java.math.BigDecimal.valueOf(500.00), "USD")));

    when(accountRepository.findAll()).thenReturn(List.of(assetAccount, liabilityAccount));

    boolean balanced = balanceService.isTrialBalanceBalanced();

    assertFalse(balanced);
    verify(accountRepository).findAll();
  }

  @Test
  void getAccountsWithNegativeBalance_Success() {
    when(accountRepository.findAll()).thenReturn(List.of(testAccount));

    List<Account> accounts = balanceService.getAccountsWithNegativeBalance();

    assertNotNull(accounts);
    assertTrue(accounts.isEmpty()); // Test account has positive balance
    verify(accountRepository).findAll();
  }

  @Test
  void getAccountsWithZeroBalance_Success() {
    when(accountRepository.findAll()).thenReturn(List.of(testAccount));

    List<Account> accounts = balanceService.getAccountsWithZeroBalance();

    assertNotNull(accounts);
    assertTrue(accounts.isEmpty()); // Test account has non-zero balance
    verify(accountRepository).findAll();
  }

  @Test
  void reconcileBalance_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

    balanceService.reconcileBalance(
        testId, Money.of(BigDecimal.valueOf(1200.00), "USD"), "Reconciliation", "test-user");

    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void reconcileBalance_NotFound_ThrowsException() {
    when(accountRepository.findById(testId)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          balanceService.reconcileBalance(
              testId, Money.of(BigDecimal.valueOf(1000.00), "USD"), "Reconciliation", "test-user");
        });

    verify(accountRepository, never()).save(any(Account.class));
  }
}
