package com.finance.smartLedger.receipt.application;

import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.infrastructure.persistence.PaymentRepository;
import com.finance.smartLedger.receipt.domain.Receipt;
import com.finance.smartLedger.receipt.domain.ReceiptStatus;
import com.finance.smartLedger.receipt.infrastructure.persistence.ReceiptRepository;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptService {

  private final ReceiptRepository receiptRepository;
  private final PaymentRepository paymentRepository;
  private final com.finance.smartLedger.notification.application.NotificationService
      notificationService;
  private final com.finance.smartLedger.audit.application.AuditService auditService;

  @Transactional
  public Receipt generateReceipt(UUID paymentId, String createdBy) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

    if (payment.getStatus() != com.finance.smartLedger.payment.domain.PaymentStatus.COMPLETED) {
      throw new IllegalStateException("Can only generate receipts for completed payments");
    }

    Optional<Receipt> existingReceipt = receiptRepository.findByPaymentId(paymentId);
    if (existingReceipt.isPresent()) {
      return existingReceipt.get();
    }

    String receiptNumber = generateReceiptNumber();

    Receipt receipt =
        new Receipt(
            receiptNumber,
            paymentId,
            LocalDateTime.now(),
            payment.getAmount(),
            payment.getCurrencyCode(),
            payment.getPayerName(),
            payment.getPayerEmail(),
            payment.getPayerPhone(),
            payment.getDescription(),
            payment.getPaymentMethod().name(),
            payment.getPaymentNumber(),
            createdBy);

    Receipt savedReceipt = receiptRepository.save(receipt);

    // Audit log for receipt creation
    auditService.logCreate(
        "Receipt",
        savedReceipt.getId(),
        "Receipt generated: " + receiptNumber,
        savedReceipt.toString(),
        createdBy);

    // Send receipt generated notification
    if (payment.getPayerEmail() != null || payment.getPayerPhone() != null) {
      notificationService.sendReceiptGeneratedNotification(
          payment.getPayerEmail(),
          payment.getPayerPhone(),
          receiptNumber,
          payment.getPaymentNumber(),
          savedReceipt.getId(),
          createdBy);
    }

    return savedReceipt;
  }

  @Transactional
  public Receipt markAsSent(UUID receiptId) {
    Receipt receipt =
        receiptRepository
            .findById(receiptId)
            .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));

    String oldStatus = receipt.getStatus().name();
    receipt.markAsSent();
    Receipt savedReceipt = receiptRepository.save(receipt);

    // Audit log for receipt status change
    auditService.logStatusChange(
        "Receipt",
        savedReceipt.getId(),
        "Receipt status changed to SENT",
        oldStatus,
        savedReceipt.getStatus().name(),
        "system");

    return savedReceipt;
  }

  @Transactional
  public Receipt markAsDelivered(UUID receiptId) {
    Receipt receipt =
        receiptRepository
            .findById(receiptId)
            .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));

    String oldStatus = receipt.getStatus().name();
    receipt.markAsDelivered();
    Receipt savedReceipt = receiptRepository.save(receipt);

    // Audit log for receipt status change
    auditService.logStatusChange(
        "Receipt",
        savedReceipt.getId(),
        "Receipt status changed to DELIVERED",
        oldStatus,
        savedReceipt.getStatus().name(),
        "system");

    // Send receipt delivered notification
    if (receipt.getPayerEmail() != null || receipt.getPayerPhone() != null) {
      notificationService.sendReceiptDeliveredNotification(
          receipt.getPayerEmail(),
          receipt.getPayerPhone(),
          receipt.getReceiptNumber(),
          savedReceipt.getId(),
          "system");
    }

    return savedReceipt;
  }

  @Transactional
  public Receipt markAsFailed(UUID receiptId, String reason) {
    Receipt receipt =
        receiptRepository
            .findById(receiptId)
            .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));

    receipt.markAsFailed(reason);
    return receiptRepository.save(receipt);
  }

  @Transactional
  public Receipt cancelReceipt(UUID receiptId) {
    Receipt receipt =
        receiptRepository
            .findById(receiptId)
            .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));

    receipt.cancel();
    return receiptRepository.save(receipt);
  }

  public Optional<Receipt> findById(UUID id) {
    return receiptRepository.findById(id);
  }

  public Optional<Receipt> findByReceiptNumber(String receiptNumber) {
    return receiptRepository.findByReceiptNumber(receiptNumber);
  }

  public Optional<Receipt> findByPaymentId(UUID paymentId) {
    return receiptRepository.findByPaymentId(paymentId);
  }

  public List<Receipt> findByStatus(ReceiptStatus status) {
    return receiptRepository.findByStatus(status);
  }

  public List<Receipt> findByPayerEmail(String payerEmail) {
    return receiptRepository.findByPayerEmail(payerEmail);
  }

  public List<Receipt> findByReceiptDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return receiptRepository.findByReceiptDateBetween(startDate, endDate);
  }

  @Transactional
  public void deleteReceipt(UUID id) {
    Receipt receipt =
        receiptRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));

    if (receipt.getStatus() == ReceiptStatus.DELIVERED) {
      throw new IllegalStateException("Cannot delete a delivered receipt");
    }

    receiptRepository.deleteById(id);
  }

  private String generateReceiptNumber() {
    int year = Year.now().getValue();
    long sequence = receiptRepository.count() + 1;
    return String.format("RCP-%d-%06d", year, sequence);
  }
}
