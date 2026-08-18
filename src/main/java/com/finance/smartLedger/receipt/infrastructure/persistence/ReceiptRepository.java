package com.finance.smartLedger.receipt.infrastructure.persistence;

import com.finance.smartLedger.receipt.domain.Receipt;
import com.finance.smartLedger.receipt.domain.ReceiptStatus;
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
public interface ReceiptRepository
    extends JpaRepository<Receipt, UUID>, JpaSpecificationExecutor<Receipt> {

  @Query("SELECT r FROM Receipt r WHERE r.receiptNumber = :receiptNumber")
  Optional<Receipt> findByReceiptNumber(@Param("receiptNumber") String receiptNumber);

  @Query(
      "SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Receipt r WHERE r.receiptNumber = :receiptNumber")
  boolean existsByReceiptNumber(@Param("receiptNumber") String receiptNumber);

  @Query("SELECT r FROM Receipt r WHERE r.paymentId = :paymentId")
  Optional<Receipt> findByPaymentId(@Param("paymentId") UUID paymentId);

  @Query("SELECT r FROM Receipt r WHERE r.status = :status")
  List<Receipt> findByStatus(@Param("status") ReceiptStatus status);

  @Query("SELECT r FROM Receipt r WHERE r.payerEmail = :payerEmail")
  List<Receipt> findByPayerEmail(@Param("payerEmail") String payerEmail);

  @Query("SELECT r FROM Receipt r WHERE r.receiptDate BETWEEN :startDate AND :endDate")
  List<Receipt> findByReceiptDateBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT r FROM Receipt r WHERE r.status = :status ORDER BY r.receiptDate DESC")
  List<Receipt> findByStatusOrderByDateDesc(@Param("status") ReceiptStatus status);
}
