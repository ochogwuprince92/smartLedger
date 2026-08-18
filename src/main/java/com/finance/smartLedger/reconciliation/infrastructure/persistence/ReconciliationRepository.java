package com.finance.smartLedger.reconciliation.infrastructure.persistence;

import com.finance.smartLedger.reconciliation.domain.Reconciliation;
import com.finance.smartLedger.reconciliation.domain.ReconciliationStatus;
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
public interface ReconciliationRepository
    extends JpaRepository<Reconciliation, UUID>, JpaSpecificationExecutor<Reconciliation> {

  @Query("SELECT r FROM Reconciliation r WHERE r.reconciliationNumber = :reconciliationNumber")
  Optional<Reconciliation> findByReconciliationNumber(
      @Param("reconciliationNumber") String reconciliationNumber);

  @Query(
      "SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reconciliation r WHERE r.reconciliationNumber = :reconciliationNumber")
  boolean existsByReconciliationNumber(@Param("reconciliationNumber") String reconciliationNumber);

  @Query("SELECT r FROM Reconciliation r WHERE r.status = :status")
  List<Reconciliation> findByStatus(@Param("status") ReconciliationStatus status);

  @Query("SELECT r FROM Reconciliation r WHERE r.sourceSystem = :sourceSystem")
  List<Reconciliation> findBySourceSystem(@Param("sourceSystem") String sourceSystem);

  @Query(
      "SELECT r FROM Reconciliation r WHERE r.reconciliationDate BETWEEN :startDate AND :endDate")
  List<Reconciliation> findByReconciliationDateBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT r FROM Reconciliation r WHERE r.suspenseAccountId = :suspenseAccountId")
  List<Reconciliation> findBySuspenseAccountId(@Param("suspenseAccountId") UUID suspenseAccountId);

  @Query(
      "SELECT r FROM Reconciliation r WHERE r.status = :status ORDER BY r.reconciliationDate DESC")
  List<Reconciliation> findByStatusOrderByDateDesc(@Param("status") ReconciliationStatus status);
}
