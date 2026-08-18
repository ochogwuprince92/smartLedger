package com.finance.smartLedger.ledger.domain.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AccountNumberTest {

  @Test
  void shouldCreateValidAccountNumber() {
    AccountNumber accountNumber = AccountNumber.of("12345678");
    assertEquals("12345678", accountNumber.getValue());
  }

  @Test
  void shouldRejectNullAccountNumber() {
    assertThrows(IllegalArgumentException.class, () -> AccountNumber.of(null));
  }

  @Test
  void shouldRejectEmptyAccountNumber() {
    assertThrows(IllegalArgumentException.class, () -> AccountNumber.of(""));
  }

  @Test
  void shouldRejectAccountNumberWithLessThan8Digits() {
    assertThrows(IllegalArgumentException.class, () -> AccountNumber.of("1234567"));
  }

  @Test
  void shouldRejectAccountNumberWithMoreThan20Digits() {
    assertThrows(IllegalArgumentException.class, () -> AccountNumber.of("123456789012345678901"));
  }

  @Test
  void shouldRejectAccountNumberWithLetters() {
    assertThrows(IllegalArgumentException.class, () -> AccountNumber.of("AB123456"));
  }

  @Test
  void shouldRejectAccountNumberWithSpecialCharacters() {
    assertThrows(IllegalArgumentException.class, () -> AccountNumber.of("123-45678"));
  }

  @Test
  void shouldImplementEqualsAndHashCode() {
    AccountNumber accountNumber1 = AccountNumber.of("12345678");
    AccountNumber accountNumber2 = AccountNumber.of("12345678");
    AccountNumber accountNumber3 = AccountNumber.of("87654321");

    assertEquals(accountNumber1, accountNumber2);
    assertEquals(accountNumber1.hashCode(), accountNumber2.hashCode());
    assertNotEquals(accountNumber1, accountNumber3);
  }

  @Test
  void shouldReturnCorrectToString() {
    AccountNumber accountNumber = AccountNumber.of("12345678");
    assertEquals("12345678", accountNumber.toString());
  }
}
