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
    testAccount.setAccountNumber(
        com.finance.smartLedger.ledger.domain.valueobject.AccountNumber.of("10000001"));
    testAccount.setAccountCode(
        com.finance.smartLedger.ledger.domain.valueobject.AccountCode.of("GL001"));
    testAccount.setBalance(
        new com.finance.smartLedger.ledger.domain.valueobject.AccountBalance(
            com.finance.smartLedger.shared.valueobject.Money.of(
                java.math.BigDecimal.valueOf(1000.00), "USD")));
  }

  @Test
  void createAccount_Success() {
    when(accountRepository.existsByAccountNumber_Value("10000001")).thenReturn(false);
    when(accountRepository.existsByAccountCode_Value("GL001")).thenReturn(false);
    when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

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
              AccountType.ASSET,
              Money.of(BigDecimal.valueOf(1000.00), "USD"),
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
              AccountType.ASSET,
              Money.of(BigDecimal.valueOf(1000.00), "USD"),
              "test-user");
        });

    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void findById_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));

    Optional<Account> result = accountService.findById(testId);

    assertTrue(result.isPresent());
    assertEquals(testId, result.get().getId());
  }

  @Test
  void findById_NotFound_ReturnsEmpty() {
    when(accountRepository.findById(testId)).thenReturn(Optional.empty());

    Optional<Account> result = accountService.findById(testId);

    assertFalse(result.isPresent());
  }

  @Test
  void findByAccountNumber_Success() {
    when(accountRepository.findByAccountNumber_Value("10000001"))
        .thenReturn(Optional.of(testAccount));

    Optional<Account> result = accountService.findByAccountNumber("10000001");

    assertTrue(result.isPresent());
  }

  @Test
  void findByAccountCode_Success() {
    when(accountRepository.findByAccountCode_Value("GL001")).thenReturn(Optional.of(testAccount));

    Optional<Account> result = accountService.findByAccountCode("GL001");

    assertTrue(result.isPresent());
  }

  @Test
  void findByAccountType_Success() {
    when(accountRepository.findByAccountType(AccountType.ASSET)).thenReturn(List.of(testAccount));

    List<Account> result = accountService.findByAccountType(AccountType.ASSET);

    assertEquals(1, result.size());
    assertEquals(AccountType.ASSET, result.get(0).getAccountType());
  }

  @Test
  void findActiveAccounts_Success() {
    when(accountRepository.findByIsActiveTrue()).thenReturn(List.of(testAccount));

    List<Account> result = accountService.findActiveAccounts();

    assertEquals(1, result.size());
  }

  @Test
  void findByParentAccountId_Success() {
    UUID parentId = UUID.randomUUID();
    when(accountRepository.findByParentAccountId(parentId)).thenReturn(List.of(testAccount));

    List<Account> result = accountService.findByParentAccountId(parentId);

    assertEquals(1, result.size());
  }

  @Test
  void findAllAccounts_Success() {
    when(accountRepository.findAll()).thenReturn(List.of(testAccount));

    List<Account> result = accountService.findAllAccounts();

    assertEquals(1, result.size());
  }

  @Test
  void updateAccount_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

    Account result =
        accountService.updateAccount(testId, "Updated Name", "Updated Description", "test-user");

    assertNotNull(result);
    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void updateAccount_NotFound_ThrowsException() {
    when(accountRepository.findById(testId)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          accountService.updateAccount(testId, "Updated Name", "Updated Description", "test-user");
        });

    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void activateAccount_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

    accountService.activateAccount(testId, "test-user");

    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void deactivateAccount_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

    accountService.deactivateAccount(testId, "test-user");

    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void debitAccount_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

    accountService.debitAccount(testId, Money.of(BigDecimal.valueOf(100.00), "USD"), "test-user");

    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void creditAccount_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

    accountService.creditAccount(testId, Money.of(BigDecimal.valueOf(100.00), "USD"), "test-user");

    verify(accountRepository).save(any(Account.class));
  }

  @Test
  void deleteAccount_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.findByParentAccountId(testId)).thenReturn(List.of());
    doNothing().when(accountRepository).delete(any(Account.class));

    accountService.deleteAccount(testId);

    verify(accountRepository).delete(any(Account.class));
  }

  @Test
  void deleteAccount_NotFound_ThrowsException() {
    when(accountRepository.findById(testId)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          accountService.deleteAccount(testId);
        });

    verify(accountRepository, never()).delete(any(Account.class));
  }

  @Test
  void deleteAccount_WithChildren_ThrowsException() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));
    when(accountRepository.findByParentAccountId(testId)).thenReturn(List.of(testAccount));

    assertThrows(
        IllegalStateException.class,
        () -> {
          accountService.deleteAccount(testId);
        });

    verify(accountRepository, never()).delete(any(Account.class));
  }

  @Test
  void getAccountBalance_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));

    Money balance = accountService.getAccountBalance(testId);

    assertNotNull(balance);
  }

  @Test
  void getAccountBalanceDetails_Success() {
    when(accountRepository.findById(testId)).thenReturn(Optional.of(testAccount));

    var balance = accountService.getAccountBalanceDetails(testId);

    assertNotNull(balance);
  }
}
