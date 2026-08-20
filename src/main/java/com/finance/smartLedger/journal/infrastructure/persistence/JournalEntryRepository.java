package com.finance.smartLedger.journal.infrastructure.persistence;

import com.finance.smartLedger.journal.domain.JournalEntry;
import com.finance.smartLedger.journal.domain.JournalEntryType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalEntryRepository
    extends JpaRepository<JournalEntry, UUID>, JpaSpecificationExecutor<JournalEntry> {

  @Query("SELECT je FROM JournalEntry je WHERE je.entryNumber = :entryNumber")
  Optional<JournalEntry> findByEntryNumber(@Param("entryNumber") String entryNumber);

  @Query(
      "SELECT CASE WHEN COUNT(je) > 0 THEN true ELSE false END FROM JournalEntry je WHERE je.entryNumber = :entryNumber")
  boolean existsByEntryNumber(@Param("entryNumber") String entryNumber);

  @Query("SELECT je FROM JournalEntry je WHERE je.entryType = :entryType")
  List<JournalEntry> findByEntryType(@Param("entryType") JournalEntryType entryType);

  @Query("SELECT je FROM JournalEntry je WHERE je.posted = :posted")
  List<JournalEntry> findByPosted(@Param("posted") Boolean posted);

  @Query("SELECT je FROM JournalEntry je WHERE je.entryDate BETWEEN :startDate AND :endDate")
  List<JournalEntry> findByEntryDateBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT je FROM JournalEntry je WHERE je.referenceNumber = :referenceNumber")
  List<JournalEntry> findByReferenceNumber(@Param("referenceNumber") String referenceNumber);

  @Query(
      "SELECT je FROM JournalEntry je WHERE je.posted = true ORDER BY je.entryDate DESC, je.entryNumber DESC")
  List<JournalEntry> findPostedEntriesOrderByDateDesc();

  @Query("SELECT DISTINCT je FROM JournalEntry je JOIN je.lineItems li WHERE li.accountId = :accountId")
  List<JournalEntry> findByAccountId(@Param("accountId") UUID accountId);

  @Query("SELECT DISTINCT je FROM JournalEntry je JOIN je.lineItems li WHERE li.accountId = :accountId AND je.entryDate BETWEEN :startDate AND :endDate")
  List<JournalEntry> findByAccountIdAndEntryDateBetween(
      @Param("accountId") UUID accountId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT je FROM JournalEntry je LEFT JOIN FETCH je.lineItems WHERE je.id = :id")
  Optional<JournalEntry> findByIdWithLineItems(@Param("id") UUID id);
}
