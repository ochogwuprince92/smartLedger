package com.finance.smartLedger.ledger.infrastructure.persistence;

import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.Transaction;
import com.finance.smartLedger.ledger.domain.TransactionType;
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
public interface TransactionRepository
    extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

  @Query("SELECT t FROM Transaction t WHERE t.referenceNumber = :referenceNumber")
  Optional<Transaction> findByReferenceNumber(@Param("referenceNumber") String referenceNumber);

  @Query("SELECT t FROM Transaction t WHERE t.type = :type")
  List<Transaction> findByType(@Param("type") TransactionType type);

  @Query("SELECT t FROM Transaction t WHERE t.debitAccount = :debitAccount")
  List<Transaction> findByDebitAccount(@Param("debitAccount") Account debitAccount);

  @Query("SELECT t FROM Transaction t WHERE t.creditAccount = :creditAccount")
  List<Transaction> findByCreditAccount(@Param("creditAccount") Account creditAccount);

  @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
  List<Transaction> findByTransactionDateBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT t FROM Transaction t WHERE t.isPosted = true")
  List<Transaction> findByIsPostedTrue();

  @Query(
      "SELECT t FROM Transaction t WHERE t.debitAccount.id = :accountId OR t.creditAccount.id = :accountId")
  List<Transaction> findByAccountId(@Param("accountId") UUID accountId);

  @Query(
      "SELECT t FROM Transaction t WHERE (t.debitAccount.id = :accountId OR t.creditAccount.id = :accountId) AND t.transactionDate BETWEEN :startDate AND :endDate")
  List<Transaction> findByAccountIdAndDateRange(
      @Param("accountId") UUID accountId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  boolean existsByReferenceNumber(String referenceNumber);
}
