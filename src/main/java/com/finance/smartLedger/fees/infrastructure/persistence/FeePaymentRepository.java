package com.finance.smartLedger.fees.infrastructure.persistence;

import com.finance.smartLedger.fees.domain.FeePayment;
import com.finance.smartLedger.fees.domain.FeePayment.PaymentStatus;
import com.finance.smartLedger.fees.domain.FeeType;
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
public interface FeePaymentRepository
    extends JpaRepository<FeePayment, UUID>, JpaSpecificationExecutor<FeePayment> {

  @Query("SELECT f FROM FeePayment f WHERE f.receiptNumber = :receiptNumber")
  Optional<FeePayment> findByReceiptNumber(@Param("receiptNumber") String receiptNumber);

  @Query("SELECT f FROM FeePayment f WHERE f.referenceNumber = :referenceNumber")
  Optional<FeePayment> findByReferenceNumber(@Param("referenceNumber") String referenceNumber);

  @Query("SELECT f FROM FeePayment f WHERE f.studentId = :studentId")
  List<FeePayment> findByStudentId(@Param("studentId") UUID studentId);

  @Query("SELECT f FROM FeePayment f WHERE f.invoiceId = :invoiceId")
  List<FeePayment> findByInvoiceId(@Param("invoiceId") UUID invoiceId);

  @Query("SELECT f FROM FeePayment f WHERE f.feeType = :feeType")
  List<FeePayment> findByFeeType(@Param("feeType") FeeType feeType);

  @Query("SELECT f FROM FeePayment f WHERE f.status = :status")
  List<FeePayment> findByStatus(@Param("status") PaymentStatus status);

  @Query("SELECT f FROM FeePayment f WHERE f.paymentDate BETWEEN :startDate AND :endDate")
  List<FeePayment> findByPaymentDateBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT f FROM FeePayment f WHERE f.studentId = :studentId AND f.status = :status")
  List<FeePayment> findByStudentIdAndStatus(
      @Param("studentId") UUID studentId, @Param("status") PaymentStatus status);

  @Query("SELECT f FROM FeePayment f WHERE f.studentId = :studentId AND f.invoiceId = :invoiceId")
  List<FeePayment> findByStudentIdAndInvoiceId(
      @Param("studentId") UUID studentId, @Param("invoiceId") UUID invoiceId);

  @Query("SELECT f FROM FeePayment f WHERE f.status = 'COMPLETED' ORDER BY f.paymentDate DESC")
  List<FeePayment> findCompletedPaymentsOrderByDateDesc();

  @Query("SELECT f FROM FeePayment f WHERE f.studentId = :studentId ORDER BY f.paymentDate DESC")
  List<FeePayment> findByStudentIdOrderByPaymentDateDesc(@Param("studentId") UUID studentId);

  @Query(
      "SELECT SUM(f.amount.amount) FROM FeePayment f WHERE f.studentId = :studentId AND f.status = 'COMPLETED'")
  java.math.BigDecimal sumCompletedPaymentsByStudent(@Param("studentId") UUID studentId);

  @Query(
      "SELECT COUNT(f) FROM FeePayment f WHERE f.studentId = :studentId AND f.status = 'COMPLETED'")
  Long countCompletedPaymentsByStudent(@Param("studentId") UUID studentId);
}
