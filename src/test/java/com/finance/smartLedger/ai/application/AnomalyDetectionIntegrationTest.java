package com.finance.smartLedger.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.finance.smartLedger.ai.domain.AIInsight;
import com.finance.smartLedger.ai.infrastructure.persistence.AIInsightRepository;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.domain.Transaction;
import com.finance.smartLedger.ledger.domain.TransactionType;
import com.finance.smartLedger.ledger.domain.valueobject.AccountBalance;
import com.finance.smartLedger.ledger.domain.valueobject.AccountCode;
import com.finance.smartLedger.ledger.domain.valueobject.AccountNumber;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.ledger.infrastructure.persistence.TransactionRepository;
import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import com.finance.smartLedger.payment.domain.PaymentStatus;
import com.finance.smartLedger.payment.infrastructure.persistence.PaymentRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AnomalyDetectionIntegrationTest {

  @Autowired private AnomalyDetectionService anomalyDetectionService;

  @Autowired private AIInsightRepository aiInsightRepository;

  @Autowired private PaymentRepository paymentRepository;

  @Autowired private AccountRepository accountRepository;

  @Autowired private TransactionRepository transactionRepository;

  @BeforeEach
  void setUp() {
    // Clean up existing data
    aiInsightRepository.deleteAll();
    paymentRepository.deleteAll();
    accountRepository.deleteAll();
    transactionRepository.deleteAll();
  }

  @Test
  void detectDuplicatePayments_ShouldDetectAndSaveAnomalies() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();

    Payment payment1 =
        Payment.builder()
            .paymentNumber("PAY001")
            .idempotencyKey(UUID.randomUUID().toString())
            .paymentDate(now)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .amount(new BigDecimal("100.00"))
            .currencyCode("USD")
            .payerName("John Doe")
            .payerEmail("john@example.com")
            .description("Test payment 1")
            .status(PaymentStatus.COMPLETED)
            .build();

    Payment payment2 =
        Payment.builder()
            .paymentNumber("PAY002")
            .idempotencyKey(UUID.randomUUID().toString())
            .paymentDate(now.plusSeconds(60))
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .amount(new BigDecimal("100.00"))
            .currencyCode("USD")
            .payerName("John Doe")
            .payerEmail("john@example.com")
            .description("Test payment 2")
            .status(PaymentStatus.COMPLETED)
            .build();

    paymentRepository.save(payment1);
    paymentRepository.save(payment2);

    List<Payment> payments = List.of(payment1, payment2);

    // Act
    List<AIInsight> anomalies = anomalyDetectionService.detectDuplicatePayments(payments);

    // Assert
    assertEquals(1, anomalies.size());
    assertEquals("DUPLICATE_PAYMENT", anomalies.get(0).getInsightType());
    assertEquals("HIGH", anomalies.get(0).getSeverity());
    assertNotNull(anomalies.get(0).getId());

    // Verify it was saved to repository
    List<AIInsight> savedInsights = aiInsightRepository.findAll();
    assertEquals(1, savedInsights.size());
  }

  @Test
  void detectNegativeBalances_ShouldDetectAndSaveAnomalies() {
    // Arrange
    Account account =
        Account.builder()
            .accountNumber(new AccountNumber("ACC001"))
            .accountCode(new AccountCode("CODE001"))
            .accountName("Test Account")
            .accountType(AccountType.ASSET)
            .balance(new AccountBalance(Money.of(new BigDecimal("-100.00"), "USD")))
            .build();

    accountRepository.save(account);

    List<Account> accounts = List.of(account);

    // Act
    List<AIInsight> anomalies = anomalyDetectionService.detectNegativeBalances(accounts);

    // Assert
    assertEquals(1, anomalies.size());
    assertEquals("NEGATIVE_BALANCE", anomalies.get(0).getInsightType());
    assertEquals("HIGH", anomalies.get(0).getSeverity());
    assertNotNull(anomalies.get(0).getId());

    // Verify it was saved to repository
    List<AIInsight> savedInsights = aiInsightRepository.findAll();
    assertEquals(1, savedInsights.size());
  }

  @Test
  void detectOutlierTransactions_ShouldDetectAndSaveAnomalies() {
    // Arrange
    List<Transaction> transactions = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      Transaction transaction =
          Transaction.builder()
              .amount(Money.of(new BigDecimal("100.00"), "USD"))
              .description("Normal transaction")
              .type(TransactionType.DEBIT)
              .transactionDate(LocalDateTime.now())
              .build();
      transactions.add(transaction);
    }

    Transaction outlier =
        Transaction.builder()
            .amount(Money.of(new BigDecimal("1000.00"), "USD"))
            .description("Outlier transaction")
            .type(TransactionType.DEBIT)
            .transactionDate(LocalDateTime.now())
            .build();
    transactions.add(outlier);

    transactionRepository.saveAll(transactions);

    // Act
    List<AIInsight> anomalies = anomalyDetectionService.detectOutlierTransactions(transactions);

    // Assert
    assertEquals(1, anomalies.size());
    assertEquals("OUTLIER_TRANSACTION", anomalies.get(0).getInsightType());
    assertNotNull(anomalies.get(0).getId());

    // Verify it was saved to repository
    List<AIInsight> savedInsights = aiInsightRepository.findAll();
    assertEquals(1, savedInsights.size());
  }

  @Test
  void detectHighRiskPayments_ShouldDetectAndSaveAnomalies() {
    // Arrange
    Payment highRiskPayment =
        Payment.builder()
            .paymentNumber("PAY999")
            .idempotencyKey(UUID.randomUUID().toString())
            .paymentDate(LocalDateTime.now())
            .paymentMethod(PaymentMethod.BANK_TRANSFER)
            .amount(new BigDecimal("10000.00"))
            .currencyCode("USD")
            .payerName("John Doe")
            .payerEmail("john@example.com")
            .description("High risk payment")
            .status(PaymentStatus.COMPLETED)
            .build();

    paymentRepository.save(highRiskPayment);

    List<Payment> payments = List.of(highRiskPayment);

    // Act
    List<AIInsight> anomalies = anomalyDetectionService.detectHighRiskPayments(payments, 0.7);

    // Assert
    assertEquals(1, anomalies.size());
    assertEquals("HIGH_RISK_PAYMENT", anomalies.get(0).getInsightType());
    assertNotNull(anomalies.get(0).getId());

    // Verify it was saved to repository
    List<AIInsight> savedInsights = aiInsightRepository.findAll();
    assertEquals(1, savedInsights.size());
  }

  @Test
  void runComprehensiveDetection_ShouldRunAllDetectionMethodsAndSaveAnomalies() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();

    // Create duplicate payments
    Payment payment1 =
        Payment.builder()
            .paymentNumber("PAY001")
            .idempotencyKey(UUID.randomUUID().toString())
            .paymentDate(now)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .amount(new BigDecimal("100.00"))
            .currencyCode("USD")
            .payerName("John Doe")
            .payerEmail("john@example.com")
            .description("Test payment 1")
            .status(PaymentStatus.COMPLETED)
            .build();

    Payment payment2 =
        Payment.builder()
            .paymentNumber("PAY002")
            .idempotencyKey(UUID.randomUUID().toString())
            .paymentDate(now.plusSeconds(60))
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .amount(new BigDecimal("100.00"))
            .currencyCode("USD")
            .payerName("John Doe")
            .payerEmail("john@example.com")
            .description("Test payment 2")
            .status(PaymentStatus.COMPLETED)
            .build();

    paymentRepository.save(payment1);
    paymentRepository.save(payment2);

    // Create account with negative balance
    Account account =
        Account.builder()
            .accountNumber(new AccountNumber("ACC001"))
            .accountCode(new AccountCode("CODE001"))
            .accountName("Test Account")
            .accountType(AccountType.ASSET)
            .balance(new AccountBalance(Money.of(new BigDecimal("-100.00"), "USD")))
            .build();

    accountRepository.save(account);

    // Create transactions
    List<Transaction> transactions = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      Transaction transaction =
          Transaction.builder()
              .amount(Money.of(new BigDecimal("100.00"), "USD"))
              .description("Normal transaction")
              .type(TransactionType.DEBIT)
              .transactionDate(LocalDateTime.now())
              .build();
      transactions.add(transaction);
    }

    Transaction outlier =
        Transaction.builder()
            .amount(Money.of(new BigDecimal("1000.00"), "USD"))
            .description("Outlier transaction")
            .type(TransactionType.DEBIT)
            .transactionDate(LocalDateTime.now())
            .build();
    transactions.add(outlier);

    transactionRepository.saveAll(transactions);

    List<Payment> payments = List.of(payment1, payment2);
    List<Account> accounts = List.of(account);

    // Act
    var results =
        anomalyDetectionService.runComprehensiveDetection(payments, accounts, transactions);

    // Assert
    assertTrue(results.containsKey("duplicates"));
    assertTrue(results.containsKey("negative_balances"));
    assertTrue(results.containsKey("outliers"));
    assertTrue(results.containsKey("high_risk"));

    // Verify all anomalies were saved
    List<AIInsight> savedInsights = aiInsightRepository.findAll();
    assertTrue(savedInsights.size() >= 3); // At least duplicate, negative balance, and outlier
  }

  @Test
  void calculateRiskScore_ShouldReturnValidScore() {
    // Arrange
    List<Payment> recentPayments = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      Payment payment =
          Payment.builder()
              .paymentNumber("PAY00" + i)
              .idempotencyKey(UUID.randomUUID().toString())
              .paymentDate(LocalDateTime.now())
              .paymentMethod(PaymentMethod.CREDIT_CARD)
              .amount(new BigDecimal("100.00"))
              .currencyCode("USD")
              .payerName("John Doe")
              .payerEmail("john@example.com")
              .description("Test payment")
              .status(PaymentStatus.COMPLETED)
              .build();
      recentPayments.add(payment);
    }

    Payment highRiskPayment =
        Payment.builder()
            .paymentNumber("PAY999")
            .idempotencyKey(UUID.randomUUID().toString())
            .paymentDate(LocalDateTime.now())
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .amount(new BigDecimal("500.00"))
            .currencyCode("USD")
            .payerName("John Doe")
            .payerEmail("john@example.com")
            .description("High risk payment")
            .status(PaymentStatus.COMPLETED)
            .build();

    // Act
    double riskScore = anomalyDetectionService.calculateRiskScore(highRiskPayment, recentPayments);

    // Assert
    assertTrue(riskScore >= 0.0);
    assertTrue(riskScore <= 1.0);
  }

  @Test
  void detectDuplicatePayments_ShouldNotDetectNonDuplicates() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();

    Payment payment1 =
        Payment.builder()
            .paymentNumber("PAY001")
            .idempotencyKey(UUID.randomUUID().toString())
            .paymentDate(now)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .amount(new BigDecimal("100.00"))
            .currencyCode("USD")
            .payerName("John Doe")
            .payerEmail("john@example.com")
            .description("Test payment 1")
            .status(PaymentStatus.COMPLETED)
            .build();

    Payment payment2 =
        Payment.builder()
            .paymentNumber("PAY002")
            .idempotencyKey(UUID.randomUUID().toString())
            .paymentDate(now.plusSeconds(60))
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .amount(new BigDecimal("200.00"))
            .currencyCode("USD")
            .payerName("John Doe")
            .payerEmail("john@example.com")
            .description("Test payment 2")
            .status(PaymentStatus.COMPLETED)
            .build();

    paymentRepository.save(payment1);
    paymentRepository.save(payment2);

    List<Payment> payments = List.of(payment1, payment2);

    // Act
    List<AIInsight> anomalies = anomalyDetectionService.detectDuplicatePayments(payments);

    // Assert
    assertEquals(0, anomalies.size());

    // Verify no insights were saved
    List<AIInsight> savedInsights = aiInsightRepository.findAll();
    assertEquals(0, savedInsights.size());
  }
}
