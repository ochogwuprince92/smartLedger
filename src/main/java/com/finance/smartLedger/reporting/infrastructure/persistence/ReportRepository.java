package com.finance.smartLedger.reporting.infrastructure.persistence;

import com.finance.smartLedger.reporting.domain.Report;
import com.finance.smartLedger.reporting.domain.ReportStatus;
import com.finance.smartLedger.reporting.domain.ReportType;
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
public interface ReportRepository
    extends JpaRepository<Report, UUID>, JpaSpecificationExecutor<Report> {

  @Query("SELECT r FROM Report r WHERE r.reportNumber = :reportNumber")
  Optional<Report> findByReportNumber(@Param("reportNumber") String reportNumber);

  @Query(
      "SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Report r WHERE r.reportNumber = :reportNumber")
  boolean existsByReportNumber(@Param("reportNumber") String reportNumber);

  @Query("SELECT r FROM Report r WHERE r.reportType = :reportType")
  List<Report> findByReportType(@Param("reportType") ReportType reportType);

  @Query("SELECT r FROM Report r WHERE r.status = :status")
  List<Report> findByStatus(@Param("status") ReportStatus status);

  @Query("SELECT r FROM Report r WHERE r.currencyCode = :currencyCode")
  List<Report> findByCurrencyCode(@Param("currencyCode") String currencyCode);

  @Query("SELECT r FROM Report r WHERE r.periodStartDate BETWEEN :startDate AND :endDate")
  List<Report> findByPeriodDateBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT r FROM Report r WHERE r.reportType = :reportType AND r.status = :status ORDER BY r.reportDate DESC")
  List<Report> findByReportTypeAndStatusOrderByDateDesc(
      @Param("reportType") ReportType reportType, @Param("status") ReportStatus status);

  @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.reportDate DESC")
  List<Report> findByStatusOrderByDateDesc(@Param("status") ReportStatus status);
}
