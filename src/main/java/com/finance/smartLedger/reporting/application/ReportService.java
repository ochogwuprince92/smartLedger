package com.finance.smartLedger.reporting.application;

import com.finance.smartLedger.reporting.domain.Report;
import com.finance.smartLedger.reporting.domain.ReportStatus;
import com.finance.smartLedger.reporting.domain.ReportType;
import com.finance.smartLedger.reporting.infrastructure.persistence.ReportRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;
  private final BalanceSheetGenerator balanceSheetGenerator;
  private final IncomeStatementGenerator incomeStatementGenerator;
  private final CashFlowStatementGenerator cashFlowStatementGenerator;

  @Transactional
  public Report createReport(
      String reportNumber,
      LocalDateTime reportDate,
      ReportType reportType,
      LocalDateTime periodStartDate,
      LocalDateTime periodEndDate,
      String currencyCode,
      String description,
      String createdBy) {

    if (reportRepository.existsByReportNumber(reportNumber)) {
      throw new IllegalArgumentException("Report with number " + reportNumber + " already exists");
    }

    Report report =
        new Report(
            reportNumber,
            reportDate,
            reportType,
            periodStartDate,
            periodEndDate,
            currencyCode,
            description,
            createdBy);

    return reportRepository.save(report);
  }

  @Transactional
  public Report generateReport(UUID reportId, String updatedBy) {
    Report report =
        reportRepository
            .findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));

    if (report.getStatus() != ReportStatus.PENDING) {
      throw new IllegalStateException("Can only generate report in PENDING status");
    }

    report.startGenerating(updatedBy);
    reportRepository.save(report);

    try {
      String reportData = generateReportData(report);
      report.complete(null, reportData, updatedBy);
    } catch (Exception e) {
      report.fail(e.getMessage(), updatedBy);
    }

    return reportRepository.save(report);
  }

  private String generateReportData(Report report) throws Exception {
    LocalDateTime asOfDate =
        report.getPeriodEndDate() != null ? report.getPeriodEndDate() : report.getReportDate();
    LocalDateTime periodStartDate = report.getPeriodStartDate();
    LocalDateTime periodEndDate = report.getPeriodEndDate();
    String currencyCode = report.getCurrencyCode();

    return switch (report.getReportType()) {
      case BALANCE_SHEET -> balanceSheetGenerator.generateBalanceSheet(asOfDate, currencyCode);
      case INCOME_STATEMENT ->
          incomeStatementGenerator.generateIncomeStatement(
              periodStartDate, periodEndDate, currencyCode);
      case CASH_FLOW_STATEMENT ->
          cashFlowStatementGenerator.generateCashFlowStatement(
              periodStartDate, periodEndDate, currencyCode);
      case TRIAL_BALANCE ->
          throw new UnsupportedOperationException("Trial balance generation not yet implemented");
      case CUSTOM_REPORT ->
          throw new UnsupportedOperationException("Custom report generation not yet implemented");
    };
  }

  public Optional<Report> findById(UUID id) {
    return reportRepository.findById(id);
  }

  public Optional<Report> findByReportNumber(String reportNumber) {
    return reportRepository.findByReportNumber(reportNumber);
  }

  public List<Report> findByReportType(ReportType reportType) {
    return reportRepository.findByReportType(reportType);
  }

  public List<Report> findByStatus(ReportStatus status) {
    return reportRepository.findByStatus(status);
  }

  public List<Report> findByCurrencyCode(String currencyCode) {
    return reportRepository.findByCurrencyCode(currencyCode);
  }

  public List<Report> findByPeriodDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return reportRepository.findByPeriodDateBetween(startDate, endDate);
  }

  public List<Report> findByReportTypeAndStatus(ReportType reportType, ReportStatus status) {
    return reportRepository.findByReportTypeAndStatusOrderByDateDesc(reportType, status);
  }

  @Transactional
  public void deleteReport(UUID id) {
    Report report =
        reportRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));

    if (report.getStatus() == ReportStatus.GENERATING) {
      throw new IllegalStateException("Cannot delete a report that is currently generating");
    }

    reportRepository.deleteById(id);
  }
}
