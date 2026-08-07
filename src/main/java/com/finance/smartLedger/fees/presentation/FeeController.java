package com.finance.smartLedger.fees.presentation;

import com.finance.smartLedger.fees.application.FeeInvoiceService;
import com.finance.smartLedger.fees.application.FeeScheduleService;
import com.finance.smartLedger.fees.domain.FeeInvoice;
import com.finance.smartLedger.fees.domain.FeePayment;
import com.finance.smartLedger.fees.domain.FeeSchedule;
import com.finance.smartLedger.fees.domain.FeeType;
import com.finance.smartLedger.shared.dto.ApiResponse;
import com.finance.smartLedger.shared.valueobject.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
@Tag(name = "Fees", description = "Fee management endpoints")
public class FeeController {

  private final FeeScheduleService feeScheduleService;
  private final FeeInvoiceService feeInvoiceService;

  // ==================== Fee Schedule Endpoints ====================

  @PostMapping("/schedules")
  @Operation(summary = "Create fee schedule", description = "Creates a new fee schedule")
  @PreAuthorize("hasAuthority('FEE:CREATE')")
  public ResponseEntity<ApiResponse<FeeSchedule>> createSchedule(
      @RequestBody @Valid CreateFeeScheduleRequest request) {
    FeeSchedule schedule =
        feeScheduleService.createSchedule(
            request.code(),
            request.name(),
            request.academicYear(),
            request.academicTerm(),
            request.classGrade(),
            request.effectiveFrom(),
            request.effectiveTo(),
            request.description(),
            "system");
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Fee schedule created successfully", schedule));
  }

  @GetMapping("/schedules/{scheduleId}")
  @Operation(summary = "Get fee schedule", description = "Retrieves a fee schedule by ID")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<FeeSchedule>> getSchedule(@PathVariable UUID scheduleId) {
    FeeSchedule schedule = feeScheduleService.getSchedule(scheduleId);
    return ResponseEntity.ok(ApiResponse.success(schedule));
  }

  @GetMapping("/schedules/code/{code}")
  @Operation(summary = "Get fee schedule by code", description = "Retrieves a fee schedule by code")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<FeeSchedule>> getScheduleByCode(@PathVariable String code) {
    FeeSchedule schedule = feeScheduleService.getScheduleByCode(code);
    return ResponseEntity.ok(ApiResponse.success(schedule));
  }

  @GetMapping("/schedules")
  @Operation(summary = "List fee schedules", description = "Lists all fee schedules")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<List<FeeSchedule>>> getAllSchedules() {
    List<FeeSchedule> schedules = feeScheduleService.getAllSchedules();
    return ResponseEntity.ok(ApiResponse.success(schedules));
  }

  @GetMapping("/schedules/academic-year/{academicYear}")
  @Operation(
      summary = "Get schedules by academic year",
      description = "Retrieves fee schedules for a specific academic year")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<List<FeeSchedule>>> getSchedulesByAcademicYear(
      @PathVariable String academicYear) {
    List<FeeSchedule> schedules = feeScheduleService.getSchedulesByAcademicYear(academicYear);
    return ResponseEntity.ok(ApiResponse.success(schedules));
  }

  @GetMapping("/schedules/class/{classGrade}")
  @Operation(
      summary = "Get schedules by class grade",
      description = "Retrieves fee schedules for a specific class grade")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<List<FeeSchedule>>> getSchedulesByClassGrade(
      @PathVariable String classGrade) {
    List<FeeSchedule> schedules = feeScheduleService.getSchedulesByClassGrade(classGrade);
    return ResponseEntity.ok(ApiResponse.success(schedules));
  }

  @GetMapping("/schedules/active/{academicYear}")
  @Operation(
      summary = "Get active schedules by academic year",
      description = "Retrieves active fee schedules for a specific academic year")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<List<FeeSchedule>>> getActiveSchedulesByAcademicYear(
      @PathVariable String academicYear) {
    List<FeeSchedule> schedules = feeScheduleService.getActiveSchedulesByAcademicYear(academicYear);
    return ResponseEntity.ok(ApiResponse.success(schedules));
  }

  @PostMapping("/schedules/{scheduleId}/items")
  @Operation(
      summary = "Add fee item to schedule",
      description = "Adds a fee item to a fee schedule")
  @PreAuthorize("hasAuthority('FEE:UPDATE')")
  public ResponseEntity<ApiResponse<FeeSchedule>> addFeeItem(
      @PathVariable UUID scheduleId, @RequestBody @Valid AddFeeItemRequest request) {
    FeeSchedule schedule =
        feeScheduleService.addFeeItem(
            scheduleId,
            request.feeType(),
            Money.of(request.amount(), request.currencyCode()),
            request.mandatory(),
            request.description(),
            "system");
    return ResponseEntity.ok(ApiResponse.success("Fee item added successfully", schedule));
  }

  @DeleteMapping("/schedules/{scheduleId}/items/{itemId}")
  @Operation(
      summary = "Remove fee item from schedule",
      description = "Removes a fee item from a fee schedule")
  @PreAuthorize("hasAuthority('FEE:UPDATE')")
  public ResponseEntity<ApiResponse<FeeSchedule>> removeFeeItem(
      @PathVariable UUID scheduleId, @PathVariable UUID itemId) {
    FeeSchedule schedule = feeScheduleService.removeFeeItem(scheduleId, itemId, "system");
    return ResponseEntity.ok(ApiResponse.success("Fee item removed successfully", schedule));
  }

  @PostMapping("/schedules/{scheduleId}/activate")
  @Operation(summary = "Activate fee schedule", description = "Activates a draft fee schedule")
  @PreAuthorize("hasAuthority('FEE:UPDATE')")
  public ResponseEntity<ApiResponse<FeeSchedule>> activateSchedule(@PathVariable UUID scheduleId) {
    FeeSchedule schedule = feeScheduleService.activateSchedule(scheduleId, "system");
    return ResponseEntity.ok(ApiResponse.success("Fee schedule activated successfully", schedule));
  }

  @PostMapping("/schedules/{scheduleId}/deactivate")
  @Operation(
      summary = "Deactivate fee schedule",
      description = "Deactivates an active fee schedule")
  @PreAuthorize("hasAuthority('FEE:UPDATE')")
  public ResponseEntity<ApiResponse<FeeSchedule>> deactivateSchedule(
      @PathVariable UUID scheduleId) {
    FeeSchedule schedule = feeScheduleService.deactivateSchedule(scheduleId, "system");
    return ResponseEntity.ok(
        ApiResponse.success("Fee schedule deactivated successfully", schedule));
  }

  @PutMapping("/schedules/{scheduleId}")
  @Operation(summary = "Update fee schedule", description = "Updates a fee schedule")
  @PreAuthorize("hasAuthority('FEE:UPDATE')")
  public ResponseEntity<ApiResponse<FeeSchedule>> updateSchedule(
      @PathVariable UUID scheduleId, @RequestBody @Valid UpdateFeeScheduleRequest request) {
    FeeSchedule schedule =
        feeScheduleService.updateSchedule(
            scheduleId,
            request.name(),
            request.academicTerm(),
            request.classGrade(),
            request.effectiveFrom(),
            request.effectiveTo(),
            request.description(),
            "system");
    return ResponseEntity.ok(ApiResponse.success("Fee schedule updated successfully", schedule));
  }

  @DeleteMapping("/schedules/{scheduleId}")
  @Operation(summary = "Delete fee schedule", description = "Deletes a fee schedule")
  @PreAuthorize("hasAuthority('FEE:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable UUID scheduleId) {
    feeScheduleService.deleteSchedule(scheduleId, "system");
    return ResponseEntity.ok(ApiResponse.success("Fee schedule deleted successfully", null));
  }

  // ==================== Fee Invoice Endpoints ====================

  @PostMapping("/invoices")
  @Operation(summary = "Create fee invoice", description = "Creates a new fee invoice")
  @PreAuthorize("hasAuthority('FEE:CREATE')")
  public ResponseEntity<ApiResponse<FeeInvoice>> createInvoice(
      @RequestBody @Valid CreateFeeInvoiceRequest request) {
    FeeInvoice invoice =
        feeInvoiceService.createInvoice(
            request.studentId(),
            request.academicYear(),
            request.academicTerm(),
            request.classGrade(),
            request.dueDate(),
            "system");
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Fee invoice created successfully", invoice));
  }

  @PostMapping("/invoices/generate-from-schedule")
  @Operation(
      summary = "Generate invoice from schedule",
      description = "Generates a fee invoice from a fee schedule")
  @PreAuthorize("hasAuthority('FEE:CREATE')")
  public ResponseEntity<ApiResponse<FeeInvoice>> generateInvoiceFromSchedule(
      @RequestBody @Valid GenerateInvoiceFromScheduleRequest request) {
    FeeInvoice invoice =
        feeInvoiceService.generateInvoiceFromSchedule(
            request.studentId(), request.scheduleCode(), request.dueDate(), "system");
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Fee invoice generated successfully", invoice));
  }

  @GetMapping("/invoices/{invoiceId}")
  @Operation(summary = "Get fee invoice", description = "Retrieves a fee invoice by ID")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<FeeInvoice>> getInvoice(@PathVariable UUID invoiceId) {
    FeeInvoice invoice = feeInvoiceService.getInvoice(invoiceId);
    return ResponseEntity.ok(ApiResponse.success(invoice));
  }

  @GetMapping("/invoices/student/{studentId}")
  @Operation(
      summary = "Get invoices by student",
      description = "Retrieves all invoices for a specific student")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<List<FeeInvoice>>> getInvoicesByStudent(
      @PathVariable UUID studentId) {
    List<FeeInvoice> invoices = feeInvoiceService.getInvoicesByStudent(studentId);
    return ResponseEntity.ok(ApiResponse.success(invoices));
  }

  @GetMapping("/invoices/academic-year/{academicYear}")
  @Operation(
      summary = "Get invoices by academic year",
      description = "Retrieves all invoices for a specific academic year")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<List<FeeInvoice>>> getInvoicesByAcademicYear(
      @PathVariable String academicYear) {
    List<FeeInvoice> invoices = feeInvoiceService.getInvoicesByAcademicYear(academicYear);
    return ResponseEntity.ok(ApiResponse.success(invoices));
  }

  @GetMapping("/invoices/unpaid")
  @Operation(summary = "Get unpaid invoices", description = "Retrieves all unpaid invoices")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<List<FeeInvoice>>> getUnpaidInvoices() {
    List<FeeInvoice> invoices = feeInvoiceService.getUnpaidInvoices();
    return ResponseEntity.ok(ApiResponse.success(invoices));
  }

  @GetMapping("/invoices/student/{studentId}/unpaid")
  @Operation(
      summary = "Get unpaid invoices by student",
      description = "Retrieves unpaid invoices for a specific student")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<List<FeeInvoice>>> getUnpaidInvoicesByStudent(
      @PathVariable UUID studentId) {
    List<FeeInvoice> invoices = feeInvoiceService.getUnpaidInvoicesByStudent(studentId);
    return ResponseEntity.ok(ApiResponse.success(invoices));
  }

  @GetMapping("/invoices/overdue")
  @Operation(summary = "Get overdue invoices", description = "Retrieves all overdue invoices")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<List<FeeInvoice>>> getOverdueInvoices() {
    List<FeeInvoice> invoices = feeInvoiceService.getOverdueInvoices();
    return ResponseEntity.ok(ApiResponse.success(invoices));
  }

  @PostMapping("/invoices/{invoiceId}/items")
  @Operation(
      summary = "Add line item to invoice",
      description = "Adds a line item to a fee invoice")
  @PreAuthorize("hasAuthority('FEE:UPDATE')")
  public ResponseEntity<ApiResponse<FeeInvoice>> addInvoiceLineItem(
      @PathVariable UUID invoiceId, @RequestBody @Valid AddInvoiceLineItemRequest request) {
    FeeInvoice invoice =
        feeInvoiceService.addLineItem(
            invoiceId,
            request.feeType(),
            Money.of(request.amount(), request.currencyCode()),
            request.description(),
            "system");
    return ResponseEntity.ok(ApiResponse.success("Line item added successfully", invoice));
  }

  @DeleteMapping("/invoices/{invoiceId}/items/{lineItemId}")
  @Operation(
      summary = "Remove line item from invoice",
      description = "Removes a line item from a fee invoice")
  @PreAuthorize("hasAuthority('FEE:UPDATE')")
  public ResponseEntity<ApiResponse<FeeInvoice>> removeInvoiceLineItem(
      @PathVariable UUID invoiceId, @PathVariable UUID lineItemId) {
    FeeInvoice invoice = feeInvoiceService.removeLineItem(invoiceId, lineItemId, "system");
    return ResponseEntity.ok(ApiResponse.success("Line item removed successfully", invoice));
  }

  @PostMapping("/invoices/{invoiceId}/discount")
  @Operation(
      summary = "Apply discount to invoice",
      description = "Applies a discount to a fee invoice")
  @PreAuthorize("hasAuthority('FEE:UPDATE')")
  public ResponseEntity<ApiResponse<FeeInvoice>> applyDiscount(
      @PathVariable UUID invoiceId, @RequestBody @Valid ApplyDiscountRequest request) {
    FeeInvoice invoice =
        feeInvoiceService.applyDiscount(
            invoiceId,
            Money.of(request.discountAmount(), request.currencyCode()),
            request.reason(),
            "system");
    return ResponseEntity.ok(ApiResponse.success("Discount applied successfully", invoice));
  }

  @PostMapping("/invoices/{invoiceId}/issue")
  @Operation(summary = "Issue invoice", description = "Issues a draft fee invoice")
  @PreAuthorize("hasAuthority('FEE:UPDATE')")
  public ResponseEntity<ApiResponse<FeeInvoice>> issueInvoice(@PathVariable UUID invoiceId) {
    FeeInvoice invoice = feeInvoiceService.issueInvoice(invoiceId, "system");
    return ResponseEntity.ok(ApiResponse.success("Invoice issued successfully", invoice));
  }

  @PostMapping("/invoices/{invoiceId}/cancel")
  @Operation(summary = "Cancel invoice", description = "Cancels a fee invoice")
  @PreAuthorize("hasAuthority('FEE:UPDATE')")
  public ResponseEntity<ApiResponse<FeeInvoice>> cancelInvoice(
      @PathVariable UUID invoiceId, @RequestBody @Valid CancelInvoiceRequest request) {
    FeeInvoice invoice = feeInvoiceService.cancelInvoice(invoiceId, request.reason(), "system");
    return ResponseEntity.ok(ApiResponse.success("Invoice cancelled successfully", invoice));
  }

  // ==================== Fee Payment Endpoints ====================

  @PostMapping("/invoices/{invoiceId}/payments")
  @Operation(summary = "Record fee payment", description = "Records a payment for a fee invoice")
  @PreAuthorize("hasAuthority('FEE:UPDATE')")
  public ResponseEntity<ApiResponse<FeePayment>> recordPayment(
      @PathVariable UUID invoiceId, @RequestBody @Valid RecordFeePaymentRequest request) {
    FeePayment payment =
        feeInvoiceService.recordPayment(
            invoiceId,
            request.feeType(),
            Money.of(request.amount(), request.currencyCode()),
            request.paymentMethod(),
            request.referenceNumber(),
            "system");
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Payment recorded successfully", payment));
  }

  @PostMapping("/payments/{paymentId}/complete")
  @Operation(summary = "Complete payment", description = "Marks a payment as completed")
  @PreAuthorize("hasAuthority('FEE:UPDATE')")
  public ResponseEntity<ApiResponse<FeePayment>> completePayment(
      @PathVariable UUID paymentId, @RequestBody @Valid CompletePaymentRequest request) {
    FeePayment payment =
        feeInvoiceService.completePayment(paymentId, request.receiptNumber(), "system");
    return ResponseEntity.ok(ApiResponse.success("Payment completed successfully", payment));
  }

  @GetMapping("/payments/student/{studentId}")
  @Operation(
      summary = "Get payments by student",
      description = "Retrieves all payments for a specific student")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<List<FeePayment>>> getPaymentsByStudent(
      @PathVariable UUID studentId) {
    List<FeePayment> payments = feeInvoiceService.getPaymentsByStudent(studentId);
    return ResponseEntity.ok(ApiResponse.success(payments));
  }

  @GetMapping("/invoices/{invoiceId}/payments")
  @Operation(
      summary = "Get payments by invoice",
      description = "Retrieves all payments for a specific invoice")
  @PreAuthorize("hasAuthority('FEE:READ')")
  public ResponseEntity<ApiResponse<List<FeePayment>>> getPaymentsByInvoice(
      @PathVariable UUID invoiceId) {
    List<FeePayment> payments = feeInvoiceService.getPaymentsByInvoice(invoiceId);
    return ResponseEntity.ok(ApiResponse.success(payments));
  }

  // ==================== Request DTOs ====================

  record CreateFeeScheduleRequest(
      String code,
      String name,
      String academicYear,
      String academicTerm,
      String classGrade,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo,
      String description) {}

  record AddFeeItemRequest(
      FeeType feeType,
      java.math.BigDecimal amount,
      String currencyCode,
      boolean mandatory,
      String description) {}

  record UpdateFeeScheduleRequest(
      String name,
      String academicTerm,
      String classGrade,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo,
      String description) {}

  record CreateFeeInvoiceRequest(
      UUID studentId,
      String academicYear,
      String academicTerm,
      String classGrade,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {}

  record GenerateInvoiceFromScheduleRequest(
      UUID studentId,
      String scheduleCode,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {}

  record AddInvoiceLineItemRequest(
      FeeType feeType, java.math.BigDecimal amount, String currencyCode, String description) {}

  record ApplyDiscountRequest(
      java.math.BigDecimal discountAmount, String currencyCode, String reason) {}

  record CancelInvoiceRequest(String reason) {}

  record RecordFeePaymentRequest(
      FeeType feeType,
      java.math.BigDecimal amount,
      String currencyCode,
      String paymentMethod,
      String referenceNumber) {}

  record CompletePaymentRequest(String receiptNumber) {}
}
