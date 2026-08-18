package com.finance.smartLedger.fees.application;

import com.finance.smartLedger.fees.domain.FeePayment;
import com.finance.smartLedger.fees.domain.FeeType;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.domain.Transaction;
import com.finance.smartLedger.ledger.domain.TransactionType;
import com.finance.smartLedger.ledger.domain.valueobject.AccountCode;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.ledger.infrastructure.persistence.TransactionRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeeAccountingService {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  @Transactional
  public Transaction recordFeePayment(FeePayment feePayment) {
    Account feeRevenueAccount = getOrCreateFeeAccount(feePayment.getFeeType());
    Account cashAccount = getCashAccount();

    Transaction transaction =
        Transaction.builder()
            .type(TransactionType.FEE_PAYMENT)
            .description(
                "Fee payment: "
                    + feePayment.getFeeType().getDisplayName()
                    + " for student "
                    + feePayment.getStudentId())
            .amount(feePayment.getAmount())
            .debitAccount(cashAccount)
            .creditAccount(feeRevenueAccount)
            .referenceNumber(feePayment.getReferenceNumber())
            .build();

    return transactionRepository.save(transaction);
  }

  @Transactional
  public Transaction recordFeeRefund(FeePayment feePayment, String reason) {
    Account feeRevenueAccount = getOrCreateFeeAccount(feePayment.getFeeType());
    Account cashAccount = getCashAccount();

    Transaction transaction =
        Transaction.builder()
            .type(TransactionType.FEE_REFUND)
            .description(
                "Fee refund: "
                    + feePayment.getFeeType().getDisplayName()
                    + " for student "
                    + feePayment.getStudentId()
                    + " - "
                    + reason)
            .amount(feePayment.getAmount())
            .debitAccount(feeRevenueAccount)
            .creditAccount(cashAccount)
            .referenceNumber(feePayment.getReferenceNumber())
            .build();

    return transactionRepository.save(transaction);
  }

  @Transactional
  public Transaction recordScholarship(FeeType feeType, Money amount, UUID studentId) {
    Account scholarshipExpenseAccount = getOrCreateFeeAccount(FeeType.SCHOLARSHIP);
    Account cashAccount = getCashAccount();

    Transaction transaction =
        Transaction.builder()
            .type(TransactionType.SCHOLARSHIP)
            .description("Scholarship: " + feeType.getDisplayName() + " for student " + studentId)
            .amount(amount)
            .debitAccount(scholarshipExpenseAccount)
            .creditAccount(cashAccount)
            .build();

    return transactionRepository.save(transaction);
  }

  @Transactional
  public Transaction recordDiscount(FeeType feeType, Money amount, UUID studentId, String reason) {
    Account discountExpenseAccount = getOrCreateFeeAccount(FeeType.DISCOUNT);
    Account cashAccount = getCashAccount();

    Transaction transaction =
        Transaction.builder()
            .type(TransactionType.DISCOUNT)
            .description(
                "Discount: "
                    + feeType.getDisplayName()
                    + " for student "
                    + studentId
                    + " - "
                    + reason)
            .amount(amount)
            .debitAccount(discountExpenseAccount)
            .creditAccount(cashAccount)
            .build();

    return transactionRepository.save(transaction);
  }

  private Account getOrCreateFeeAccount(FeeType feeType) {
    String accountCode = "FEE" + feeType.name().substring(0, 2);
    String accountName = feeType.getDisplayName();

    Optional<Account> existingAccount = accountRepository.findByAccountCode_Value(accountCode);

    if (existingAccount.isPresent()) {
      return existingAccount.get();
    }

    Account newAccount =
        Account.builder()
            .accountCode(AccountCode.of(accountCode))
            .accountName(accountName)
            .accountType(feeType.getAccountType())
            .description(feeType.getDescription())
            .build();

    return accountRepository.save(newAccount);
  }

  private Account getCashAccount() {
    Optional<Account> cashAccount = accountRepository.findByAccountCode_Value("CASH01");

    if (cashAccount.isPresent()) {
      return cashAccount.get();
    }

    Account newAccount =
        Account.builder()
            .accountCode(AccountCode.of("CASH01"))
            .accountName("Cash Account")
            .accountType(AccountType.ASSET)
            .description("Main cash account for fee collections")
            .build();

    return accountRepository.save(newAccount);
  }

  public AccountType getAccountTypeForFee(FeeType feeType) {
    return feeType.getAccountType();
  }

  public boolean isRevenueFee(FeeType feeType) {
    return feeType.isRevenue();
  }

  public boolean isExpenseFee(FeeType feeType) {
    return feeType.isExpense();
  }
}
