package com.finance.smartLedger.journal.application;

import static org.junit.jupiter.api.Assertions.*;

import com.finance.smartLedger.journal.domain.DebitCredit;
import com.finance.smartLedger.journal.domain.JournalEntry;
import com.finance.smartLedger.journal.domain.JournalEntryType;
import com.finance.smartLedger.journal.infrastructure.persistence.JournalEntryRepository;
import com.finance.smartLedger.journal.infrastructure.persistence.JournalLineItemRepository;
import com.finance.smartLedger.ledger.application.AccountService;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class JournalEntryServiceIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("smartledger_test")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
  }

  @Autowired private JournalEntryService journalEntryService;

  @Autowired private JournalEntryRepository journalEntryRepository;

  @Autowired private JournalLineItemRepository journalLineItemRepository;

  @Autowired private AccountService accountService;

  @Autowired private AccountRepository accountRepository;

  private Account cashAccount;
  private Account revenueAccount;

  @BeforeEach
  void setUp() {
    journalLineItemRepository.deleteAll();
    journalEntryRepository.deleteAll();
    accountRepository.deleteAll();

    cashAccount =
        accountService.createAccount(
            "1001",
            "1001",
            "Cash",
            AccountType.ASSET,
            Money.of(BigDecimal.valueOf(1000.00), "USD"),
            "test-user");

    revenueAccount =
        accountService.createAccount(
            "4001",
            "4001",
            "Service Revenue",
            AccountType.REVENUE,
            Money.of(BigDecimal.ZERO, "USD"),
            "test-user");
  }

  @AfterEach
  void tearDown() {
    journalLineItemRepository.deleteAll();
    journalEntryRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @Test
  void createJournalEntry_Success() {
    JournalEntry journalEntry =
        journalEntryService.createJournalEntry(
            "JE-001",
            LocalDateTime.now(),
            JournalEntryType.MANUAL,
            "REF-001",
            "Test journal entry",
            "test-user");

    assertNotNull(journalEntry);
    assertNotNull(journalEntry.getId());
    assertEquals("JE-001", journalEntry.getEntryNumber());
    assertEquals(JournalEntryType.MANUAL, journalEntry.getEntryType());
    assertFalse(journalEntry.getPosted());
  }

  @Test
  void createJournalEntry_DuplicateEntryNumber_ThrowsException() {
    journalEntryService.createJournalEntry(
        "JE-001",
        LocalDateTime.now(),
        JournalEntryType.MANUAL,
        "REF-001",
        "Test journal entry",
        "test-user");

    assertThrows(
        IllegalArgumentException.class,
        () ->
            journalEntryService.createJournalEntry(
                "JE-001",
                LocalDateTime.now(),
                JournalEntryType.MANUAL,
                "REF-002",
                "Another journal entry",
                "test-user"));
  }

  @Test
  void addLineItem_Success() {
    JournalEntry journalEntry =
        journalEntryService.createJournalEntry(
            "JE-001",
            LocalDateTime.now(),
            JournalEntryType.MANUAL,
            "REF-001",
            "Test journal entry",
            "test-user");

    JournalEntry updatedEntry =
        journalEntryService.addLineItem(
            journalEntry.getId(),
            cashAccount.getId(),
            DebitCredit.DEBIT,
            Money.of(BigDecimal.valueOf(100.00), "USD"),
            "Cash debit",
            "test-user");

    assertEquals(1, updatedEntry.getLineItems().size());
    assertEquals(cashAccount.getId(), updatedEntry.getLineItems().get(0).getAccountId());
    assertEquals(DebitCredit.DEBIT, updatedEntry.getLineItems().get(0).getDebitCredit());
  }

  @Test
  void postJournalEntry_Success() {
    JournalEntry journalEntry =
        journalEntryService.createJournalEntry(
            "JE-001",
            LocalDateTime.now(),
            JournalEntryType.MANUAL,
            "REF-001",
            "Test journal entry",
            "test-user");

    journalEntryService.addLineItem(
        journalEntry.getId(),
        cashAccount.getId(),
        DebitCredit.DEBIT,
        Money.of(BigDecimal.valueOf(100.00), "USD"),
        "Cash debit",
        "test-user");

    journalEntryService.addLineItem(
        journalEntry.getId(),
        revenueAccount.getId(),
        DebitCredit.CREDIT,
        Money.of(BigDecimal.valueOf(100.00), "USD"),
        "Revenue credit",
        "test-user");

    JournalEntry postedEntry =
        journalEntryService.postJournalEntry(journalEntry.getId(), "test-user");

    assertTrue(postedEntry.getPosted());
    assertNotNull(postedEntry.getPostedDate());
    assertEquals("test-user", postedEntry.getPostedBy());
  }

  @Test
  void postJournalEntry_UnbalancedEntry_ThrowsException() {
    JournalEntry journalEntry =
        journalEntryService.createJournalEntry(
            "JE-001",
            LocalDateTime.now(),
            JournalEntryType.MANUAL,
            "REF-001",
            "Test journal entry",
            "test-user");

    journalEntryService.addLineItem(
        journalEntry.getId(),
        cashAccount.getId(),
        DebitCredit.DEBIT,
        Money.of(BigDecimal.valueOf(100.00), "USD"),
        "Cash debit",
        "test-user");

    journalEntryService.addLineItem(
        journalEntry.getId(),
        revenueAccount.getId(),
        DebitCredit.CREDIT,
        Money.of(BigDecimal.valueOf(50.00), "USD"),
        "Revenue credit",
        "test-user");

    assertThrows(
        IllegalStateException.class,
        () -> journalEntryService.postJournalEntry(journalEntry.getId(), "test-user"));
  }

  @Test
  void postJournalEntry_UpdatesAccountBalances() {
    BigDecimal initialCashBalance = cashAccount.getBalance().getCurrentBalance().getAmount();
    BigDecimal initialRevenueBalance = revenueAccount.getBalance().getCurrentBalance().getAmount();

    JournalEntry journalEntry =
        journalEntryService.createJournalEntry(
            "JE-001",
            LocalDateTime.now(),
            JournalEntryType.MANUAL,
            "REF-001",
            "Test journal entry",
            "test-user");

    journalEntryService.addLineItem(
        journalEntry.getId(),
        cashAccount.getId(),
        DebitCredit.DEBIT,
        Money.of(BigDecimal.valueOf(100.00), "USD"),
        "Cash debit",
        "test-user");

    journalEntryService.addLineItem(
        journalEntry.getId(),
        revenueAccount.getId(),
        DebitCredit.CREDIT,
        Money.of(BigDecimal.valueOf(100.00), "USD"),
        "Revenue credit",
        "test-user");

    journalEntryService.postJournalEntry(journalEntry.getId(), "test-user");

    Account updatedCash = accountService.findById(cashAccount.getId()).orElseThrow();
    Account updatedRevenue = accountService.findById(revenueAccount.getId()).orElseThrow();

    assertEquals(
        initialCashBalance.add(BigDecimal.valueOf(100.00)),
        updatedCash.getBalance().getCurrentBalance().getAmount());
    assertEquals(
        initialRevenueBalance.add(BigDecimal.valueOf(100.00)),
        updatedRevenue.getBalance().getCurrentBalance().getAmount());
  }

  @Test
  void deleteJournalEntry_Success() {
    JournalEntry journalEntry =
        journalEntryService.createJournalEntry(
            "JE-001",
            LocalDateTime.now(),
            JournalEntryType.MANUAL,
            "REF-001",
            "Test journal entry",
            "test-user");

    journalEntryService.addLineItem(
        journalEntry.getId(),
        cashAccount.getId(),
        DebitCredit.DEBIT,
        Money.of(BigDecimal.valueOf(100.00), "USD"),
        "Cash debit",
        "test-user");

    journalEntryService.deleteJournalEntry(journalEntry.getId());

    assertFalse(journalEntryRepository.findById(journalEntry.getId()).isPresent());
  }

  @Test
  void deleteJournalEntry_PostedEntry_ThrowsException() {
    JournalEntry journalEntry =
        journalEntryService.createJournalEntry(
            "JE-001",
            LocalDateTime.now(),
            JournalEntryType.MANUAL,
            "REF-001",
            "Test journal entry",
            "test-user");

    journalEntryService.addLineItem(
        journalEntry.getId(),
        cashAccount.getId(),
        DebitCredit.DEBIT,
        Money.of(BigDecimal.valueOf(100.00), "USD"),
        "Cash debit",
        "test-user");

    journalEntryService.addLineItem(
        journalEntry.getId(),
        revenueAccount.getId(),
        DebitCredit.CREDIT,
        Money.of(BigDecimal.valueOf(100.00), "USD"),
        "Revenue credit",
        "test-user");

    journalEntryService.postJournalEntry(journalEntry.getId(), "test-user");

    assertThrows(
        IllegalStateException.class,
        () -> journalEntryService.deleteJournalEntry(journalEntry.getId()));
  }
}
