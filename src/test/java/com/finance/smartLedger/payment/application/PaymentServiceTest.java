package com.finance.smartLedger.payment.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import com.finance.smartLedger.payment.domain.PaymentStatus;
import com.finance.smartLedger.payment.infrastructure.persistence.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock private PaymentRepository paymentRepository;

  @Mock private PaymentAccountingService paymentAccountingService;

  @Mock private com.finance.smartLedger.receipt.application.ReceiptService receiptService;

  @Mock
  private com.finance.smartLedger.notification.application.NotificationService notificationService;

  @Mock private com.finance.smartLedger.audit.application.AuditService auditService;

  @InjectMocks private PaymentService paymentService;

  private static final String PAYMENT_NUMBER = "PAY-2023-001";
  private static final String PAYER_NAME = "John Doe";
  private static final String PAYER_EMAIL = "john@example.com";
  private static final String PAYER_PHONE = "+1234567890";
  private static final String CREATED_BY = "admin";
  private static final String UPDATED_BY = "admin";

  @BeforeEach
  void setUp() {
    // Reset mocks before each test
    reset(
        paymentRepository,
        paymentAccountingService,
        receiptService,
        notificationService,
        auditService);
  }

  @Test
  void testCreatePayment_Success() {
    // Given
    LocalDateTime paymentDate = LocalDateTime.now();
    PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
    BigDecimal amount = new BigDecimal("100.00");
    String currencyCode = "USD";
    String description = "School fees payment";

    Payment expectedPayment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            paymentDate,
            paymentMethod,
            amount,
            currencyCode,
            PAYER_NAME,
            PAYER_EMAIL,
            description,
            CREATED_BY);
    expectedPayment.setPayerPhone(PAYER_PHONE);

    when(paymentRepository.existsByPaymentNumber(PAYMENT_NUMBER)).thenReturn(false);
    when(paymentRepository.save(any(Payment.class))).thenReturn(expectedPayment);

    // When
    Payment result =
        paymentService.createPayment(
            PAYMENT_NUMBER,
            null,
            paymentDate,
            paymentMethod,
            amount,
            currencyCode,
            PAYER_NAME,
            PAYER_EMAIL,
            PAYER_PHONE,
            description,
            CREATED_BY);

    // Then
    assertNotNull(result);
    assertEquals(PAYMENT_NUMBER, result.getPaymentNumber());
    assertEquals(PaymentStatus.PENDING, result.getStatus());
    assertEquals(amount, result.getAmount());
    assertEquals(currencyCode, result.getCurrencyCode());
    assertEquals(PAYER_NAME, result.getPayerName());
    assertEquals(PAYER_EMAIL, result.getPayerEmail());
    assertEquals(PAYER_PHONE, result.getPayerPhone());

    verify(paymentRepository).existsByPaymentNumber(PAYMENT_NUMBER);
    verify(paymentRepository).save(any(Payment.class));
    verify(auditService)
        .logCreate(
            eq("Payment"),
            isNull(),
            eq("Payment created: " + PAYMENT_NUMBER),
            any(String.class),
            eq(CREATED_BY));
  }

  @Test
  void testCreatePayment_DuplicatePaymentNumber_ThrowsException() {
    // Given
    when(paymentRepository.existsByPaymentNumber(PAYMENT_NUMBER)).thenReturn(true);

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                paymentService.createPayment(
                    PAYMENT_NUMBER,
                    null,
                    LocalDateTime.now(),
                    PaymentMethod.CREDIT_CARD,
                    new BigDecimal("100.00"),
                    "USD",
                    PAYER_NAME,
                    PAYER_EMAIL,
                    PAYER_PHONE,
                    "Test payment",
                    CREATED_BY));

    assertEquals(
        "Payment with number " + PAYMENT_NUMBER + " already exists", exception.getMessage());
    verify(paymentRepository).existsByPaymentNumber(PAYMENT_NUMBER);
    verify(paymentRepository, never()).save(any(Payment.class));
    verify(auditService, never()).logCreate(any(), any(), any(), any(), any());
  }

  @Test
  void testProcessPayment_Success() {
    // Given
    UUID paymentId = UUID.randomUUID();
    Payment payment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            LocalDateTime.now(),
            PaymentMethod.CREDIT_CARD,
            new BigDecimal("100.00"),
            "USD",
            PAYER_NAME,
            PAYER_EMAIL,
            "Test payment",
            CREATED_BY);

    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

    // When
    Payment result = paymentService.processPayment(paymentId, UPDATED_BY);

    // Then
    assertNotNull(result);
    assertEquals(PaymentStatus.PROCESSING, result.getStatus());
    assertNotNull(result.getProcessedAt());

    verify(paymentRepository).findById(paymentId);
    verify(paymentRepository).save(any(Payment.class));
    verify(auditService)
        .logStatusChange(
            eq("Payment"),
            isNull(),
            eq("Payment status changed to PROCESSING"),
            eq("PENDING"),
            eq("PROCESSING"),
            eq(UPDATED_BY));
  }

  @Test
  void testProcessPayment_PaymentNotFound_ThrowsException() {
    // Given
    UUID paymentId = UUID.randomUUID();
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.processPayment(paymentId, UPDATED_BY));

    assertEquals("Payment not found", exception.getMessage());
    verify(paymentRepository).findById(paymentId);
    verify(paymentRepository, never()).save(any(Payment.class));
  }

  @Test
  void testProcessPayment_InvalidStatus_ThrowsException() {
    // Given
    UUID paymentId = UUID.randomUUID();
    Payment payment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            LocalDateTime.now(),
            PaymentMethod.CREDIT_CARD,
            new BigDecimal("100.00"),
            "USD",
            PAYER_NAME,
            PAYER_EMAIL,
            "Test payment",
            CREATED_BY);
    payment.startProcessing(CREATED_BY); // Already in PROCESSING status

    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

    // When & Then
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> paymentService.processPayment(paymentId, UPDATED_BY));

    assertEquals("Payment cannot be processed in current status", exception.getMessage());
    verify(paymentRepository).findById(paymentId);
    verify(paymentRepository, never()).save(any(Payment.class));
  }

  @Test
  void testCompletePayment_Success() {
    // Given
    UUID paymentId = UUID.randomUUID();
    Payment payment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            LocalDateTime.now(),
            PaymentMethod.CREDIT_CARD,
            new BigDecimal("100.00"),
            "USD",
            PAYER_NAME,
            PAYER_EMAIL,
            "Test payment",
            CREATED_BY);
    payment.setId(paymentId);
    payment.startProcessing(CREATED_BY);

    String gatewayTransactionId = "TXN-12345";
    String gatewayReference = "REF-67890";
    String gatewayResponseCode = "200";
    String gatewayResponseMessage = "Success";

    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

    // When
    Payment result =
        paymentService.completePayment(
            paymentId,
            gatewayTransactionId,
            gatewayReference,
            gatewayResponseCode,
            gatewayResponseMessage,
            UPDATED_BY);

    // Then
    assertNotNull(result);
    assertEquals(PaymentStatus.COMPLETED, result.getStatus());
    assertEquals(gatewayTransactionId, result.getGatewayTransactionId());
    assertEquals(gatewayReference, result.getGatewayReference());
    assertEquals(gatewayResponseCode, result.getGatewayResponseCode());
    assertEquals(gatewayResponseMessage, result.getGatewayResponseMessage());
    assertNotNull(result.getCompletedAt());

    verify(paymentRepository).findById(paymentId);
    verify(paymentRepository).save(any(Payment.class));
    verify(auditService)
        .logStatusChange(
            eq("Payment"),
            eq(paymentId),
            eq("Payment status changed to COMPLETED"),
            eq("PROCESSING"),
            eq("COMPLETED"),
            eq(UPDATED_BY));
    verify(paymentAccountingService).recordPayment(payment);
    verify(receiptService).generateReceipt(paymentId, UPDATED_BY);
    verify(notificationService)
        .sendPaymentCompletedNotification(
            PAYER_EMAIL, null, PAYMENT_NUMBER, "100.00", "USD", paymentId, UPDATED_BY);
  }

  @Test
  void testFailPayment_Success() {
    // Given
    UUID paymentId = UUID.randomUUID();
    Payment payment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            LocalDateTime.now(),
            PaymentMethod.CREDIT_CARD,
            new BigDecimal("100.00"),
            "USD",
            PAYER_NAME,
            PAYER_EMAIL,
            "Test payment",
            CREATED_BY);
    payment.setId(paymentId);
    payment.startProcessing(CREATED_BY);

    String gatewayResponseCode = "500";
    String gatewayResponseMessage = "Insufficient funds";

    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

    // When
    Payment result =
        paymentService.failPayment(
            paymentId, gatewayResponseCode, gatewayResponseMessage, UPDATED_BY);

    // Then
    assertNotNull(result);
    assertEquals(PaymentStatus.FAILED, result.getStatus());
    assertEquals(gatewayResponseCode, result.getGatewayResponseCode());
    assertEquals(gatewayResponseMessage, result.getGatewayResponseMessage());
    assertNotNull(result.getFailedAt());

    verify(paymentRepository).findById(paymentId);
    verify(paymentRepository).save(any(Payment.class));
    verify(auditService)
        .logStatusChange(
            eq("Payment"),
            eq(paymentId),
            eq("Payment status changed to FAILED"),
            eq("PROCESSING"),
            eq("FAILED"),
            eq(UPDATED_BY));
    verify(notificationService)
        .sendPaymentFailedNotification(
            PAYER_EMAIL,
            null,
            PAYMENT_NUMBER,
            gatewayResponseMessage,
            paymentId,
            UPDATED_BY);
  }

  @Test
  void testRefundPayment_Success() {
    // Given
    UUID paymentId = UUID.randomUUID();
    Payment payment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            LocalDateTime.now(),
            PaymentMethod.CREDIT_CARD,
            new BigDecimal("100.00"),
            "USD",
            PAYER_NAME,
            PAYER_EMAIL,
            "Test payment",
            CREATED_BY);
    payment.startProcessing(CREATED_BY);
    payment.complete("TXN-12345", "REF-67890", "200", "Success", CREATED_BY);

    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

    // When
    Payment result = paymentService.refundPayment(paymentId, UPDATED_BY);

    // Then
    assertNotNull(result);
    assertEquals(PaymentStatus.REFUNDED, result.getStatus());
    assertNotNull(result.getRefundedAt());

    verify(paymentRepository).findById(paymentId);
    verify(paymentRepository).save(any(Payment.class));
    verify(auditService)
        .logStatusChange(
            eq("Payment"),
            isNull(),
            eq("Payment status changed to REFUNDED"),
            eq("COMPLETED"),
            eq("REFUNDED"),
            eq(UPDATED_BY));
    verify(paymentAccountingService).recordPaymentRefund(payment, "Payment refunded");
  }

  @Test
  void testRefundPayment_InvalidStatus_ThrowsException() {
    // Given
    UUID paymentId = UUID.randomUUID();
    Payment payment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            LocalDateTime.now(),
            PaymentMethod.CREDIT_CARD,
            new BigDecimal("100.00"),
            "USD",
            PAYER_NAME,
            PAYER_EMAIL,
            "Test payment",
            CREATED_BY);

    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

    // When & Then
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> paymentService.refundPayment(paymentId, UPDATED_BY));

    assertEquals("Payment cannot be refunded in current status", exception.getMessage());
    verify(paymentRepository).findById(paymentId);
    verify(paymentRepository, never()).save(any(Payment.class));
  }

  @Test
  void testCancelPayment_Success() {
    // Given
    UUID paymentId = UUID.randomUUID();
    Payment payment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            LocalDateTime.now(),
            PaymentMethod.CREDIT_CARD,
            new BigDecimal("100.00"),
            "USD",
            PAYER_NAME,
            PAYER_EMAIL,
            "Test payment",
            CREATED_BY);

    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

    // When
    Payment result = paymentService.cancelPayment(paymentId, UPDATED_BY);

    // Then
    assertNotNull(result);
    assertEquals(PaymentStatus.CANCELLED, result.getStatus());

    verify(paymentRepository).findById(paymentId);
    verify(paymentRepository).save(any(Payment.class));
    verify(auditService)
        .logStatusChange(
            eq("Payment"),
            isNull(),
            eq("Payment status changed to CANCELLED"),
            eq("PENDING"),
            eq("CANCELLED"),
            eq(UPDATED_BY));
  }

  @Test
  void testFindById_Success() {
    // Given
    UUID paymentId = UUID.randomUUID();
    Payment payment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            LocalDateTime.now(),
            PaymentMethod.CREDIT_CARD,
            new BigDecimal("100.00"),
            "USD",
            PAYER_NAME,
            PAYER_EMAIL,
            "Test payment",
            CREATED_BY);

    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

    // When
    Optional<Payment> result = paymentService.findById(paymentId);

    // Then
    assertTrue(result.isPresent());
    assertEquals(payment, result.get());
    verify(paymentRepository).findById(paymentId);
  }

  @Test
  void testFindById_NotFound() {
    // Given
    UUID paymentId = UUID.randomUUID();
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

    // When
    Optional<Payment> result = paymentService.findById(paymentId);

    // Then
    assertFalse(result.isPresent());
    verify(paymentRepository).findById(paymentId);
  }

  @Test
  void testFindByPaymentNumber_Success() {
    // Given
    Payment payment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            LocalDateTime.now(),
            PaymentMethod.CREDIT_CARD,
            new BigDecimal("100.00"),
            "USD",
            PAYER_NAME,
            PAYER_EMAIL,
            "Test payment",
            CREATED_BY);

    when(paymentRepository.findByPaymentNumber(PAYMENT_NUMBER)).thenReturn(Optional.of(payment));

    // When
    Optional<Payment> result = paymentService.findByPaymentNumber(PAYMENT_NUMBER);

    // Then
    assertTrue(result.isPresent());
    assertEquals(payment, result.get());
    verify(paymentRepository).findByPaymentNumber(PAYMENT_NUMBER);
  }

  @Test
  void testCompletePayment_WithoutNotification() {
    // Given
    UUID paymentId = UUID.randomUUID();
    Payment payment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            LocalDateTime.now(),
            PaymentMethod.CREDIT_CARD,
            new BigDecimal("100.00"),
            "USD",
            PAYER_NAME,
            null, // No email
            "Test payment",
            CREATED_BY);
    payment.startProcessing(CREATED_BY);

    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

    // When
    Payment result =
        paymentService.completePayment(
            paymentId, "TXN-12345", "REF-67890", "200", "Success", UPDATED_BY);

    // Then
    assertNotNull(result);
    assertEquals(PaymentStatus.COMPLETED, result.getStatus());

    // Verify notification service was NOT called
    verify(notificationService, never())
        .sendPaymentCompletedNotification(any(), any(), any(), any(), any(), any(), any());
  }
}
