package com.finance.smartLedger.journal.infrastructure.persistence;

import com.finance.smartLedger.journal.domain.JournalLineItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalLineItemRepository extends JpaRepository<JournalLineItem, UUID> {

  @Query("SELECT jli FROM JournalLineItem jli WHERE jli.journalEntryId = :journalEntryId")
  List<JournalLineItem> findByJournalEntryId(@Param("journalEntryId") UUID journalEntryId);

  @Query("SELECT jli FROM JournalLineItem jli WHERE jli.accountId = :accountId")
  List<JournalLineItem> findByAccountId(@Param("accountId") UUID accountId);

  @Query(
      "SELECT jli FROM JournalLineItem jli WHERE jli.journalEntryId = :journalEntryId ORDER BY jli.sequenceNumber")
  List<JournalLineItem> findByJournalEntryIdOrderBySequenceNumber(
      @Param("journalEntryId") UUID journalEntryId);

  @Query("DELETE FROM JournalLineItem jli WHERE jli.journalEntryId = :journalEntryId")
  void deleteByJournalEntryId(@Param("journalEntryId") UUID journalEntryId);
}
