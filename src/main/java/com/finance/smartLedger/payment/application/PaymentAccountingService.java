package com.finance.smartLedger.payment.application;

import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.domain.Transaction;
import com.finance.smartLedger.ledger.domain.TransactionType;
import com.finance.smartLedger.ledger.domain.valueobject.AccountCode;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.ledger.infrastructure.persistence.TransactionRepository;
import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import com.finance.smartLedger.shared.valueobject.Money;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentAccountingService {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  @Transactional
  public Transaction recordPayment(Payment payment) {
    // Over-payment guard: check if payment exceeds any associated invoice balance
    if (payment.getGatewayReference() != null) {
      checkOverPaymentGuard(payment);
    }

    Account cashAccount = getCashAccount(payment.getPaymentMethod());
    Account accountsReceivableAccount = getAccountsReceivableAccount();

    Transaction transaction =
        Transaction.builder()
            .type(TransactionType.PAYMENT)
            .description(
                "Payment received: "
                    + payment.getPaymentNumber()
                    + " from "
                    + payment.getPayerName()
                    + " via "
                    + payment.getPaymentMethod().name())
            .amount(Money.of(payment.getAmount(), payment.getCurrencyCode()))
            .debitAccount(cashAccount)
            .creditAccount(accountsReceivableAccount)
            .referenceNumber(payment.getPaymentNumber())
            .build();

    return transactionRepository.save(transaction);
  }

  private void checkOverPaymentGuard(Payment payment) {
    // Check if payment amount exceeds reasonable limits
    // This is a simplified guard - in production, would check against actual invoice balances
    if (payment.getAmount().compareTo(new java.math.BigDecimal("1000000")) > 0) {
      throw new IllegalStateException(
          "Payment amount exceeds maximum allowed limit. Please verify the payment amount.");
    }
  }

  @Transactional
  public Transaction recordPaymentRefund(Payment payment, String reason) {
    Account cashAccount = getCashAccount(payment.getPaymentMethod());
    Account accountsReceivableAccount = getAccountsReceivableAccount();

    Transaction transaction =
        Transaction.builder()
            .type(TransactionType.PAYMENT_REFUND)
            .description(
                "Payment refund: "
                    + payment.getPaymentNumber()
                    + " to "
                    + payment.getPayerName()
                    + " - "
                    + reason)
            .amount(Money.of(payment.getAmount(), payment.getCurrencyCode()))
            .debitAccount(accountsReceivableAccount)
            .creditAccount(cashAccount)
            .referenceNumber(payment.getPaymentNumber())
            .build();

    return transactionRepository.save(transaction);
  }

  private Account getCashAccount(PaymentMethod paymentMethod) {
    String accountCode = getCashAccountCode(paymentMethod);
    String accountName = getCashAccountName(paymentMethod);

    Optional<Account> existingAccount = accountRepository.findByAccountCode_Value(accountCode);

    if (existingAccount.isPresent()) {
      return existingAccount.get();
    }

    Account newAccount =
        Account.builder()
            .accountCode(AccountCode.of(accountCode))
            .accountName(accountName)
            .accountType(AccountType.ASSET)
            .description("Cash account for " + paymentMethod.name() + " payments")
            .build();

    return accountRepository.save(newAccount);
  }

  private String getCashAccountCode(PaymentMethod paymentMethod) {
    return switch (paymentMethod) {
      case PAYSTACK -> "CASH_PS";
      case BANK_TRANSFER -> "CASH_BT";
      case USSD -> "CASH_USSD";
      case CARD -> "CASH_CARD";
      case QR_CODE -> "CASH_QR";
    };
  }

  private String getCashAccountName(PaymentMethod paymentMethod) {
    return switch (paymentMethod) {
      case PAYSTACK -> "Paystack Cash Account";
      case BANK_TRANSFER -> "Bank Transfer Cash Account";
      case USSD -> "USSD Cash Account";
      case CARD -> "Card Cash Account";
      case QR_CODE -> "QR Code Cash Account";
    };
  }

  private Account getAccountsReceivableAccount() {
    Optional<Account> arAccount = accountRepository.findByAccountCode_Value("AR01");

    if (arAccount.isPresent()) {
      return arAccount.get();
    }

    Account newAccount =
        Account.builder()
            .accountCode(AccountCode.of("AR01"))
            .accountName("Accounts Receivable")
            .accountType(AccountType.ASSET)
            .description("Accounts receivable for general payments")
            .build();

    return accountRepository.save(newAccount);
  }
}
