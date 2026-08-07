package com.finance.smartLedger.fees.infrastructure.persistence;

import com.finance.smartLedger.fees.domain.FeeInvoice;
import com.finance.smartLedger.fees.domain.FeeInvoice.InvoiceStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeInvoiceRepository
    extends JpaRepository<FeeInvoice, UUID>, JpaSpecificationExecutor<FeeInvoice> {

  @Query("SELECT f FROM FeeInvoice f WHERE f.invoiceNumber = :invoiceNumber")
  Optional<FeeInvoice> findByInvoiceNumber(@Param("invoiceNumber") String invoiceNumber);

  @Query(
      "SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FeeInvoice f WHERE f.invoiceNumber = :invoiceNumber")
  boolean existsByInvoiceNumber(@Param("invoiceNumber") String invoiceNumber);

  @Query("SELECT f FROM FeeInvoice f WHERE f.studentId = :studentId")
  List<FeeInvoice> findByStudentId(@Param("studentId") UUID studentId);

  @Query("SELECT f FROM FeeInvoice f WHERE f.academicYear = :academicYear")
  List<FeeInvoice> findByAcademicYear(@Param("academicYear") String academicYear);

  @Query("SELECT f FROM FeeInvoice f WHERE f.academicTerm = :academicTerm")
  List<FeeInvoice> findByAcademicTerm(@Param("academicTerm") String academicTerm);

  @Query("SELECT f FROM FeeInvoice f WHERE f.status = :status")
  List<FeeInvoice> findByStatus(@Param("status") InvoiceStatus status);

  @Query("SELECT f FROM FeeInvoice f WHERE f.dueDate = :dueDate")
  List<FeeInvoice> findByDueDate(@Param("dueDate") LocalDate dueDate);

  @Query(
      "SELECT f FROM FeeInvoice f WHERE f.dueDate < :date AND f.status NOT IN ('PAID', 'CANCELLED', 'WRITTEN_OFF')")
  List<FeeInvoice> findOverdueInvoices(@Param("date") LocalDate date);

  @Query(
      "SELECT f FROM FeeInvoice f WHERE f.studentId = :studentId AND f.academicYear = :academicYear")
  List<FeeInvoice> findByStudentIdAndAcademicYear(
      @Param("studentId") UUID studentId, @Param("academicYear") String academicYear);

  @Query("SELECT f FROM FeeInvoice f WHERE f.studentId = :studentId AND f.status = :status")
  List<FeeInvoice> findByStudentIdAndStatus(
      @Param("studentId") UUID studentId, @Param("status") InvoiceStatus status);

  @Query("SELECT f FROM FeeInvoice f WHERE f.issueDate BETWEEN :startDate AND :endDate")
  List<FeeInvoice> findByIssueDateBetween(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  @Query(
      "SELECT f FROM FeeInvoice f WHERE f.balanceAmount.amount > 0 AND f.status NOT IN ('PAID', 'CANCELLED', 'WRITTEN_OFF')")
  List<FeeInvoice> findUnpaidInvoices();

  @Query(
      "SELECT f FROM FeeInvoice f WHERE f.studentId = :studentId AND f.balanceAmount.amount > 0 AND f.status NOT IN ('PAID', 'CANCELLED', 'WRITTEN_OFF')")
  List<FeeInvoice> findUnpaidInvoicesByStudent(@Param("studentId") UUID studentId);

  @Query("SELECT f FROM FeeInvoice f WHERE f.studentId = :studentId ORDER BY f.issueDate DESC")
  List<FeeInvoice> findByStudentIdOrderByIssueDateDesc(@Param("studentId") UUID studentId);
}
