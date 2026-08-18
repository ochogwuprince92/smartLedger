package com.finance.smartLedger.journal.application;

import com.finance.smartLedger.journal.domain.DebitCredit;
import com.finance.smartLedger.journal.domain.JournalEntry;
import com.finance.smartLedger.journal.domain.JournalEntryType;
import com.finance.smartLedger.journal.domain.JournalLineItem;
import com.finance.smartLedger.journal.infrastructure.persistence.JournalEntryRepository;
import com.finance.smartLedger.journal.infrastructure.persistence.JournalLineItemRepository;
import com.finance.smartLedger.ledger.application.AccountService;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.shared.valueobject.Money;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JournalEntryService {

  private final JournalEntryRepository journalEntryRepository;
  private final JournalLineItemRepository journalLineItemRepository;
  private final AccountService accountService;

  @Transactional
  public JournalEntry createJournalEntry(
      String entryNumber,
      LocalDateTime entryDate,
      JournalEntryType entryType,
      String referenceNumber,
      String description,
      String createdBy) {

    // Idempotent journal entry creation - check if entryNumber already exists
    if (journalEntryRepository.existsByEntryNumber(entryNumber)) {
      Optional<JournalEntry> existingEntry = journalEntryRepository.findByEntryNumber(entryNumber);
      if (existingEntry.isPresent()) {
        // Return existing entry instead of throwing (idempotent behavior)
        return existingEntry.get();
      }
    }

    JournalEntry journalEntry =
        new JournalEntry(
            entryNumber, entryDate, entryType, referenceNumber, description, createdBy);
    return journalEntryRepository.save(journalEntry);
  }

  @Transactional
  public JournalEntry addLineItem(
      UUID journalEntryId,
      UUID accountId,
      DebitCredit debitCredit,
      Money amount,
      String description,
      String createdBy) {

    JournalEntry journalEntry =
        journalEntryRepository
            .findById(journalEntryId)
            .orElseThrow(() -> new IllegalArgumentException("Journal entry not found"));

    if (journalEntry.getPosted()) {
      throw new IllegalStateException("Cannot add line items to a posted journal entry");
    }

    Account account =
        accountService
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));

    if (!account.getIsActive()) {
      throw new IllegalStateException(
          "Cannot post to inactive account: " + account.getAccountNumber());
    }

    int sequenceNumber = journalEntry.getLineItems().size() + 1;

    JournalLineItem lineItem =
        new JournalLineItem(
            journalEntryId,
            accountId,
            account.getAccountNumber().getValue(),
            account.getAccountName(),
            debitCredit,
            amount,
            description,
            sequenceNumber,
            createdBy);

    journalEntry.addLineItem(lineItem);
    journalLineItemRepository.save(lineItem);
    return journalEntryRepository.save(journalEntry);
  }

  @Transactional
  public JournalEntry postJournalEntry(UUID journalEntryId, String postedBy) {
    JournalEntry journalEntry =
        journalEntryRepository
            .findById(journalEntryId)
            .orElseThrow(() -> new IllegalArgumentException("Journal entry not found"));

    journalEntry.validateDoubleEntry();

    journalEntry.post(postedBy);

    updateAccountBalances(journalEntry, postedBy);

    return journalEntryRepository.save(journalEntry);
  }

  private void updateAccountBalances(JournalEntry journalEntry, String updatedBy) {
    for (JournalLineItem lineItem : journalEntry.getLineItems()) {
      Account account = accountService.findById(lineItem.getAccountId()).orElseThrow();

      if (lineItem.getDebitCredit() == DebitCredit.DEBIT) {
        account.debit(lineItem.getAmount(), updatedBy);
      } else {
        account.credit(lineItem.getAmount(), updatedBy);
      }

      accountService.updateAccount(
          account.getId(), account.getAccountName(), account.getDescription(), updatedBy);
    }
  }

  public Optional<JournalEntry> findById(UUID id) {
    return journalEntryRepository.findById(id);
  }

  public Optional<JournalEntry> findByEntryNumber(String entryNumber) {
    return journalEntryRepository.findByEntryNumber(entryNumber);
  }

  public List<JournalEntry> findByEntryType(JournalEntryType entryType) {
    return journalEntryRepository.findByEntryType(entryType);
  }

  public List<JournalEntry> findByPosted(Boolean posted) {
    return journalEntryRepository.findByPosted(posted);
  }

  public List<JournalEntry> findByEntryDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return journalEntryRepository.findByEntryDateBetween(startDate, endDate);
  }

  public List<JournalEntry> findPostedEntriesOrderByDateDesc() {
    return journalEntryRepository.findPostedEntriesOrderByDateDesc();
  }

  public List<JournalEntry> findByAccountId(UUID accountId) {
    return journalEntryRepository.findByAccountId(accountId);
  }

  public List<JournalEntry> findByAccountIdAndDateBetween(UUID accountId, LocalDateTime startDate, LocalDateTime endDate) {
    return journalEntryRepository.findByAccountIdAndEntryDateBetween(accountId, startDate, endDate);
  }

  @Transactional
  public void deleteJournalEntry(UUID id) {
    JournalEntry journalEntry =
        journalEntryRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Journal entry not found"));

    if (journalEntry.getPosted()) {
      throw new IllegalStateException("Cannot delete a posted journal entry");
    }

    journalLineItemRepository.deleteByJournalEntryId(id);
    journalEntryRepository.deleteById(id);
  }
}
