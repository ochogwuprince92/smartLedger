package com.finance.smartLedger.reporting.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.smartLedger.journal.application.JournalEntryService;
import com.finance.smartLedger.journal.domain.DebitCredit;
import com.finance.smartLedger.journal.domain.JournalEntry;
import com.finance.smartLedger.journal.domain.JournalEntryType;
import com.finance.smartLedger.journal.domain.JournalLineItem;
import com.finance.smartLedger.ledger.application.AccountService;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.domain.valueobject.AccountBalance;
import com.finance.smartLedger.ledger.domain.valueobject.AccountNumber;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncomeStatementGeneratorTest {

  @Mock private AccountService accountService;

  @Mock private JournalEntryService journalEntryService;

  @Mock private ObjectMapper objectMapper;

  @InjectMocks private IncomeStatementGenerator incomeStatementGenerator;

  private List<Account> mockAccounts;
  private UUID revenueAccountId;
  private UUID cashAccountId;

  @BeforeEach
  void setUp() throws JsonProcessingException {
    revenueAccountId = UUID.randomUUID();
    cashAccountId = UUID.randomUUID();
    
    // Create mock revenue account
    Account revenueAccount = Account.builder()
        .accountNumber(AccountNumber.of("00004001"))
        .accountName("Sales Revenue")
        .accountType(AccountType.REVENUE)
        .balance(new AccountBalance(Money.of(BigDecimal.valueOf(3000.00), "USD")))
        .isActive(true)
        .build();
    revenueAccount.setId(revenueAccountId);

    // Create mock cash account
    Account cashAccount = Account.builder()
        .accountNumber(AccountNumber.of("00001001"))
        .accountName("Cash")
        .accountType(AccountType.ASSET)
        .balance(new AccountBalance(Money.of(BigDecimal.valueOf(3000.00), "USD")))
        .isActive(true)
        .build();
    cashAccount.setId(cashAccountId);

    mockAccounts = new ArrayList<>();
    mockAccounts.add(revenueAccount);
    mockAccounts.add(cashAccount);

    when(accountService.findActiveAccounts()).thenReturn(mockAccounts);
    lenient().when(objectMapper.writeValueAsString(any())).thenAnswer(invocation -> {
      Object obj = invocation.getArgument(0);
      return obj.toString();
    });
  }

  @Test
  void generateIncomeStatement_filtersByPeriod() throws Exception {
    // Create journal entry INSIDE the period (January 15, 2024)
    LocalDateTime jan15 = LocalDateTime.of(2024, 1, 15, 10, 0);
    JournalLineItem janRevenueLine = JournalLineItem.builder()
        .accountId(revenueAccountId)
        .accountNumber("00004001")
        .accountName("Sales Revenue")
        .debitCredit(DebitCredit.CREDIT)
        .amount(Money.of(BigDecimal.valueOf(1000.00), "USD"))
        .build();
    
    JournalEntry janEntry = JournalEntry.builder()
        .entryNumber("JE-001")
        .entryDate(jan15)
        .entryType(JournalEntryType.MANUAL)
        .description("January sale")
        .posted(true)
        .lineItems(new ArrayList<>(List.of(janRevenueLine)))
        .build();

    // Mock journal entry service to return only January entry
    when(journalEntryService.findByEntryDateBetween(
        LocalDateTime.of(2024, 1, 1, 0, 0), 
        LocalDateTime.of(2024, 1, 31, 23, 59, 59)))
        .thenReturn(List.of(janEntry));

    // Generate income statement for January only
    LocalDateTime periodStart = LocalDateTime.of(2024, 1, 1, 0, 0);
    LocalDateTime periodEnd = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
    
    String reportJson = incomeStatementGenerator.generateIncomeStatement(periodStart, periodEnd, "USD");

    // Assert: Only the January entry (1000.00) should be included
    assertTrue(reportJson.contains("1000") || reportJson.contains("1000.00"), 
        "Income statement should only include revenue from the specified period (January)");
    assertFalse(reportJson.contains("3000") && !reportJson.contains("3000.00"),
        "Income statement should not include all-time balance");
  }

  @Test
  void generateIncomeStatement_differentPeriodsProduceDifferentResults() throws Exception {
    // Create journal entry in January
    LocalDateTime jan15 = LocalDateTime.of(2024, 1, 15, 10, 0);
    JournalLineItem janRevenueLine = JournalLineItem.builder()
        .accountId(revenueAccountId)
        .accountNumber("00004001")
        .accountName("Sales Revenue")
        .debitCredit(DebitCredit.CREDIT)
        .amount(Money.of(BigDecimal.valueOf(1000.00), "USD"))
        .build();
    
    JournalEntry janEntry = JournalEntry.builder()
        .entryNumber("JE-001")
        .entryDate(jan15)
        .entryType(JournalEntryType.MANUAL)
        .description("January sale")
        .posted(true)
        .lineItems(new ArrayList<>(List.of(janRevenueLine)))
        .build();

    // Create journal entry in February
    LocalDateTime feb15 = LocalDateTime.of(2024, 2, 15, 10, 0);
    JournalLineItem febRevenueLine = JournalLineItem.builder()
        .accountId(revenueAccountId)
        .accountNumber("00004001")
        .accountName("Sales Revenue")
        .debitCredit(DebitCredit.CREDIT)
        .amount(Money.of(BigDecimal.valueOf(2000.00), "USD"))
        .build();
    
    JournalEntry febEntry = JournalEntry.builder()
        .entryNumber("JE-002")
        .entryDate(feb15)
        .entryType(JournalEntryType.MANUAL)
        .description("February sale")
        .posted(true)
        .lineItems(new ArrayList<>(List.of(febRevenueLine)))
        .build();

    // Mock journal entry service to return entries based on date range
    when(journalEntryService.findByEntryDateBetween(
        LocalDateTime.of(2024, 1, 1, 0, 0), 
        LocalDateTime.of(2024, 1, 31, 23, 59, 59)))
        .thenReturn(List.of(janEntry));

    when(journalEntryService.findByEntryDateBetween(
        LocalDateTime.of(2024, 2, 1, 0, 0), 
        LocalDateTime.of(2024, 2, 28, 23, 59, 59)))
        .thenReturn(List.of(febEntry));

    // Generate income statement for January
    LocalDateTime janStart = LocalDateTime.of(2024, 1, 1, 0, 0);
    LocalDateTime janEnd = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
    String janReport = incomeStatementGenerator.generateIncomeStatement(janStart, janEnd, "USD");

    // Generate income statement for February
    LocalDateTime febStart = LocalDateTime.of(2024, 2, 1, 0, 0);
    LocalDateTime febEnd = LocalDateTime.of(2024, 2, 28, 23, 59, 59);
    String febReport = incomeStatementGenerator.generateIncomeStatement(febStart, febEnd, "USD");

    // Assert: Different periods should produce different results
    assertNotEquals(janReport, febReport, 
        "Different periods should produce different revenue totals");
  }
}
