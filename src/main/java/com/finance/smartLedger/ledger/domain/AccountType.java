package com.finance.smartLedger.ledger.domain;

public enum AccountType {
  ASSET("Asset accounts represent resources owned by the business"),
  LIABILITY("Liability accounts represent obligations of the business"),
  EQUITY("Equity accounts represent owner's interest in the business"),
  REVENUE("Revenue accounts represent income from business operations"),
  EXPENSE("Expense accounts represent costs of business operations"),
  GAIN("Gain accounts represent unrealized gains"),
  LOSS("Loss accounts represent unrealized losses");

  private final String description;

  AccountType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public boolean isBalanceSheetAccount() {
    return this == ASSET || this == LIABILITY || this == EQUITY;
  }

  public boolean isIncomeStatementAccount() {
    return this == REVENUE || this == EXPENSE || this == GAIN || this == LOSS;
  }

  public boolean isDebitAccount() {
    return this == ASSET || this == EXPENSE || this == LOSS;
  }

  public boolean isCreditAccount() {
    return this == LIABILITY || this == EQUITY || this == REVENUE || this == GAIN;
  }
}
