package com.finance.smartLedger.ledger.domain.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AccountCodeTest {

  @Test
  void shouldCreateValidAccountCode() {
    AccountCode accountCode = AccountCode.of("GL001");
    assertEquals("GL001", accountCode.getValue());
  }

  @Test
  void shouldConvertToUpperCase() {
    AccountCode accountCode = AccountCode.of("gl001");
    assertEquals("GL001", accountCode.getValue());
  }

  @Test
  void shouldRejectNullAccountCode() {
    assertThrows(NullPointerException.class, () -> AccountCode.of(null));
  }

  @Test
  void shouldRejectEmptyAccountCode() {
    assertThrows(IllegalArgumentException.class, () -> AccountCode.of(""));
  }

  @Test
  void shouldRejectAccountCodeWithLettersOnly() {
    assertThrows(IllegalArgumentException.class, () -> AccountCode.of("GL"));
  }

  @Test
  void shouldRejectAccountCodeWithDigitsOnly() {
    assertThrows(IllegalArgumentException.class, () -> AccountCode.of("123"));
  }

  @Test
  void shouldRejectAccountCodeWithTooManyLetters() {
    assertThrows(IllegalArgumentException.class, () -> AccountCode.of("GLABC123"));
  }

  @Test
  void shouldRejectAccountCodeWithTooManyDigits() {
    assertThrows(IllegalArgumentException.class, () -> AccountCode.of("GL1234567"));
  }

  @Test
  void shouldRejectAccountCodeWithSpecialCharacters() {
    assertThrows(IllegalArgumentException.class, () -> AccountCode.of("GL-001"));
  }

  @Test
  void shouldImplementEqualsAndHashCode() {
    AccountCode accountCode1 = AccountCode.of("GL001");
    AccountCode accountCode2 = AccountCode.of("GL001");
    AccountCode accountCode3 = AccountCode.of("SACC123");

    assertEquals(accountCode1, accountCode2);
    assertEquals(accountCode1.hashCode(), accountCode2.hashCode());
    assertNotEquals(accountCode1, accountCode3);
  }

  @Test
  void shouldReturnCorrectToString() {
    AccountCode accountCode = AccountCode.of("GL001");
    assertEquals("GL001", accountCode.toString());
  }
}
