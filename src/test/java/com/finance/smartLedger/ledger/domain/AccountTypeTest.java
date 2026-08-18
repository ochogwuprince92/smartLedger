package com.finance.smartLedger.ledger.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AccountTypeTest {

  @Test
  void shouldHaveCorrectDescriptions() {
    assertEquals(
        "Asset accounts represent resources owned by the business",
        AccountType.ASSET.getDescription());
    assertEquals(
        "Liability accounts represent obligations of the business",
        AccountType.LIABILITY.getDescription());
    assertEquals(
        "Equity accounts represent owner's interest in the business",
        AccountType.EQUITY.getDescription());
  }

  @Test
  void shouldIdentifyBalanceSheetAccounts() {
    assertTrue(AccountType.ASSET.isBalanceSheetAccount());
    assertTrue(AccountType.LIABILITY.isBalanceSheetAccount());
    assertTrue(AccountType.EQUITY.isBalanceSheetAccount());
    assertFalse(AccountType.REVENUE.isBalanceSheetAccount());
    assertFalse(AccountType.EXPENSE.isBalanceSheetAccount());
  }

  @Test
  void shouldIdentifyIncomeStatementAccounts() {
    assertTrue(AccountType.REVENUE.isIncomeStatementAccount());
    assertTrue(AccountType.EXPENSE.isIncomeStatementAccount());
    assertTrue(AccountType.GAIN.isIncomeStatementAccount());
    assertTrue(AccountType.LOSS.isIncomeStatementAccount());
    assertFalse(AccountType.ASSET.isIncomeStatementAccount());
  }

  @Test
  void shouldIdentifyDebitAccounts() {
    assertTrue(AccountType.ASSET.isDebitAccount());
    assertTrue(AccountType.EXPENSE.isDebitAccount());
    assertTrue(AccountType.LOSS.isDebitAccount());
    assertFalse(AccountType.LIABILITY.isDebitAccount());
    assertFalse(AccountType.EQUITY.isDebitAccount());
  }

  @Test
  void shouldIdentifyCreditAccounts() {
    assertTrue(AccountType.LIABILITY.isCreditAccount());
    assertTrue(AccountType.EQUITY.isCreditAccount());
    assertTrue(AccountType.REVENUE.isCreditAccount());
    assertTrue(AccountType.GAIN.isCreditAccount());
    assertFalse(AccountType.ASSET.isCreditAccount());
    assertFalse(AccountType.EXPENSE.isCreditAccount());
  }
}
