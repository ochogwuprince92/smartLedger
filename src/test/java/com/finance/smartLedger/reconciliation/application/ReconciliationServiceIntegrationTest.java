package com.finance.smartLedger.reconciliation.application;

import static org.junit.jupiter.api.Assertions.*;

import com.finance.smartLedger.ledger.application.AccountService;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.reconciliation.domain.MatchStatus;
import com.finance.smartLedger.reconciliation.domain.Reconciliation;
import com.finance.smartLedger.reconciliation.domain.ReconciliationItem;
import com.finance.smartLedger.reconciliation.domain.ReconciliationStatus;
import com.finance.smartLedger.reconciliation.infrastructure.persistence.ReconciliationItemRepository;
import com.finance.smartLedger.reconciliation.infrastructure.persistence.ReconciliationRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.finance.smartLedger.test.configuration.TestDatabaseConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.Disabled;

@SpringBootTest
@TestPropertySource(properties = {
    "app.scheduled.enabled=false",
    "app.data-loader.enabled=false"
})
@Disabled("Docker not available on this system")
@Testcontainers
class ReconciliationServiceIntegrationTest {

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    TestDatabaseConfiguration.configureDatabase(registry);
  }

  @Autowired private ReconciliationService reconciliationService;

  @Autowired private ReconciliationRepository reconciliationRepository;

  @Autowired private ReconciliationItemRepository reconciliationItemRepository;

  @Autowired private AccountService accountService;

  @Autowired private AccountRepository accountRepository;

  private Account suspenseAccount;

  @BeforeEach
  void setUp() {
    reconciliationItemRepository.deleteAll();
    reconciliationRepository.deleteAll();
    accountRepository.deleteAll();

    suspenseAccount =
        accountService.createAccount(
            "9999",
            "9999",
            "Suspense Account",
            AccountType.ASSET,
            Money.of(BigDecimal.ZERO, "USD"),
            "test-user");
  }

  @AfterEach
  void tearDown() {
    reconciliationItemRepository.deleteAll();
    reconciliationRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @Test
  void createReconciliation_Success() {
    Reconciliation reconciliation =
        reconciliationService.createReconciliation(
            "REC-001",
            LocalDateTime.now(),
            "BANK",
            "BANK-001",
            BigDecimal.valueOf(1000.00),
            "Monthly bank reconciliation",
            "test-user");

    assertNotNull(reconciliation);
    assertNotNull(reconciliation.getId());
    assertEquals("REC-001", reconciliation.getReconciliationNumber());
    assertEquals(ReconciliationStatus.PENDING, reconciliation.getStatus());
    assertEquals(BigDecimal.valueOf(1000.00), reconciliation.getTotalExpectedAmount());
  }

  @Test
  void createReconciliation_DuplicateNumber_ThrowsException() {
    reconciliationService.createReconciliation(
        "REC-001",
        LocalDateTime.now(),
        "BANK",
        "BANK-001",
        BigDecimal.valueOf(1000.00),
        "Monthly bank reconciliation",
        "test-user");

    assertThrows(
        IllegalArgumentException.class,
        () ->
            reconciliationService.createReconciliation(
                "REC-001",
                LocalDateTime.now(),
                "BANK",
                "BANK-002",
                BigDecimal.valueOf(2000.00),
                "Another reconciliation",
                "test-user"));
  }

  @Test
  void addItem_Success() {
    Reconciliation reconciliation =
        reconciliationService.createReconciliation(
            "REC-001",
            LocalDateTime.now(),
            "BANK",
            "BANK-001",
            BigDecimal.valueOf(1000.00),
            "Monthly bank reconciliation",
            "test-user");

    ReconciliationItem item =
        reconciliationService.addItem(
            reconciliation.getId(),
            "TXN-001",
            "PAYMENT",
            BigDecimal.valueOf(100.00),
            "Payment from customer",
            "test-user");

    assertNotNull(item);
    assertEquals("TXN-001", item.getItemReference());
    assertEquals(BigDecimal.valueOf(100.00), item.getExpectedAmount());
    assertEquals(MatchStatus.UNMATCHED, item.getMatchStatus());
  }

  @Test
  void startReconciliation_Success() {
    Reconciliation reconciliation =
        reconciliationService.createReconciliation(
            "REC-001",
            LocalDateTime.now(),
            "BANK",
            "BANK-001",
            BigDecimal.valueOf(1000.00),
            "Monthly bank reconciliation",
            "test-user");

    Reconciliation started =
        reconciliationService.startReconciliation(reconciliation.getId(), "test-user");

    assertEquals(ReconciliationStatus.IN_PROGRESS, started.getStatus());
  }

  @Test
  void matchItem_Success() {
    Reconciliation reconciliation =
        reconciliationService.createReconciliation(
            "REC-001",
            LocalDateTime.now(),
            "BANK",
            "BANK-001",
            BigDecimal.valueOf(1000.00),
            "Monthly bank reconciliation",
            "test-user");

    ReconciliationItem item =
        reconciliationService.addItem(
            reconciliation.getId(),
            "TXN-001",
            "PAYMENT",
            BigDecimal.valueOf(100.00),
            "Payment from customer",
            "test-user");

    ReconciliationItem matched =
        reconciliationService.matchItem(
            item.getId(), UUID.randomUUID(), BigDecimal.valueOf(100.00), "test-user");

    assertEquals(MatchStatus.MATCHED, matched.getMatchStatus());
    assertEquals(BigDecimal.valueOf(100.00), matched.getActualAmount());
    assertNotNull(matched.getMatchedAt());
  }

  @Test
  void moveToSuspense_Success() {
    Reconciliation reconciliation =
        reconciliationService.createReconciliation(
            "REC-001",
            LocalDateTime.now(),
            "BANK",
            "BANK-001",
            BigDecimal.valueOf(1000.00),
            "Monthly bank reconciliation",
            "test-user");

    ReconciliationItem item =
        reconciliationService.addItem(
            reconciliation.getId(),
            "TXN-001",
            "PAYMENT",
            BigDecimal.valueOf(100.00),
            "Payment from customer",
            "test-user");

    ReconciliationItem suspense =
        reconciliationService.moveToSuspense(item.getId(), suspenseAccount.getId(), "test-user");

    assertEquals(MatchStatus.SUSPENSE, suspense.getMatchStatus());

    Reconciliation updated = reconciliationService.findById(reconciliation.getId()).orElseThrow();
    assertEquals(ReconciliationStatus.PARTIALLY_MATCHED, updated.getStatus());
    assertEquals(suspenseAccount.getId(), updated.getSuspenseAccountId());
  }

  @Test
  void completeReconciliation_Balanced_Success() {
    Reconciliation reconciliation =
        reconciliationService.createReconciliation(
            "REC-001",
            LocalDateTime.now(),
            "BANK",
            "BANK-001",
            BigDecimal.valueOf(1000.00),
            "Monthly bank reconciliation",
            "test-user");

    reconciliationService.addItem(
        reconciliation.getId(),
        "TXN-001",
        "PAYMENT",
        BigDecimal.valueOf(500.00),
        "Payment 1",
        "test-user");

    reconciliationService.addItem(
        reconciliation.getId(),
        "TXN-002",
        "PAYMENT",
        BigDecimal.valueOf(500.00),
        "Payment 2",
        "test-user");

    List<ReconciliationItem> items =
        reconciliationService.findItemsByReconciliationId(reconciliation.getId());

    reconciliationService.matchItem(
        items.get(0).getId(), UUID.randomUUID(), BigDecimal.valueOf(500.00), "test-user");
    reconciliationService.matchItem(
        items.get(1).getId(), UUID.randomUUID(), BigDecimal.valueOf(500.00), "test-user");

    Reconciliation completed =
        reconciliationService.completeReconciliation(reconciliation.getId(), "test-user");

    assertEquals(ReconciliationStatus.COMPLETED, completed.getStatus());
    assertNotNull(completed.getCompletedAt());
    assertTrue(completed.isBalanced());
  }

  @Test
  void completeReconciliation_Unbalanced_MarksPartiallyMatched() {
    Reconciliation reconciliation =
        reconciliationService.createReconciliation(
            "REC-001",
            LocalDateTime.now(),
            "BANK",
            "BANK-001",
            BigDecimal.valueOf(1000.00),
            "Monthly bank reconciliation",
            "test-user");

    reconciliationService.addItem(
        reconciliation.getId(),
        "TXN-001",
        "PAYMENT",
        BigDecimal.valueOf(500.00),
        "Payment 1",
        "test-user");

    List<ReconciliationItem> items =
        reconciliationService.findItemsByReconciliationId(reconciliation.getId());

    reconciliationService.matchItem(
        items.get(0).getId(), UUID.randomUUID(), BigDecimal.valueOf(500.00), "test-user");

    Reconciliation completed =
        reconciliationService.completeReconciliation(reconciliation.getId(), "test-user");

    assertEquals(ReconciliationStatus.PARTIALLY_MATCHED, completed.getStatus());
    assertFalse(completed.isBalanced());
  }

  @Test
  void failReconciliation_Success() {
    Reconciliation reconciliation =
        reconciliationService.createReconciliation(
            "REC-001",
            LocalDateTime.now(),
            "BANK",
            "BANK-001",
            BigDecimal.valueOf(1000.00),
            "Monthly bank reconciliation",
            "test-user");

    Reconciliation failed =
        reconciliationService.failReconciliation(reconciliation.getId(), "test-user");

    assertEquals(ReconciliationStatus.FAILED, failed.getStatus());
  }

  @Test
  void deleteReconciliation_Success() {
    Reconciliation reconciliation =
        reconciliationService.createReconciliation(
            "REC-001",
            LocalDateTime.now(),
            "BANK",
            "BANK-001",
            BigDecimal.valueOf(1000.00),
            "Monthly bank reconciliation",
            "test-user");

    reconciliationService.deleteReconciliation(reconciliation.getId());

    assertFalse(reconciliationService.findById(reconciliation.getId()).isPresent());
  }

  @Test
  void deleteReconciliation_Completed_ThrowsException() {
    Reconciliation reconciliation =
        reconciliationService.createReconciliation(
            "REC-001",
            LocalDateTime.now(),
            "BANK",
            "BANK-001",
            BigDecimal.valueOf(1000.00),
            "Monthly bank reconciliation",
            "test-user");

    reconciliationService.addItem(
        reconciliation.getId(),
        "TXN-001",
        "PAYMENT",
        BigDecimal.valueOf(1000.00),
        "Payment",
        "test-user");

    List<ReconciliationItem> items =
        reconciliationService.findItemsByReconciliationId(reconciliation.getId());

    reconciliationService.matchItem(
        items.get(0).getId(), UUID.randomUUID(), BigDecimal.valueOf(1000.00), "test-user");

    reconciliationService.completeReconciliation(reconciliation.getId(), "test-user");

    assertThrows(
        IllegalStateException.class,
        () -> reconciliationService.deleteReconciliation(reconciliation.getId()));
  }

  @Test
  void findByStatus_Success() {
    reconciliationService.createReconciliation(
        "REC-001",
        LocalDateTime.now(),
        "BANK",
        "BANK-001",
        BigDecimal.valueOf(1000.00),
        "Reconciliation 1",
        "test-user");

    reconciliationService.createReconciliation(
        "REC-002",
        LocalDateTime.now(),
        "BANK",
        "BANK-002",
        BigDecimal.valueOf(2000.00),
        "Reconciliation 2",
        "test-user");

    List<Reconciliation> pending = reconciliationService.findByStatus(ReconciliationStatus.PENDING);

    assertEquals(2, pending.size());
  }

  @Test
  void findBySourceSystem_Success() {
    reconciliationService.createReconciliation(
        "REC-001",
        LocalDateTime.now(),
        "BANK",
        "BANK-001",
        BigDecimal.valueOf(1000.00),
        "Bank reconciliation",
        "test-user");

    reconciliationService.createReconciliation(
        "REC-002",
        LocalDateTime.now(),
        "PAYMENT_GATEWAY",
        "PG-001",
        BigDecimal.valueOf(2000.00),
        "Payment gateway reconciliation",
        "test-user");

    List<Reconciliation> bankReconciliations = reconciliationService.findBySourceSystem("BANK");

    assertEquals(1, bankReconciliations.size());
    assertEquals("BANK", bankReconciliations.get(0).getSourceSystem());
  }

  @Test
  void findUnmatchedItems_Success() {
    Reconciliation reconciliation =
        reconciliationService.createReconciliation(
            "REC-001",
            LocalDateTime.now(),
            "BANK",
            "BANK-001",
            BigDecimal.valueOf(1000.00),
            "Monthly bank reconciliation",
            "test-user");

    reconciliationService.addItem(
        reconciliation.getId(),
        "TXN-001",
        "PAYMENT",
        BigDecimal.valueOf(500.00),
        "Payment 1",
        "test-user");

    reconciliationService.addItem(
        reconciliation.getId(),
        "TXN-002",
        "PAYMENT",
        BigDecimal.valueOf(500.00),
        "Payment 2",
        "test-user");

    List<ReconciliationItem> items =
        reconciliationService.findItemsByReconciliationId(reconciliation.getId());

    reconciliationService.matchItem(
        items.get(0).getId(), UUID.randomUUID(), BigDecimal.valueOf(500.00), "test-user");

    List<ReconciliationItem> unmatched =
        reconciliationService.findUnmatchedItems(reconciliation.getId());

    assertEquals(1, unmatched.size());
    assertEquals("TXN-002", unmatched.get(0).getItemReference());
  }
}
