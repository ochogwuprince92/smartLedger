package com.finance.smartLedger.reconciliation.infrastructure.persistence;

import com.finance.smartLedger.reconciliation.domain.MatchStatus;
import com.finance.smartLedger.reconciliation.domain.ReconciliationItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReconciliationItemRepository extends JpaRepository<ReconciliationItem, UUID> {

  @Query("SELECT ri FROM ReconciliationItem ri WHERE ri.reconciliationId = :reconciliationId")
  List<ReconciliationItem> findByReconciliationId(@Param("reconciliationId") UUID reconciliationId);

  @Query("SELECT ri FROM ReconciliationItem ri WHERE ri.matchStatus = :matchStatus")
  List<ReconciliationItem> findByMatchStatus(@Param("matchStatus") MatchStatus matchStatus);

  @Query("SELECT ri FROM ReconciliationItem ri WHERE ri.itemReference = :itemReference")
  List<ReconciliationItem> findByItemReference(@Param("itemReference") String itemReference);

  @Query(
      "SELECT ri FROM ReconciliationItem ri WHERE ri.reconciliationId = :reconciliationId AND ri.matchStatus = :matchStatus")
  List<ReconciliationItem> findByReconciliationIdAndMatchStatus(
      @Param("reconciliationId") UUID reconciliationId,
      @Param("matchStatus") MatchStatus matchStatus);

  @Query("DELETE FROM ReconciliationItem ri WHERE ri.reconciliationId = :reconciliationId")
  void deleteByReconciliationId(@Param("reconciliationId") UUID reconciliationId);
}
