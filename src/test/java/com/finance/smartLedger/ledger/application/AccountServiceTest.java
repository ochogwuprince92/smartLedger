package com.finance.smartLedger.ledger.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.shared.domain.EventPublisher;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @Mock private AccountRepository accountRepository;

  @Mock private EventPublisher eventPublisher;

  @InjectMocks private AccountService accountService;

  private Account assetAccount;
  private Account liabilityAccount;
  private Account revenueAccount;
  private UUID assetId;
  private UUID liabilityId;
  private UUID revenueId;

  @BeforeEach
  void setUp() {
    assetId = UUID.randomUUID();
    liabilityId = UUID.randomUUID();
    revenueId = UUID.randomUUID();
    
    assetAccount =
        Account.builder()
            .accountName("Asset Account")
            .accountType(AccountType.ASSET)
            .isActive(true)
            .build();
    assetAccount.setId(assetId);
    assetAccount.setAccountNumber(
        com.finance.smartLedger.ledger.domain.valueobject.AccountNumber.of("10000001"));
    assetAccount.setAccountCode(
        com.finance.smartLedger.ledger.domain.valueobject.AccountCode.of("GL001"));
    assetAccount.setBalance(
        new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
            com.finance.smartLedger.shared.valueobject.Money.of(
                java.math.BigDecimal.valueOf(1000.00), "USD")));
    
    liabilityAccount =
        Account.builder()
            .accountName("Liability Account")
            .accountType(AccountType.LIABILITY)
            .isActive(true)
            .build();
    liabilityAccount.setId(liabilityId);
    liabilityAccount.setAccountNumber(
        com.finance.smartLedger.ledger.domain.valueobject.AccountNumber.of("20000001"));
    liabilityAccount.setAccountCode(
        com.finance.smartLedger.ledger.domain.valueobject.AccountCode.of("GL002"));
    liabilityAccount.setBalance(
        new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
            com.finance.smartLedger.shared.valueobject.Money.of(
                java.math.BigDecimal.valueOf(500.00), "USD")));
    
    revenueAccount =
        Account.builder()
            .accountName("Revenue Account")
            .accountType(AccountType.REVENUE)
            .isActive(true)
            .build();
    revenueAccount.setId(revenueId);
    revenueAccount.setAccountNumber(
        com.finance.smartLedger.ledger.domain.valueobject.AccountNumber.of("40000001"));
    revenueAccount.setAccountCode(
        com.finance.smartLedger.ledger.domain.valueobject.AccountCode.of("GL003"));
    revenueAccount.setBalance(
        new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
            com.finance.smartLedger.shared.valueobject.Money.of(
                java.math.BigDecimal.valueOf(2000.00), "USD")));
  }

  @Test
  void createAccount_Success() {
    when(accountRepository.existsByAccountNumber_Value("10000001")).thenReturn(false);
    when(accountRepository.existsByAccountCode_Value("GL001")).thenReturn(false);
    when(accountRepository.save(any(Account.class))).thenReturn(assetAccount);

    Account result =
        accountService.createAccount(
            "10000001",
            "GL001",
            "Test Account",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");

    assertNotNull(result);
    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void createAccount_DuplicateAccountNumber_ThrowsException() {
    when(accountRepository.existsByAccountNumber_Value("10000001")).thenReturn(true);

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          accountService.createAccount(
              "10000001",
              "GL001",
              "Test Account",
              AccountType.LIABILITY,
              Money.of(BigDecimal.valueOf(500.00), "USD"),
              "test-user");
        });

    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void createAccount_DuplicateAccountCode_ThrowsException() {
    when(accountRepository.existsByAccountNumber_Value(anyString())).thenReturn(false);
    when(accountRepository.existsByAccountCode_Value("GL001")).thenReturn(true);

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          accountService.createAccount(
              "10000002",
              "GL001",
              "Test Account",
              AccountType.REVENUE,
              Money.of(BigDecimal.valueOf(2000.00), "USD"),
              "test-user");
        });

    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void findById_Success() {
    when(accountRepository.findById(assetId)).thenReturn(Optional.of(assetAccount));

    Optional<Account> result = accountService.findById(assetId);

    assertTrue(result.isPresent());
    assertEquals(assetId, result.get().getId());
  }

  @Test
  void findById_NotFound_ReturnsEmpty() {
    when(accountRepository.findById(assetId)).thenReturn(Optional.empty());

    Optional<Account> result = accountService.findById(assetId);

    assertFalse(result.isPresent());
  }

  @Test
  void findByAccountNumber_Success() {
    when(accountRepository.findByAccountNumber_Value("10000001"))
        .thenReturn(Optional.of(assetAccount));

    Optional<Account> result = accountService.findByAccountNumber("10000001");

    assertTrue(result.isPresent());
  }

  @Test
  void findByAccountCode_Success() {
    when(accountRepository.findByAccountCode_Value("GL001")).thenReturn(Optional.of(assetAccount));

    Optional<Account> result = accountService.findByAccountCode("GL001");

    assertTrue(result.isPresent());
  }

  @Test
  void findByAccountType_Success() {
    when(accountRepository.findByAccountType(AccountType.ASSET)).thenReturn(List.of(assetAccount));
    when(accountRepository.findByAccountType(AccountType.LIABILITY)).thenReturn(List.of(liabilityAccount));
    when(accountRepository.findByAccountType(AccountType.REVENUE)).thenReturn(List.of(revenueAccount));

    List<Account> assetResult = accountService.findByAccountType(AccountType.ASSET);
    List<Account> liabilityResult = accountService.findByAccountType(AccountType.LIABILITY);
    List<Account> revenueResult = accountService.findByAccountType(AccountType.REVENUE);

    assertEquals(1, assetResult.size());
    assertEquals(AccountType.ASSET, assetResult.get(0).getAccountType());
    assertEquals(1, liabilityResult.size());
    assertEquals(AccountType.LIABILITY, liabilityResult.get(0).getAccountType());
    assertEquals(1, revenueResult.size());
    assertEquals(AccountType.REVENUE, revenueResult.get(0).getAccountType());
  }

  @Test
  void findActiveAccounts_Success() {
    when(accountRepository.findByIsActiveTrue()).thenReturn(List.of(assetAccount, liabilityAccount, revenueAccount));

    List<Account> result = accountService.findActiveAccounts();

    assertEquals(3, result.size());
  }

  @Test
  void findByParentAccountId_Success() {
    UUID parentId = UUID.randomUUID();
    when(accountRepository.findByParentAccountId(parentId)).thenReturn(List.of(assetAccount));

    List<Account> result = accountService.findByParentAccountId(parentId);

    assertEquals(1, result.size());
  }

  @Test
  void findAllAccounts_Success() {
    when(accountRepository.findAll()).thenReturn(List.of(assetAccount, liabilityAccount, revenueAccount));

    List<Account> result = accountService.findAllAccounts();

    assertEquals(3, result.size());
  }

  @Test
  void updateAccount_Success() {
    when(accountRepository.findById(assetId)).thenReturn(Optional.of(assetAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(assetAccount);

    Account result =
        accountService.updateAccount(assetId, "Updated Name", "Updated Description", "test-user");

    assertNotNull(result);
    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void updateAccount_NotFound_ThrowsException() {
    when(accountRepository.findById(assetId)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          accountService.updateAccount(assetId, "Updated Name", "Updated Description", "test-user");
        });

    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void activateAccount_Success() {
    when(accountRepository.findById(liabilityId)).thenReturn(Optional.of(liabilityAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(liabilityAccount);

    accountService.activateAccount(liabilityId, "test-user");

    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void deactivateAccount_Success() {
    when(accountRepository.findById(revenueId)).thenReturn(Optional.of(revenueAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(revenueAccount);

    accountService.deactivateAccount(revenueId, "test-user");

    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void debitAccount_Success() {
    when(accountRepository.findById(assetId)).thenReturn(Optional.of(assetAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(assetAccount);

    accountService.debitAccount(assetId, Money.of(BigDecimal.valueOf(100.00), "USD"), "test-user");

    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void creditAccount_Success() {
    when(accountRepository.findById(revenueId)).thenReturn(Optional.of(revenueAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(revenueAccount);

    accountService.creditAccount(revenueId, Money.of(BigDecimal.valueOf(100.00), "USD"), "test-user");

    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void deleteAccount_Success() {
    when(accountRepository.findById(assetId)).thenReturn(Optional.of(assetAccount));
    when(accountRepository.findByParentAccountId(assetId)).thenReturn(List.of());
    doNothing().when(accountRepository).delete(any(Account.class));

    accountService.deleteAccount(assetId);

    verify(accountRepository).delete(any(Account.class));
  }

  @Test
  void deleteAccount_NotFound_ThrowsException() {
    when(accountRepository.findById(assetId)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          accountService.deleteAccount(assetId);
        });

    verify(accountRepository, never()).delete(any(Account.class));
  }

  @Test
  void deleteAccount_WithChildren_ThrowsException() {
    when(accountRepository.findById(assetId)).thenReturn(Optional.of(assetAccount));
    when(accountRepository.findByParentAccountId(assetId)).thenReturn(List.of(liabilityAccount));

    assertThrows(
        IllegalStateException.class,
        () -> {
          accountService.deleteAccount(assetId);
        });

    verify(accountRepository, never()).delete(any(Account.class));
  }

  @Test
  void getAccountBalance_Success() {
    when(accountRepository.findById(liabilityId)).thenReturn(Optional.of(liabilityAccount));

    Money balance = accountService.getAccountBalance(liabilityId);

    assertEquals(Money.of(BigDecimal.valueOf(500.00), "USD"), balance);
  }

  @Test
  void getAccountBalanceDetails_Success() {
    when(accountRepository.findById(revenueId)).thenReturn(Optional.of(revenueAccount));

    var balance = accountService.getAccountBalanceDetails(revenueId);

    assertNotNull(balance);
    assertEquals(Money.of(BigDecimal.valueOf(2000.00), "USD"), balance.getCurrentBalance());
  }
}
