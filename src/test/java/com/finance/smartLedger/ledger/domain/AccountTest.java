package com.finance.smartLedger.ledger.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.finance.smartLedger.ledger.domain.valueobject.AccountCode;
import com.finance.smartLedger.ledger.domain.valueobject.AccountNumber;
import com.finance.smartLedger.shared.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountTest {

  private Account account;
  private AccountNumber accountNumber;
  private AccountCode accountCode;
  private Money initialBalance;

  @BeforeEach
  void setUp() {
    accountNumber = AccountNumber.of("1234567890");
    accountCode = AccountCode.of("GL001");
    initialBalance = Money.of(1000.00, "USD");
    account =
        new Account(
            accountNumber,
            accountCode,
            "Test Account",
            AccountType.ASSET,
            initialBalance,
            "SYSTEM");
  }

  @Test
  void shouldCreateAccountWithRequiredFields() {
    assertEquals("1234567890", account.getAccountNumber().getValue());
    assertEquals("GL001", account.getAccountCode().getValue());
    assertEquals("Test Account", account.getAccountName());
    assertEquals(AccountType.ASSET, account.getAccountType());
    assertEquals(initialBalance, account.getBalance().getCurrentBalance());
    assertTrue(account.getIsActive());
    assertEquals("SYSTEM", account.getCreatedBy());
  }

  @Test
  void shouldDebitFromAccount() {
    Money debitAmount = Money.of(100.00, "USD");
    account.debit(debitAmount, "USER1");

    assertEquals(Money.of(1100.00, "USD"), account.getBalance().getCurrentBalance());
    assertEquals("USER1", account.getUpdatedBy());
  }

  @Test
  void shouldCreditToAccount() {
    Money creditAmount = Money.of(100.00, "USD");
    account.credit(creditAmount, "USER1");

    assertEquals(Money.of(900.00, "USD"), account.getBalance().getCurrentBalance());
    assertEquals("USER1", account.getUpdatedBy());
  }

  @Test
  void shouldRejectDebitFromInactiveAccount() {
    account.deactivate("ADMIN");
    Money debitAmount = Money.of(100.00, "USD");

    assertThrows(IllegalStateException.class, () -> account.debit(debitAmount, "USER1"));
  }

  @Test
  void shouldRejectCreditToInactiveAccount() {
    account.deactivate("ADMIN");
    Money creditAmount = Money.of(100.00, "USD");

    assertThrows(IllegalStateException.class, () -> account.credit(creditAmount, "USER1"));
  }

  @Test
  void shouldAllowDebitForAssetAccountWithSufficientBalance() {
    assertTrue(account.canDebit(Money.of(500.00, "USD")));
  }

  @Test
  void shouldRejectDebitForCreditAccountWithInsufficientBalance() {
    Account liabilityAccount =
        new Account(
            AccountNumber.of("0987654321"),
            AccountCode.of("LI001"),
            "Test Liability",
            AccountType.LIABILITY,
            Money.of(100.00, "USD"),
            "SYSTEM");

    assertFalse(liabilityAccount.canDebit(Money.of(200.00, "USD")));
  }

  @Test
  void shouldActivateAccount() {
    account.deactivate("ADMIN");
    account.activate("ADMIN");

    assertTrue(account.getIsActive());
  }

  @Test
  void shouldDeactivateAccount() {
    account.deactivate("ADMIN");

    assertFalse(account.getIsActive());
  }

  @Test
  void shouldGenerateAccountCreatedEvent() {
    var event = account.toAccountCreatedEvent();

    assertNotNull(event);
    assertEquals(account.getId(), event.getAccountId());
    assertEquals("1234567890", event.getAccountNumber());
    assertEquals("GL001", event.getAccountCode());
    assertEquals("Test Account", event.getAccountName());
    assertEquals("ASSET", event.getAccountType());
  }

  @Test
  void shouldGenerateBalanceUpdatedEvent() {
    account.debit(Money.of(100.00, "USD"), "USER1");
    var event = account.toBalanceUpdatedEvent("USER1");

    assertNotNull(event);
    assertEquals(account.getId(), event.getAccountId());
    assertEquals("1234567890", event.getAccountNumber());
  }
}
