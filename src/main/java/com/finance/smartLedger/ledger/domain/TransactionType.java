package com.finance.smartLedger.ledger.domain;

public enum TransactionType {
  FEE_PAYMENT("Fee payment transaction"),
  FEE_REFUND("Fee refund transaction"),
  SCHOLARSHIP("Scholarship transaction"),
  DISCOUNT("Discount transaction"),
  PAYMENT("Payment transaction"),
  PAYMENT_REFUND("Payment refund transaction"),
  JOURNAL_ENTRY("General journal entry"),
  ADJUSTMENT("Account adjustment");

  private final String description;

  TransactionType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
