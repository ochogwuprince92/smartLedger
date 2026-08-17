package com.finance.smartLedger.payment.application;

import com.finance.smartLedger.fees.domain.FeeInvoice;
import com.finance.smartLedger.fees.infrastructure.persistence.FeeInvoiceRepository;
import com.finance.smartLedger.payment.application.dto.PaymentVerifyResponse;
import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentCompleted;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import com.finance.smartLedger.payment.domain.PaymentStatus;
import com.finance.smartLedger.payment.infrastructure.external.PaymentGatewayClient;
import com.finance.smartLedger.payment.infrastructure.external.PaystackInitiationResult;
import com.finance.smartLedger.payment.infrastructure.persistence.PaymentRepository;
import com.finance.smartLedger.shared.exception.BusinessException;
import com.finance.smartLedger.shared.exception.ErrorCodes;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentAccountingService paymentAccountingService;
  private final com.finance.smartLedger.receipt.application.ReceiptService receiptService;
  private final com.finance.smartLedger.notification.application.NotificationService
      notificationService;
  private final com.finance.smartLedger.audit.application.AuditService auditService;
  private final com.finance.smartLedger.shared.domain.EventPublisher eventPublisher;
  private final PaymentGatewayClient paymentGatewayClient;
  private final FeeInvoiceRepository feeInvoiceRepository;

  @Transactional
  public Payment createPayment(
      String paymentNumber,
      String idempotencyKey,
      UUID invoiceId,
      LocalDateTime paymentDate,
      PaymentMethod paymentMethod,
      BigDecimal amount,
      String currencyCode,
      String payerName,
      String payerEmail,
      String payerPhone,
      String description,
      String callbackUrl,
      String createdBy) {

    if (paymentRepository.existsByPaymentNumber(paymentNumber)) {
      throw new IllegalArgumentException(
          "Payment with number " + paymentNumber + " already exists");
    }

    // Idempotent payment capture - check if idempotency key already exists
    if (idempotencyKey != null && paymentRepository.existsByIdempotencyKey(idempotencyKey)) {
      Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
      if (existingPayment.isPresent()) {
        // Return existing payment instead of creating duplicate
        // Do NOT re-call gateway on idempotency replay
        return existingPayment.get();
      }
    }

    // Invoice/amount pre-validation - must happen BEFORE gateway call
    // This prevents the "stranded student" scenario where money is taken by Paystack
    // but rejected locally afterward
    if (invoiceId != null) {
      FeeInvoice invoice = feeInvoiceRepository.findById(invoiceId)
          .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "Invoice not found"));
      
      // Validate amount does not exceed outstanding balance
      Money paymentAmount = new Money(amount, currencyCode);
      Money outstandingBalance = invoice.getBalanceAmount();
      
      if (paymentAmount.getAmount().compareTo(outstandingBalance.getAmount()) > 0) {
        throw new BusinessException(ErrorCodes.PAYMENT_EXCEEDS_INVOICE_BALANCE, 
            "Payment amount " + paymentAmount.getAmount() + " exceeds outstanding balance " + outstandingBalance.getAmount());
      }
    }

    Payment payment =
        new Payment(
            paymentNumber,
            idempotencyKey,
            invoiceId,
            paymentDate,
            paymentMethod,
            amount,
            currencyCode,
            payerName,
            payerEmail,
            description,
            createdBy);

    payment.setPayerPhone(payerPhone);
    payment.setCallbackUrl(callbackUrl);

    // Initiate payment with gateway for PAYSTACK method
    if (paymentMethod == PaymentMethod.PAYSTACK) {
      try {
        PaystackInitiationResult initiationResult =
            paymentGatewayClient.initiatePayment(
                amount,
                currencyCode,
                description,
                payerEmail,
                payerName,
                null); // metadata can be added later if needed

        payment.setGatewayReference(initiationResult.reference());
        payment.setAuthorizationUrl(initiationResult.authorizationUrl());
      } catch (Exception e) {
        // Gateway failure - fail the entire creation to keep local and gateway state consistent
        throw new RuntimeException("Failed to initiate payment with gateway: " + e.getMessage(), e);
      }
    }

    Payment savedPayment = paymentRepository.save(payment);

    // Audit log for payment creation
    auditService.logCreate(
        "Payment",
        savedPayment.getId(),
        "Payment created: " + paymentNumber,
        savedPayment.toString(),
        createdBy);

    return savedPayment;
  }

  @Transactional
  public Payment processPayment(UUID paymentId, String updatedBy) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

    if (!payment.canProcess()) {
      throw new IllegalStateException("Payment cannot be processed in current status");
    }

    String oldStatus = payment.getStatus().name();
    payment.startProcessing(updatedBy);
    Payment savedPayment = paymentRepository.save(payment);

    // Audit log for payment status change
    auditService.logStatusChange(
        "Payment",
        savedPayment.getId(),
        "Payment status changed to PROCESSING",
        oldStatus,
        savedPayment.getStatus().name(),
        updatedBy);

    return savedPayment;
  }

  @Transactional
  public Payment completePayment(
      UUID paymentId,
      String gatewayTransactionId,
      String gatewayReference,
      String gatewayResponseCode,
      String gatewayResponseMessage,
      String updatedBy) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

    // Duplicate reference validation - check if gateway reference already used
    if (gatewayReference != null) {
      Optional<Payment> existingByRef = paymentRepository.findByGatewayReference(gatewayReference);
      if (existingByRef.isPresent() && !existingByRef.get().getId().equals(paymentId)) {
        throw new IllegalArgumentException(
            "Gateway reference " + gatewayReference + " already used by another payment");
      }
    }

    String oldStatus = payment.getStatus().name();
    payment.complete(
        gatewayTransactionId,
        gatewayReference,
        gatewayResponseCode,
        gatewayResponseMessage,
        updatedBy);

    Payment savedPayment = paymentRepository.save(payment);

    // Audit log for payment status change
    auditService.logStatusChange(
        "Payment",
        savedPayment.getId(),
        "Payment status changed to COMPLETED",
        oldStatus,
        savedPayment.getStatus().name(),
        updatedBy);

    // Publish PaymentCompleted event for fee payment integration
    PaymentCompleted paymentCompletedEvent =
        new PaymentCompleted(
            savedPayment.getId(),
            savedPayment.getInvoiceId(),
            savedPayment.getAmount(),
            savedPayment.getCurrencyCode(),
            savedPayment.getPaymentMethod().name(),
            savedPayment.getPaymentDate(),
            savedPayment.getPayerName());
    eventPublisher.publish(paymentCompletedEvent);

    // Record the payment transaction in the ledger
    paymentAccountingService.recordPayment(savedPayment);

    // Generate receipt for the completed payment
    receiptService.generateReceipt(savedPayment.getId(), updatedBy);

    // Send payment completed notification
    if (savedPayment.getPayerEmail() != null || savedPayment.getPayerPhone() != null) {
      notificationService.sendPaymentCompletedNotification(
          savedPayment.getPayerEmail(),
          savedPayment.getPayerPhone(),
          savedPayment.getPaymentNumber(),
          savedPayment.getAmount().toString(),
          savedPayment.getCurrencyCode(),
          savedPayment.getId(),
          updatedBy);
    }

    return savedPayment;
  }

  @Transactional
  public Payment failPayment(
      UUID paymentId, String gatewayResponseCode, String gatewayResponseMessage, String updatedBy) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

    String oldStatus = payment.getStatus().name();
    payment.fail(gatewayResponseCode, gatewayResponseMessage, updatedBy);
    Payment savedPayment = paymentRepository.save(payment);

    // Audit log for payment status change
    auditService.logStatusChange(
        "Payment",
        savedPayment.getId(),
        "Payment status changed to FAILED",
        oldStatus,
        savedPayment.getStatus().name(),
        updatedBy);

    // Send payment failed notification
    if (savedPayment.getPayerEmail() != null || savedPayment.getPayerPhone() != null) {
      notificationService.sendPaymentFailedNotification(
          savedPayment.getPayerEmail(),
          savedPayment.getPayerPhone(),
          savedPayment.getPaymentNumber(),
          gatewayResponseMessage,
          savedPayment.getId(),
          updatedBy);
    }

    return savedPayment;
  }

  @Transactional
  public Payment refundPayment(UUID paymentId, String updatedBy) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

    if (!payment.canRefund()) {
      throw new IllegalStateException("Payment cannot be refunded in current status");
    }

    String oldStatus = payment.getStatus().name();
    payment.refund(updatedBy);
    Payment savedPayment = paymentRepository.save(payment);

    // Audit log for payment status change
    auditService.logStatusChange(
        "Payment",
        savedPayment.getId(),
        "Payment status changed to REFUNDED",
        oldStatus,
        savedPayment.getStatus().name(),
        updatedBy);

    // Record the refund transaction in the ledger
    paymentAccountingService.recordPaymentRefund(savedPayment, "Payment refunded");

    return savedPayment;
  }

  @Transactional
  public Payment cancelPayment(UUID paymentId, String updatedBy) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

    String oldStatus = payment.getStatus().name();
    payment.cancel(updatedBy);
    Payment savedPayment = paymentRepository.save(payment);

    // Audit log for payment status change
    auditService.logStatusChange(
        "Payment",
        savedPayment.getId(),
        "Payment status changed to CANCELLED",
        oldStatus,
        savedPayment.getStatus().name(),
        updatedBy);

    return savedPayment;
  }

  public Optional<Payment> findById(UUID id) {
    return paymentRepository.findById(id);
  }

  public Optional<Payment> findByPaymentNumber(String paymentNumber) {
    return paymentRepository.findByPaymentNumber(paymentNumber);
  }

  public Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId) {
    return paymentRepository.findByGatewayTransactionId(gatewayTransactionId);
  }

  public Optional<Payment> findByGatewayReference(String gatewayReference) {
    return paymentRepository.findByGatewayReference(gatewayReference);
  }

  public List<Payment> findByStatus(PaymentStatus status) {
    return paymentRepository.findByStatus(status);
  }

  public List<Payment> findByPaymentMethod(PaymentMethod paymentMethod) {
    return paymentRepository.findByPaymentMethod(paymentMethod);
  }

  public List<Payment> findByCurrencyCode(String currencyCode) {
    return paymentRepository.findByCurrencyCode(currencyCode);
  }

  public List<Payment> findByPayerEmail(String payerEmail) {
    return paymentRepository.findByPayerEmail(payerEmail);
  }

  public List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return paymentRepository.findByPaymentDateBetween(startDate, endDate);
  }

  @Transactional
  public PaymentVerifyResponse verifyPayment(String reference, String updatedBy) {
    // Find payment by gateway reference
    Optional<Payment> paymentOpt = paymentRepository.findByGatewayReference(reference);
    if (paymentOpt.isEmpty()) {
      throw new IllegalArgumentException("Payment with reference " + reference + " not found");
    }

    Payment payment = paymentOpt.get();

    // Call gateway to verify payment
    PaymentVerifyResponse verificationResponse = paymentGatewayClient.verifyPayment(reference);

    if (verificationResponse.status() && verificationResponse.data() != null) {
      // Payment verified successfully - complete it
      PaymentVerifyResponse.PaymentData data = verificationResponse.data();

      // Extract gateway response details
      String gatewayResponseCode = "success";
      String gatewayResponseMessage = data.gatewayResponse();
      String gatewayTransactionId = data.reference();

      // Complete the payment
      completePayment(
          payment.getId(),
          gatewayTransactionId,
          reference,
          gatewayResponseCode,
          gatewayResponseMessage,
          updatedBy);
    } else if (!verificationResponse.status()) {
      // Payment verification failed - mark as failed
      failPayment(
          payment.getId(),
          "verification_failed",
          verificationResponse.message(),
          updatedBy);
    }

    return verificationResponse;
  }

  @Transactional
  public Payment initiateFeePayment(
      UUID invoiceId,
      String payerEmail,
      String payerName,
      String payerPhone,
      String callbackUrl,
      String createdBy) {

    // Fetch invoice to get payment details
    FeeInvoice invoice = feeInvoiceRepository.findById(invoiceId)
        .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "Invoice not found"));

    // Generate payment number
    String paymentNumber = generatePaymentNumber();
    
    // Use invoice balance as payment amount
    Money balanceAmount = invoice.getBalanceAmount();
    
    // Generate idempotency key for this payment
    String idempotencyKey = "FEE_PAY_" + invoiceId + "_" + System.currentTimeMillis();

    // Create payment with Paystack method
    Payment payment = createPayment(
        paymentNumber,
        idempotencyKey,
        invoiceId,
        LocalDateTime.now(),
        PaymentMethod.PAYSTACK,
        balanceAmount.getAmount(),
        balanceAmount.getCurrencyCode(),
        payerName != null ? payerName : "Student",
        payerEmail,
        payerPhone,
        "Fee payment for invoice: " + invoice.getInvoiceNumber(),
        callbackUrl,
        createdBy);

    return payment;
  }

  private String generatePaymentNumber() {
    return "PAY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  @Transactional
  public void deletePayment(UUID id) {
    Payment payment =
        paymentRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

    if (payment.getStatus() == PaymentStatus.PROCESSING) {
      throw new IllegalStateException("Cannot delete a payment that is currently processing");
    }

    paymentRepository.deleteById(id);
  }
}
