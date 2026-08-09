package com.finance.smartLedger.ledger.application;

import static org.junit.jupiter.api.Assertions.*;

import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.util.List;
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
    "spring.data.redis.enabled=false",
    "spring.cache.type=none",
    "app.scheduled.enabled=false",
    "app.data-loader.enabled=false"
})
@Disabled("Docker not available on this system")
@Testcontainers
class AccountServiceIntegrationTest {

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    TestDatabaseConfiguration.configureDatabase(registry);
  }

  @Autowired private AccountService accountService;

  @Autowired private AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    accountRepository.deleteAll();
  }

  @AfterEach
  void tearDown() {
    accountRepository.deleteAll();
  }

  @Test
  void createAccount_Success() {
    Account account =
        accountService.createAccount(
            "1001",
            "1001",
            "Test Account",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");

    assertNotNull(account);
    assertNotNull(account.getId());
    assertEquals("1001", account.getAccountNumber().getValue());
    assertEquals("1001", account.getAccountCode().getValue());
    assertEquals("Test Account", account.getAccountName());
    assertEquals(AccountType.ASSET, account.getAccountType());
    assertTrue(account.getIsActive());
  }

  @Test
  void createAccount_DuplicateAccountNumber_ThrowsException() {
    accountService.createAccount(
        "1001",
        "1001",
        "Test Account",
        AccountType.ASSET,
        Money.of(BigDecimal.valueOf(1000.00), "USD"),
        "test-user");

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          accountService.createAccount(
              "1001",
              "1002",
              "Another Account",
              AccountType.ASSET,
              Money.of(BigDecimal.valueOf(500.00), "USD"),
              "test-user");
        });
  }

  @Test
  void createAccount_DuplicateAccountCode_ThrowsException() {
    accountService.createAccount(
        "1001",
        "1001",
        "Test Account",
        AccountType.ASSET,
        Money.of(BigDecimal.valueOf(1000.00), "USD"),
        "test-user");

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          accountService.createAccount(
              "1002",
              "1001",
              "Another Account",
              AccountType.ASSET,
              Money.of(BigDecimal.valueOf(500.00), "USD"),
              "test-user");
        });
  }

  @Test
  void findById_Success() {
    Account created =
        accountService.createAccount(
            "1001",
            "1001",
            "Test Account",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");

    var found = accountService.findById(created.getId());

    assertTrue(found.isPresent());
    assertEquals(created.getId(), found.get().getId());
  }

  @Test
  void findByAccountNumber_Success() {
    accountService.createAccount(
        "1001",
        "1001",
        "Test Account",
        AccountType.ASSET,
        Money.of(BigDecimal.valueOf(1000.00), "USD"),
        "test-user");

    var found = accountService.findByAccountNumber("1001");

    assertTrue(found.isPresent());
    assertEquals("1001", found.get().getAccountNumber().getValue());
  }

  @Test
  void findByAccountCode_Success() {
    accountService.createAccount(
        "1001",
        "1001",
        "Test Account",
        AccountType.ASSET,
        Money.of(BigDecimal.valueOf(1000.00), "USD"),
        "test-user");

    var found = accountService.findByAccountCode("1001");

    assertTrue(found.isPresent());
    assertEquals("1001", found.get().getAccountCode().getValue());
  }

  @Test
  void findByAccountType_Success() {
    accountService.createAccount(
        "1001",
        "1001",
        "Asset 1",
        AccountType.ASSET,
        Money.of(BigDecimal.valueOf(1000.00), "USD"),
        "test-user");
    accountService.createAccount(
        "1002",
        "1002",
        "Asset 2",
        AccountType.ASSET,
        Money.of(BigDecimal.valueOf(2000.00), "USD"),
        "test-user");
    accountService.createAccount(
        "2001",
        "2001",
        "Liability 1",
        AccountType.LIABILITY,
        Money.of(BigDecimal.valueOf(500.00), "USD"),
        "test-user");

    List<Account> assetAccounts = accountService.findByAccountType(AccountType.ASSET);

    assertEquals(2, assetAccounts.size());
    assertTrue(assetAccounts.stream().allMatch(a -> a.getAccountType() == AccountType.ASSET));
  }

  @Test
  void findActiveAccounts_Success() {
    Account active1 =
        accountService.createAccount(
            "1001",
            "1001",
            "Active 1",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");
    Account active2 =
        accountService.createAccount(
            "1002",
            "1002",
            "Active 2",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(2000.00), "USD"),
            "test-user");

    accountService.deactivateAccount(active1.getId(), "test-user");

    List<Account> activeAccounts = accountService.findActiveAccounts();

    assertEquals(1, activeAccounts.size());
    assertEquals(active2.getId(), activeAccounts.get(0).getId());
  }

  @Test
  void updateAccount_Success() {
    Account created =
        accountService.createAccount(
            "1001",
            "1001",
            "Test Account",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");

    Account updated =
        accountService.updateAccount(
            created.getId(), "Updated Account Name", "Updated description", "test-user");

    assertEquals("Updated Account Name", updated.getAccountName());
    assertEquals("Updated description", updated.getDescription());
  }

  @Test
  void activateAccount_Success() {
    Account created =
        accountService.createAccount(
            "1001",
            "1001",
            "Test Account",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");

    accountService.deactivateAccount(created.getId(), "test-user");
    accountService.activateAccount(created.getId(), "test-user");

    Account reactivated = accountService.findById(created.getId()).orElseThrow();
    assertTrue(reactivated.getIsActive());
  }

  @Test
  void deactivateAccount_Success() {
    Account created =
        accountService.createAccount(
            "1001",
            "1001",
            "Test Account",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");

    accountService.deactivateAccount(created.getId(), "test-user");

    Account deactivated = accountService.findById(created.getId()).orElseThrow();
    assertFalse(deactivated.getIsActive());
  }

  @Test
  void debitAccount_Success() {
    Account created =
        accountService.createAccount(
            "1001",
            "1001",
            "Test Account",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");

    accountService.debitAccount(
        created.getId(), Money.of(BigDecimal.valueOf(100.00), "USD"), "test-user");

    Account debited = accountService.findById(created.getId()).orElseThrow();
    assertEquals(BigDecimal.valueOf(900.00), debited.getBalance().getCurrentBalance().getAmount());
  }

  @Test
  void creditAccount_Success() {
    Account created =
        accountService.createAccount(
            "1001",
            "1001",
            "Test Account",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");

    accountService.creditAccount(
        created.getId(), Money.of(BigDecimal.valueOf(100.00), "USD"), "test-user");

    Account credited = accountService.findById(created.getId()).orElseThrow();
    assertEquals(
        BigDecimal.valueOf(1100.00), credited.getBalance().getCurrentBalance().getAmount());
  }

  @Test
  void deleteAccount_Success() {
    Account created =
        accountService.createAccount(
            "1001",
            "1001",
            "Test Account",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");

    accountService.deleteAccount(created.getId());

    var deleted = accountService.findById(created.getId());
    assertFalse(deleted.isPresent());
  }

  @Test
  void deleteAccount_WithChildren_ThrowsException() {
    Account parent =
        accountService.createAccount(
            "1000",
            "1000",
            "Parent",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");
    Account child =
        accountService.createAccount(
            "1001",
            "1001",
            "Child",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(500.00), "USD"),
            "test-user");

    child.setParentAccountId(parent.getId());
    accountRepository.save(child);

    assertThrows(
        IllegalStateException.class,
        () -> {
          accountService.deleteAccount(parent.getId());
        });
  }

  @Test
  void getAccountBalance_Success() {
    Account created =
        accountService.createAccount(
            "1001",
            "1001",
            "Test Account",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");

    Money balance = accountService.getAccountBalance(created.getId());

    assertEquals(BigDecimal.valueOf(1000.00), balance.getAmount());
  }
}
