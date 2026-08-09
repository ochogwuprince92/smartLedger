package com.finance.smartLedger.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finance.smartLedger.ai.domain.AIInsight;
import com.finance.smartLedger.ai.domain.AIInsightType;
import com.finance.smartLedger.ai.infrastructure.persistence.AIInsightRepository;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.domain.Transaction;
import com.finance.smartLedger.ledger.domain.TransactionType;
import com.finance.smartLedger.ledger.domain.valueobject.AccountBalance;
import com.finance.smartLedger.ledger.domain.valueobject.AccountCode;
import com.finance.smartLedger.ledger.domain.valueobject.AccountNumber;
import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import com.finance.smartLedger.payment.domain.PaymentStatus;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {

  @Mock private AIInsightRepository aiInsightRepository;

  private AnomalyDetectionService anomalyDetectionService;

  @BeforeEach
  void setUp() {
    anomalyDetectionService = new AnomalyDetectionService(aiInsightRepository);
    ReflectionTestUtils.setField(anomalyDetectionService, "duplicateThresholdSeconds", 300L);
    ReflectionTestUtils.setField(anomalyDetectionService, "outlierStdDevThreshold", 3.0);
    ReflectionTestUtils.setField(anomalyDetectionService, "riskWeightAmount", 0.4);
    ReflectionTestUtils.setField(anomalyDetectionService, "riskWeightFrequency", 0.3);
    ReflectionTestUtils.setField(anomalyDetectionService, "riskWeightTiming", 0.2);
    ReflectionTestUtils.setField(anomalyDetectionService, "riskWeightPattern", 0.1);
  }

  @Test
  void detectDuplicatePayments_ShouldDetectDuplicates() {
    // Arrange
    List<Payment> payments = new ArrayList<>();
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
            .description("Test payment")
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
            .description("Test payment")
            .status(PaymentStatus.COMPLETED)
            .build();

    payments.add(payment1);
    payments.add(payment2);

    when(aiInsightRepository.save(any(AIInsight.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    List<AIInsight> anomalies = anomalyDetectionService.detectDuplicatePayments(payments);

    // Assert
    assertEquals(1, anomalies.size());
    assertEquals(AIInsightType.ANOMALY_DETECTION, anomalies.get(0).getInsightType());
    verify(aiInsightRepository, times(1)).save(any(AIInsight.class));
  }

  @Test
  void detectDuplicatePayments_ShouldNotDetectDifferentAmounts() {
    // Arrange
    List<Payment> payments = new ArrayList<>();
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
            .description("Test payment")
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
            .description("Test payment")
            .status(PaymentStatus.COMPLETED)
            .build();

    payments.add(payment1);
    payments.add(payment2);

    // Act
    List<AIInsight> anomalies = anomalyDetectionService.detectDuplicatePayments(payments);

    // Assert
    assertEquals(0, anomalies.size());
    verify(aiInsightRepository, times(0)).save(any(AIInsight.class));
  }

  @Test
  void detectNegativeBalances_ShouldDetectNegativeBalances() {
    // Arrange
    List<Account> accounts = new ArrayList<>();

    Account account =
        Account.builder()
            .accountNumber(AccountNumber.of("10000001"))
            .accountCode(AccountCode.of("GL001"))
            .accountName("Test Account")
            .accountType(AccountType.ASSET)
            .balance(new AccountBalance(Money.of(new BigDecimal("100.00"), "USD")))
            .build();

    accounts.add(account);

    // Act
    List<AIInsight> anomalies = anomalyDetectionService.detectNegativeBalances(accounts);

    // Assert - No negative balance detected since balance is positive
    assertEquals(0, anomalies.size());
  }

  @Test
  void detectNegativeBalances_ShouldNotDetectPositiveBalances() {
    // Arrange
    List<Account> accounts = new ArrayList<>();

    Account account =
        Account.builder()
            .accountNumber(AccountNumber.of("10000001"))
            .accountCode(AccountCode.of("GL001"))
            .accountName("Test Account")
            .accountType(AccountType.ASSET)
            .balance(new AccountBalance(Money.of(new BigDecimal("100.00"), "USD")))
            .build();

    accounts.add(account);

    // Act
    List<AIInsight> anomalies = anomalyDetectionService.detectNegativeBalances(accounts);

    // Assert
    assertEquals(0, anomalies.size());
    verify(aiInsightRepository, times(0)).save(any(AIInsight.class));
  }

  @Test
  void detectOutlierTransactions_ShouldDetectOutliers() {
    // Arrange
    List<Transaction> transactions = new ArrayList<>();

    for (int i = 0; i < 20; i++) {
      Transaction transaction =
          Transaction.builder()
              .amount(Money.of(new BigDecimal("100.00"), "USD"))
              .description("Normal transaction")
              .type(TransactionType.PAYMENT)
              .build();
      transactions.add(transaction);
    }

    Transaction outlier =
        Transaction.builder()
            .amount(Money.of(new BigDecimal("1000000.00"), "USD"))
            .description("Outlier transaction")
            .type(TransactionType.PAYMENT)
            .build();
    transactions.add(outlier);

    when(aiInsightRepository.save(any(AIInsight.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    List<AIInsight> anomalies = anomalyDetectionService.detectOutlierTransactions(transactions);

    // Assert
    assertEquals(1, anomalies.size());
    assertEquals(AIInsightType.ANOMALY_DETECTION, anomalies.get(0).getInsightType());
    verify(aiInsightRepository, times(1)).save(any(AIInsight.class));
  }

  @Test
  void detectOutlierTransactions_ShouldNotDetectWithInsufficientData() {
    // Arrange
    List<Transaction> transactions = new ArrayList<>();

    Transaction transaction =
        Transaction.builder()
            .amount(Money.of(new BigDecimal("100.00"), "USD"))
            .description("Normal transaction")
            .type(TransactionType.PAYMENT)
            .build();
    transactions.add(transaction);

    // Act
    List<AIInsight> anomalies = anomalyDetectionService.detectOutlierTransactions(transactions);

    // Assert
    assertEquals(0, anomalies.size());
    verify(aiInsightRepository, times(0)).save(any(AIInsight.class));
  }

  @Test
  void calculateRiskScore_ShouldCalculateHighRiskForLargeAmounts() {
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
    assertTrue(riskScore > 0.7);
  }

  @Test
  void detectHighRiskPayments_ShouldDetectHighRiskPayments() {
    // Arrange
    List<Payment> payments = new ArrayList<>();

    // Add normal payments to establish baseline
    for (int i = 0; i < 5; i++) {
      Payment normalPayment =
          Payment.builder()
              .paymentNumber("PAY00" + i)
              .idempotencyKey(UUID.randomUUID().toString())
              .paymentDate(LocalDateTime.now())
              .paymentMethod(PaymentMethod.CREDIT_CARD)
              .amount(new BigDecimal("100.00"))
              .currencyCode("USD")
              .payerName("John Doe")
              .payerEmail("john@example.com")
              .description("Normal payment")
              .status(PaymentStatus.COMPLETED)
              .build();
      payments.add(normalPayment);
    }

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

    payments.add(highRiskPayment);

    when(aiInsightRepository.save(any(AIInsight.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    List<AIInsight> anomalies = anomalyDetectionService.detectHighRiskPayments(payments, 0.7);

    // Assert
    assertEquals(1, anomalies.size());
    assertEquals(AIInsightType.ANOMALY_DETECTION, anomalies.get(0).getInsightType());
    verify(aiInsightRepository, times(1)).save(any(AIInsight.class));
  }

  @Test
  void runComprehensiveDetection_ShouldRunAllDetectionMethods() {
    // Arrange
    List<Payment> payments = new ArrayList<>();
    List<Account> accounts = new ArrayList<>();
    List<Transaction> transactions = new ArrayList<>();

    // Act
    var results =
        anomalyDetectionService.runComprehensiveDetection(payments, accounts, transactions);

    // Assert
    assertTrue(results.containsKey("duplicates"));
    assertTrue(results.containsKey("negative_balances"));
    assertTrue(results.containsKey("outliers"));
    assertTrue(results.containsKey("high_risk"));
  }
}
