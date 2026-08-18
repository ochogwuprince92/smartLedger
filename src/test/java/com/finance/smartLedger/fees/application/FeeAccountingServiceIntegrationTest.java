package com.finance.smartLedger.fees.application;

import static org.junit.jupiter.api.Assertions.*;

import com.finance.smartLedger.fees.domain.FeePayment;
import com.finance.smartLedger.fees.domain.FeeType;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.domain.Transaction;
import com.finance.smartLedger.ledger.domain.TransactionType;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.ledger.infrastructure.persistence.TransactionRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.finance.smartLedger.test.configuration.TestDatabaseConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Disabled("Docker not available on this system")
class FeeAccountingServiceIntegrationTest {

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    TestDatabaseConfiguration.configureDatabase(registry);
  }

  @Autowired private FeeAccountingService feeAccountingService;

  @Autowired private AccountRepository accountRepository;

  @Autowired private TransactionRepository transactionRepository;

  @BeforeEach
  void setUp() {
    accountRepository.deleteAll();
    transactionRepository.deleteAll();
  }

  @AfterEach
  void tearDown() {
    transactionRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @Test
  void recordFeePayment_Success() {
    FeePayment feePayment =
        new FeePayment(
            UUID.randomUUID(),
            null,
            FeeType.TUITION_FEE,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "CASH",
            "REF001");

    Transaction transaction = feeAccountingService.recordFeePayment(feePayment);

    assertNotNull(transaction);
    assertNotNull(transaction.getId());
    assertEquals(TransactionType.FEE_PAYMENT, transaction.getType());
    assertEquals(BigDecimal.valueOf(1000.00), transaction.getAmount().getAmount());
    assertEquals("REF001", transaction.getReferenceNumber());
    assertTrue(transaction.getDescription().contains("Fee payment"));
  }

  @Test
  void recordFeeRefund_Success() {
    FeePayment feePayment =
        new FeePayment(
            UUID.randomUUID(),
            null,
            FeeType.TUITION_FEE,
            Money.of(BigDecimal.valueOf(500.00), "USD"),
            "CASH",
            "REF002");

    Transaction transaction =
        feeAccountingService.recordFeeRefund(feePayment, "Student withdrawal");

    assertNotNull(transaction);
    assertEquals(TransactionType.FEE_REFUND, transaction.getType());
    assertEquals(BigDecimal.valueOf(500.00), transaction.getAmount().getAmount());
    assertTrue(transaction.getDescription().contains("Fee refund"));
  }

  @Test
  void recordScholarship_Success() {
    Transaction transaction =
        feeAccountingService.recordScholarship(
            FeeType.TUITION_FEE, Money.of(BigDecimal.valueOf(2000.00), "USD"), UUID.randomUUID());

    assertNotNull(transaction);
    assertEquals(TransactionType.SCHOLARSHIP, transaction.getType());
    assertEquals(BigDecimal.valueOf(2000.00), transaction.getAmount().getAmount());
    assertTrue(transaction.getDescription().contains("Scholarship"));
  }

  @Test
  void recordDiscount_Success() {
    Transaction transaction =
        feeAccountingService.recordDiscount(
            FeeType.TUITION_FEE,
            Money.of(BigDecimal.valueOf(500.00), "USD"),
            UUID.randomUUID(),
            "Early bird discount");

    assertNotNull(transaction);
    assertEquals(TransactionType.DISCOUNT, transaction.getType());
    assertEquals(BigDecimal.valueOf(500.00), transaction.getAmount().getAmount());
    assertTrue(transaction.getDescription().contains("Discount"));
  }

  @Test
  void getAccountTypeForFee_Success() {
    assertEquals(
        AccountType.REVENUE, feeAccountingService.getAccountTypeForFee(FeeType.TUITION_FEE));
    assertEquals(
        AccountType.EXPENSE, feeAccountingService.getAccountTypeForFee(FeeType.SCHOLARSHIP));
  }

  @Test
  void isRevenueFee_Success() {
    assertTrue(feeAccountingService.isRevenueFee(FeeType.TUITION_FEE));
    assertFalse(feeAccountingService.isRevenueFee(FeeType.SCHOLARSHIP));
  }

  @Test
  void isExpenseFee_Success() {
    assertFalse(feeAccountingService.isExpenseFee(FeeType.TUITION_FEE));
    assertTrue(feeAccountingService.isExpenseFee(FeeType.SCHOLARSHIP));
  }
}
