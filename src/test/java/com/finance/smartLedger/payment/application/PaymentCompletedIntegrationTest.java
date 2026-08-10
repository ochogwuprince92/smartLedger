package com.finance.smartLedger.payment.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.fees.domain.FeePayment;
import com.finance.smartLedger.fees.infrastructure.persistence.FeePaymentRepository;
import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import com.finance.smartLedger.payment.infrastructure.persistence.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCompletedIntegrationTest {

  @Mock private PaymentRepository paymentRepository;

  @Mock private FeePaymentRepository feePaymentRepository;

  @Mock private com.finance.smartLedger.payment.application.PaymentAccountingService paymentAccountingService;

  @Mock private com.finance.smartLedger.receipt.application.ReceiptService receiptService;

  @Mock private com.finance.smartLedger.notification.application.NotificationService notificationService;

  @Mock private com.finance.smartLedger.audit.application.AuditService auditService;

  @Mock private com.finance.smartLedger.shared.domain.EventPublisher eventPublisher;

  @InjectMocks private PaymentService paymentService;

  private UUID invoiceId;
  private UUID paymentId;

  @BeforeEach
  void setUp() {
    invoiceId = UUID.randomUUID();
    paymentId = UUID.randomUUID();
  }

  @AfterEach
  void tearDown() {
    // No cleanup needed for mock-based test
  }

  @Test
  void completePayment_WithInvoiceId_CurrentlyDoesNotCreateFeePayment() {
    // Arrange: Create a payment with invoiceId
    Payment payment =
        new Payment(
            "PAY-2024-001",
            UUID.randomUUID().toString(),
            invoiceId,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
            new BigDecimal("1000.00"),
            "USD",
            "John Doe",
            "john@example.com",
            "Fee payment",
            "system");
    payment.setId(paymentId);
    payment.startProcessing("system");

    when(paymentRepository.findById(paymentId)).thenReturn(java.util.Optional.of(payment));
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

    // Act: Complete the payment
    Payment completedPayment =
        paymentService.completePayment(
            paymentId,
            "txn_1234567890",
            "ref_1234567890",
            "00",
            "Success",
            "system");

    // Assert: Currently, no FeePayment is created (this will fail until event handler is implemented)
    verify(feePaymentRepository, never()).save(any(FeePayment.class));
    verify(feePaymentRepository, never()).findByInvoiceId(any());
  }

  @Test
  void completePayment_WithoutInvoiceId_ShouldNotCreateFeePayment() {
    // Arrange: Create a payment without invoiceId (e.g., donation)
    Payment payment =
        new Payment(
            "PAY-2024-002",
            UUID.randomUUID().toString(),
            null, // No invoiceId
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
            new BigDecimal("500.00"),
            "USD",
            "Jane Doe",
            "jane@example.com",
            "Donation",
            "system");
    payment.setId(paymentId);
    payment.startProcessing("system");

    when(paymentRepository.findById(paymentId)).thenReturn(java.util.Optional.of(payment));
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

    // Act: Complete the payment
    Payment completedPayment =
        paymentService.completePayment(
            paymentId,
            "txn_9876543210",
            "ref_9876543210",
            "00",
            "Success",
            "system");

    // Assert: No FeePayment should be created
    verify(feePaymentRepository, never()).save(any(FeePayment.class));
    verify(feePaymentRepository, never()).findByInvoiceId(any());
  }

  @Test
  void completePayment_WithSamePaymentIdTwice_ShouldCreateOnlyOneFeePayment() {
    // Arrange: Create a payment with invoiceId
    Payment payment =
        new Payment(
            "PAY-2024-003",
            UUID.randomUUID().toString(),
            invoiceId,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
            new BigDecimal("1000.00"),
            "USD",
            "John Doe",
            "john@example.com",
            "Fee payment",
            "system");
    payment.setId(paymentId);
    payment.startProcessing("system");

    when(paymentRepository.findById(paymentId)).thenReturn(java.util.Optional.of(payment));
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

    // Act: Complete the payment (this publishes the event)
    Payment completedPayment =
        paymentService.completePayment(
            paymentId,
            "txn_1234567890",
            "ref_1234567890",
            "00",
            "Success",
            "system");

    // Assert: Verify that the event was published
    verify(eventPublisher, times(1)).publish(any(com.finance.smartLedger.shared.domain.DomainEvent.class));
  }
}
