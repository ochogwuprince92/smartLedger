package com.finance.smartLedger.journal.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.journal.domain.JournalEntry;
import com.finance.smartLedger.journal.domain.JournalEntryType;
import com.finance.smartLedger.journal.infrastructure.persistence.JournalEntryRepository;
import com.finance.smartLedger.journal.infrastructure.persistence.JournalLineItemRepository;
import com.finance.smartLedger.ledger.application.AccountService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JournalEntryServiceTest {

  @Mock private JournalEntryRepository journalEntryRepository;

  @Mock private JournalLineItemRepository journalLineItemRepository;

  @Mock private AccountService accountService;

  private JournalEntryService journalEntryService;

  @BeforeEach
  void setUp() {
    journalEntryService = new JournalEntryService(journalEntryRepository, journalLineItemRepository, accountService);
  }

  @Test
  void createJournalEntry_DuplicateEntryNumber_ReturnsExistingEntry() {
    // RED: This test expects idempotent behavior - duplicate entryNumber should return existing entry
    // Currently this will fail because the implementation throws IllegalArgumentException
    
    String entryNumber = "JE-001";
    JournalEntry existingEntry = new JournalEntry(
        entryNumber,
        LocalDateTime.now(),
        JournalEntryType.MANUAL,
        "REF-001",
        "Test journal entry",
        "test-user"
    );
    existingEntry.setId(UUID.randomUUID());

    when(journalEntryRepository.existsByEntryNumber(entryNumber)).thenReturn(true);
    when(journalEntryRepository.findByEntryNumber(entryNumber)).thenReturn(Optional.of(existingEntry));

    // First call would create the entry (mocked)
    // Second call with same entryNumber should return existing entry
    JournalEntry result = journalEntryService.createJournalEntry(
        entryNumber,
        LocalDateTime.now(),
        JournalEntryType.MANUAL,
        "REF-002",
        "Another journal entry",
        "test-user"
    );

    // Should return the existing entry (idempotent)
    assertNotNull(result);
    assertEquals(existingEntry.getId(), result.getId());
    assertEquals(entryNumber, result.getEntryNumber());

    verify(journalEntryRepository).findByEntryNumber(entryNumber);
    verify(journalEntryRepository, never()).save(any());
  }

  @Test
  void createJournalEntry_NewEntryNumber_CreatesEntry() {
    String entryNumber = "JE-001";
    JournalEntry newEntry = new JournalEntry(
        entryNumber,
        LocalDateTime.now(),
        JournalEntryType.MANUAL,
        "REF-001",
        "Test journal entry",
        "test-user"
    );

    when(journalEntryRepository.existsByEntryNumber(entryNumber)).thenReturn(false);
    when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(newEntry);

    JournalEntry result = journalEntryService.createJournalEntry(
        entryNumber,
        LocalDateTime.now(),
        JournalEntryType.MANUAL,
        "REF-001",
        "Test journal entry",
        "test-user"
    );

    assertNotNull(result);
    assertEquals(entryNumber, result.getEntryNumber());

    verify(journalEntryRepository).existsByEntryNumber(entryNumber);
    verify(journalEntryRepository).save(any(JournalEntry.class));
  }
}
