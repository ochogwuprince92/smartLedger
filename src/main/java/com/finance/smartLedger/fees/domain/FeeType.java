package com.finance.smartLedger.fees.domain;

import com.finance.smartLedger.ledger.domain.AccountType;

public enum FeeType {
  TUITION_FEE("Tuition Fee", "Standard tuition fees for academic programs", AccountType.REVENUE),
  REGISTRATION_FEE(
      "Registration Fee", "One-time enrollment and registration charges", AccountType.REVENUE),
  HOSTEL_FEE("Hostel Fee", "Accommodation and boarding fees", AccountType.REVENUE),
  LIBRARY_FEE("Library Fee", "Library access and resource fees", AccountType.REVENUE),
  LAB_FEE("Laboratory Fee", "Science and computer lab usage fees", AccountType.REVENUE),
  SPORTS_FEE("Sports Fee", "Sports facilities and equipment fees", AccountType.REVENUE),
  TRANSPORT_FEE("Transport Fee", "School bus and transportation fees", AccountType.REVENUE),
  EXAMINATION_FEE("Examination Fee", "Examination and assessment fees", AccountType.REVENUE),
  DEVELOPMENT_FEE("Development Fee", "Infrastructure and development fund", AccountType.REVENUE),
  SCHOLARSHIP("Scholarship", "Student scholarships and financial aid", AccountType.EXPENSE),
  DISCOUNT("Discount", "Fee discounts and waivers", AccountType.EXPENSE),
  LATE_PAYMENT_PENALTY(
      "Late Payment Penalty", "Penalty for late fee payments", AccountType.REVENUE),
  REFUND("Refund", "Fee refunds to students", AccountType.EXPENSE),
  MISCELLANEOUS_FEE("Miscellaneous Fee", "Other miscellaneous charges", AccountType.REVENUE);

  private final String displayName;
  private final String description;
  private final AccountType accountType;

  FeeType(String displayName, String description, AccountType accountType) {
    this.displayName = displayName;
    this.description = description;
    this.accountType = accountType;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  public AccountType getAccountType() {
    return accountType;
  }

  public boolean isRevenue() {
    return accountType == AccountType.REVENUE;
  }

  public boolean isExpense() {
    return accountType == AccountType.EXPENSE;
  }

  public boolean isMandatoryFee() {
    return this == TUITION_FEE || this == REGISTRATION_FEE;
  }

  public boolean isOptionalFee() {
    return !isMandatoryFee();
  }
}
