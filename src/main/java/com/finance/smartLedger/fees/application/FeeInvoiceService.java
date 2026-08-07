package com.finance.smartLedger.fees.application;

import com.finance.smartLedger.audit.application.AuditService;
import com.finance.smartLedger.fees.domain.FeeInvoice;
import com.finance.smartLedger.fees.domain.FeeInvoiceLineItem;
import com.finance.smartLedger.fees.domain.FeePayment;
import com.finance.smartLedger.fees.domain.FeeSchedule;
import com.finance.smartLedger.fees.domain.FeeScheduleItem;
import com.finance.smartLedger.fees.domain.FeeType;
import com.finance.smartLedger.fees.infrastructure.persistence.FeeInvoiceRepository;
import com.finance.smartLedger.fees.infrastructure.persistence.FeePaymentRepository;
import com.finance.smartLedger.fees.infrastructure.persistence.FeeScheduleRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeeInvoiceService {

  private final FeeInvoiceRepository feeInvoiceRepository;
  private final FeeScheduleRepository feeScheduleRepository;
  private final FeePaymentRepository feePaymentRepository;
  private final FeeAccountingService feeAccountingService;
  private final AuditService auditService;

  @Transactional
  public FeeInvoice createInvoice(
      UUID studentId,
      String academicYear,
      String academicTerm,
      String classGrade,
      LocalDate dueDate,
      String createdBy) {
    // Generate invoice number
    String invoiceNumber = generateInvoiceNumber(academicYear);

    FeeInvoice invoice = new FeeInvoice(studentId, invoiceNumber, dueDate);
    invoice.setAcademicYear(academicYear);
    invoice.setAcademicTerm(academicTerm);
    invoice.setCreatedBy(createdBy);
    invoice.setUpdatedBy(createdBy);

    FeeInvoice savedInvoice = feeInvoiceRepository.save(invoice);
    auditService.logCreate(
        "FeeInvoice", savedInvoice.getId(), "Fee invoice created", null, createdBy);

    return savedInvoice;
  }

  @Transactional
  public FeeInvoice generateInvoiceFromSchedule(
      UUID studentId, String scheduleCode, LocalDate dueDate, String createdBy) {
    FeeSchedule schedule =
        feeScheduleRepository
            .findByCode(scheduleCode)
            .orElseThrow(
                () -> new IllegalArgumentException("Fee schedule not found: " + scheduleCode));

    if (!schedule.isActive()) {
      throw new IllegalStateException("Fee schedule is not active: " + scheduleCode);
    }

    FeeInvoice invoice =
        createInvoice(
            studentId,
            schedule.getAcademicYear(),
            schedule.getAcademicTerm(),
            schedule.getClassGrade(),
            dueDate,
            createdBy);

    // Add all fee items from schedule
    for (FeeScheduleItem scheduleItem : schedule.getFeeItems()) {
      invoice.addLineItem(
          scheduleItem.getFeeType(), scheduleItem.getAmount(), scheduleItem.getDescription());
    }

    invoice.setGeneratedBy(createdBy);
    FeeInvoice savedInvoice = feeInvoiceRepository.save(invoice);
    auditService.logCreate(
        "FeeInvoice",
        savedInvoice.getId(),
        "Fee invoice generated from schedule: " + scheduleCode,
        null,
        createdBy);

    return savedInvoice;
  }

  @Transactional
  public FeeInvoice addLineItem(
      UUID invoiceId, FeeType feeType, Money amount, String description, String updatedBy) {
    FeeInvoice invoice =
        feeInvoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

    if (!invoice.isDraft()) {
      throw new IllegalStateException("Cannot add line items to non-draft invoice");
    }

    invoice.addLineItem(feeType, amount, description);
    invoice.setUpdatedBy(updatedBy);

    FeeInvoice savedInvoice = feeInvoiceRepository.save(invoice);
    auditService.logUpdate(
        "FeeInvoice",
        savedInvoice.getId(),
        "Line item added: " + feeType.getDisplayName(),
        null,
        null,
        "lineItems",
        updatedBy);

    return savedInvoice;
  }

  @Transactional
  public FeeInvoice removeLineItem(UUID invoiceId, UUID lineItemId, String updatedBy) {
    FeeInvoice invoice =
        feeInvoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

    if (!invoice.isDraft()) {
      throw new IllegalStateException("Cannot remove line items from non-draft invoice");
    }

    FeeInvoiceLineItem lineItemToRemove =
        invoice.getLineItems().stream()
            .filter(li -> li.getId().equals(lineItemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Line item not found: " + lineItemId));

    invoice.removeLineItem(lineItemToRemove);
    invoice.setUpdatedBy(updatedBy);

    FeeInvoice savedInvoice = feeInvoiceRepository.save(invoice);
    auditService.logUpdate(
        "FeeInvoice",
        savedInvoice.getId(),
        "Line item removed: " + lineItemId,
        null,
        null,
        "lineItems",
        updatedBy);

    return savedInvoice;
  }

  @Transactional
  public FeeInvoice applyDiscount(
      UUID invoiceId, Money discountAmount, String reason, String updatedBy) {
    FeeInvoice invoice =
        feeInvoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

    if (!invoice.isDraft() && !invoice.isIssued()) {
      throw new IllegalStateException("Cannot apply discount to invoice in current status");
    }

    invoice.applyDiscount(discountAmount, reason);
    invoice.setUpdatedBy(updatedBy);

    FeeInvoice savedInvoice = feeInvoiceRepository.save(invoice);
    auditService.logUpdate(
        "FeeInvoice",
        savedInvoice.getId(),
        "Discount applied: " + discountAmount.getAmount() + " - " + reason,
        null,
        null,
        "discountAmount",
        updatedBy);

    return savedInvoice;
  }

  @Transactional
  public FeeInvoice issueInvoice(UUID invoiceId, String updatedBy) {
    FeeInvoice invoice =
        feeInvoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

    if (!invoice.isDraft()) {
      throw new IllegalStateException("Invoice is not in draft status");
    }

    if (invoice.getLineItems().isEmpty()) {
      throw new IllegalStateException("Cannot issue invoice with no line items");
    }

    invoice.markAsIssued();
    invoice.setUpdatedBy(updatedBy);

    FeeInvoice savedInvoice = feeInvoiceRepository.save(invoice);
    auditService.logStatusChange(
        "FeeInvoice", savedInvoice.getId(), "Invoice issued", "DRAFT", "ISSUED", updatedBy);

    return savedInvoice;
  }

  @Transactional
  public FeePayment recordPayment(
      UUID invoiceId,
      FeeType feeType,
      Money amount,
      String paymentMethod,
      String referenceNumber,
      String processedBy) {
    FeeInvoice invoice =
        feeInvoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

    if (!invoice.isIssued() && !invoice.isPartiallyPaid() && !invoice.isOverdue()) {
      throw new IllegalStateException("Cannot record payment for invoice in current status");
    }

    // Create payment record
    FeePayment payment =
        new FeePayment(
            invoice.getStudentId(), invoiceId, feeType, amount, paymentMethod, referenceNumber);
    payment.setProcessedBy(processedBy);
    payment.setUpdatedBy(processedBy);

    FeePayment savedPayment = feePaymentRepository.save(payment);

    // Post to ledger
    feeAccountingService.recordFeePayment(savedPayment);

    // Update invoice
    invoice.getPayments().add(savedPayment);
    invoice.recalculateTotals();
    invoice.setUpdatedBy(processedBy);

    feeInvoiceRepository.save(invoice);

    auditService.logCreate(
        "FeePayment",
        savedPayment.getId(),
        "Fee payment recorded: " + amount.getAmount(),
        null,
        processedBy);

    return savedPayment;
  }

  @Transactional
  public FeePayment completePayment(UUID paymentId, String receiptNumber, String processedBy) {
    FeePayment payment =
        feePaymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

    if (!payment.isPending()) {
      throw new IllegalStateException("Payment is not in pending status");
    }

    payment.markAsCompleted(receiptNumber, processedBy);
    payment.setUpdatedBy(processedBy);

    FeePayment savedPayment = feePaymentRepository.save(payment);

    // Update invoice status
    if (payment.getInvoiceId() != null) {
      FeeInvoice invoice = feeInvoiceRepository.findById(payment.getInvoiceId()).orElse(null);
      if (invoice != null) {
        invoice.recalculateTotals();
        invoice.setUpdatedBy(processedBy);
        feeInvoiceRepository.save(invoice);
      }
    }

    auditService.logStatusChange(
        "FeePayment",
        savedPayment.getId(),
        "Payment completed",
        "PENDING",
        "COMPLETED",
        processedBy);

    return savedPayment;
  }

  @Transactional
  public FeeInvoice cancelInvoice(UUID invoiceId, String reason, String updatedBy) {
    FeeInvoice invoice =
        feeInvoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

    if (invoice.isPaid()) {
      throw new IllegalStateException("Cannot cancel paid invoice");
    }

    invoice.markAsCancelled(reason);
    invoice.setUpdatedBy(updatedBy);

    FeeInvoice savedInvoice = feeInvoiceRepository.save(invoice);
    auditService.logStatusChange(
        "FeeInvoice",
        savedInvoice.getId(),
        "Invoice cancelled: " + reason,
        savedInvoice.getStatus().name(),
        "CANCELLED",
        updatedBy);

    return savedInvoice;
  }

  public FeeInvoice getInvoice(UUID invoiceId) {
    return feeInvoiceRepository
        .findById(invoiceId)
        .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
  }

  public List<FeeInvoice> getInvoicesByStudent(UUID studentId) {
    return feeInvoiceRepository.findByStudentIdOrderByIssueDateDesc(studentId);
  }

  public List<FeeInvoice> getInvoicesByAcademicYear(String academicYear) {
    return feeInvoiceRepository.findByAcademicYear(academicYear);
  }

  public List<FeeInvoice> getUnpaidInvoices() {
    return feeInvoiceRepository.findUnpaidInvoices();
  }

  public List<FeeInvoice> getUnpaidInvoicesByStudent(UUID studentId) {
    return feeInvoiceRepository.findUnpaidInvoicesByStudent(studentId);
  }

  public List<FeeInvoice> getOverdueInvoices() {
    return feeInvoiceRepository.findOverdueInvoices(LocalDate.now());
  }

  public List<FeePayment> getPaymentsByStudent(UUID studentId) {
    return feePaymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId);
  }

  public List<FeePayment> getPaymentsByInvoice(UUID invoiceId) {
    return feePaymentRepository.findByInvoiceId(invoiceId);
  }

  private String generateInvoiceNumber(String academicYear) {
    // Format: INV-YYYY-XXXXX
    String year = academicYear.replace("-", "").substring(2, 4);
    long count = feeInvoiceRepository.count() + 1;
    return String.format("INV-%s-%05d", year, count);
  }
}
