package com.finance.smartLedger.reporting.presentation;

import com.finance.smartLedger.reporting.application.ReportService;
import com.finance.smartLedger.reporting.application.dto.*;
import com.finance.smartLedger.reporting.domain.Report;
import com.finance.smartLedger.reporting.domain.ReportStatus;
import com.finance.smartLedger.reporting.domain.ReportType;
import com.finance.smartLedger.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reporting")
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "Financial reporting endpoints")
public class ReportController {

  private final ReportService reportService;

  @PostMapping("/reports")
  @Operation(summary = "Create report", description = "Creates a new report")
  @PreAuthorize("hasAuthority('REPORTING:CREATE')")
  public ResponseEntity<ApiResponse<ReportResponse>> createReport(
      @RequestBody @Valid CreateReportRequest request) {
    Report report =
        reportService.createReport(
            request.reportNumber(),
            request.reportDate(),
            request.reportType().toDomain(),
            request.periodStartDate(),
            request.periodEndDate(),
            request.currencyCode(),
            request.description(),
            "system");

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Report created successfully", ReportResponse.from(report)));
  }

  @PostMapping("/reports/{id}/generate")
  @Operation(summary = "Generate report", description = "Generates a financial report")
  @PreAuthorize("hasAuthority('REPORTING:UPDATE')")
  public ResponseEntity<ApiResponse<ReportResponse>> generateReport(
      @Parameter(description = "Report ID") @PathVariable UUID id,
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "User generating the report",
          content = @Content(schema = @Schema(implementation = ActionRequest.class)))
      ActionRequest request) {
    Report report = reportService.generateReport(id, request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success("Report generated successfully", ReportResponse.from(report)));
  }

  @GetMapping("/reports/{id}")
  @Operation(summary = "Get report by ID", description = "Retrieves a report by its ID")
  @PreAuthorize("hasAuthority('REPORTING:READ')")
  public ResponseEntity<ApiResponse<ReportResponse>> getReport(
      @Parameter(description = "Report ID") @PathVariable UUID id) {
    Report report =
        reportService
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
    return ResponseEntity.ok(
        ApiResponse.success("Report retrieved successfully", ReportResponse.from(report)));
  }

  @GetMapping("/reports")
  @Operation(summary = "List reports", description = "Lists all reports with optional filters")
  @PreAuthorize("hasAuthority('REPORTING:READ')")
  public ResponseEntity<ApiResponse<List<ReportResponse>>> listReports(
      @Parameter(description = "Filter by report type") @RequestParam(required = false)
          ReportType reportType,
      @Parameter(description = "Filter by status") @RequestParam(required = false)
          ReportStatus status,
      @Parameter(description = "Filter by currency code") @RequestParam(required = false)
          String currencyCode,
      @Parameter(description = "Filter by period start date") @RequestParam(required = false)
          LocalDateTime startDate,
      @Parameter(description = "Filter by period end date") @RequestParam(required = false)
          LocalDateTime endDate) {
    List<Report> reports;

    if (reportType != null && status != null) {
      reports = reportService.findByReportTypeAndStatus(reportType, status);
    } else if (reportType != null) {
      reports = reportService.findByReportType(reportType);
    } else if (status != null) {
      reports = reportService.findByStatus(status);
    } else if (currencyCode != null) {
      reports = reportService.findByCurrencyCode(currencyCode);
    } else if (startDate != null && endDate != null) {
      reports = reportService.findByPeriodDateBetween(startDate, endDate);
    } else {
      reports = reportService.findByStatus(ReportStatus.PENDING);
    }

    List<ReportResponse> responses =
        reports.stream().map(ReportResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @DeleteMapping("/reports/{id}")
  @Operation(
      summary = "Delete report",
      description = "Deletes a report that is not currently generating")
  @PreAuthorize("hasAuthority('REPORTING:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteReport(
      @Parameter(description = "Report ID") @PathVariable UUID id) {
    reportService.deleteReport(id);
    return ResponseEntity.ok(ApiResponse.success("Report deleted successfully", null));
  }

  @PostMapping("/reports/balance-sheet")
  @Operation(summary = "Generate balance sheet", description = "Generates a balance sheet report")
  @PreAuthorize("hasAuthority('REPORTING:CREATE')")
  public ResponseEntity<ApiResponse<ReportResponse>> generateBalanceSheet(
      @RequestBody @Valid CreateReportRequest request) {
    Report report =
        reportService.createReport(
            request.reportNumber(),
            request.reportDate(),
            ReportType.BALANCE_SHEET,
            request.periodStartDate(),
            request.periodEndDate(),
            request.currencyCode(),
            request.description(),
            "system");
    
    report = reportService.generateReport(report.getId(), "system");
    
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Balance sheet generated successfully", ReportResponse.from(report)));
  }

  public record ActionRequest(
      @Schema(description = "User performing the action") String updatedBy) {}
}
