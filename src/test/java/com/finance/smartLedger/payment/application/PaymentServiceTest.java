package com.finance.smartLedger.payment.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import com.finance.smartLedger.payment.domain.PaymentStatus;
import com.finance.smartLedger.payment.infrastructure.external.PaymentGatewayClient;
import com.finance.smartLedger.payment.infrastructure.external.PaystackInitiationResult;
import com.finance.smartLedger.payment.infrastructure.persistence.PaymentRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
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

  @Mock private com.finance.smartLedger.shared.domain.EventPublisher eventPublisher;

  @Mock private PaymentGatewayClient paymentGatewayClient;

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
        auditService,
        paymentGatewayClient);
  }

  @Test
  void testCreatePayment_Success() {
    // Given
    LocalDateTime paymentDate = LocalDateTime.now();
    PaymentMethod paymentMethod = PaymentMethod.BANK_TRANSFER; // Use BANK_TRANSFER to avoid gateway call
    BigDecimal amount = new BigDecimal("100.00");
    String currencyCode = "USD";
    String description = "School fees payment";

    Payment expectedPayment =
        new Payment(
            PAYMENT_NUMBER,
            null,
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
            null,
            paymentDate,
            paymentMethod,
            amount,
            currencyCode,
            PAYER_NAME,
            PAYER_EMAIL,
            PAYER_PHONE,
            description,
            null,
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
    // Verify gateway was NOT called for BANK_TRANSFER
    verify(paymentGatewayClient, never()).initiatePayment(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void testCreatePayment_WithGateway_CallsGatewayClientExactlyOnce() {
    // Given
    LocalDateTime paymentDate = LocalDateTime.now();
    PaymentMethod paymentMethod = PaymentMethod.PAYSTACK;
    BigDecimal amount = new BigDecimal("100.00");
    String currencyCode = "USD";
    String description = "School fees payment";
    String callbackUrl = "https://example.com/payment/callback";

    Payment expectedPayment =
        new Payment(
            PAYMENT_NUMBER,
            null,
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

    PaystackInitiationResult gatewayResult =
        new PaystackInitiationResult("REF-12345", "https://paystack.co/checkout/REF-12345", "ACCESS_CODE_12345");

    when(paymentRepository.existsByPaymentNumber(PAYMENT_NUMBER)).thenReturn(false);
    when(paymentRepository.save(any(Payment.class))).thenReturn(expectedPayment);
    
    // Stub gateway client with specific arguments
    when(paymentGatewayClient.initiatePayment(
            eq(amount),
            eq(currencyCode),
            eq(description),
            eq(PAYER_EMAIL),
            eq(PAYER_NAME),
            any(),
            eq(callbackUrl)))
        .thenReturn(gatewayResult);

    // When
    Payment result =
        paymentService.createPayment(
            PAYMENT_NUMBER,
            null,
            null,
            paymentDate,
            paymentMethod,
            amount,
            currencyCode,
            PAYER_NAME,
            PAYER_EMAIL,
            PAYER_PHONE,
            description,
            callbackUrl,
            CREATED_BY);

    // Then
    verify(paymentGatewayClient, times(1))
        .initiatePayment(
            eq(amount),
            eq(currencyCode),
            eq(description),
            eq(PAYER_EMAIL),
            eq(PAYER_NAME),
            any(),
            eq(callbackUrl));
    assertNotNull(result);
    assertEquals(PAYMENT_NUMBER, result.getPaymentNumber());
  }

  @Test
  void testCreatePayment_GatewayFailure_ShouldFailCreation() {
    // Given
    LocalDateTime paymentDate = LocalDateTime.now();
    PaymentMethod paymentMethod = PaymentMethod.PAYSTACK;
    BigDecimal amount = new BigDecimal("100.00");
    String currencyCode = "USD";
    String description = "School fees payment";
    String callbackUrl = "https://example.com/payment/callback";

    when(paymentRepository.existsByPaymentNumber(PAYMENT_NUMBER)).thenReturn(false);
    
    // Stub gateway client to throw exception
    when(paymentGatewayClient.initiatePayment(
            eq(amount),
            eq(currencyCode),
            eq(description),
            eq(PAYER_EMAIL),
            eq(PAYER_NAME),
            any(),
            eq(callbackUrl)))
        .thenThrow(new RuntimeException("Gateway unreachable"));

    // When & Then
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () ->
                paymentService.createPayment(
                    PAYMENT_NUMBER,
                    null,
                    null,
                    paymentDate,
                    paymentMethod,
                    amount,
                    currencyCode,
                    PAYER_NAME,
                    PAYER_EMAIL,
                    PAYER_PHONE,
                    description,
                    callbackUrl,
                    CREATED_BY));

    assertTrue(exception.getMessage().contains("Failed to initiate payment with gateway"));
    verify(paymentGatewayClient, times(1))
        .initiatePayment(
            eq(amount),
            eq(currencyCode),
            eq(description),
            eq(PAYER_EMAIL),
            eq(PAYER_NAME),
            any(),
            eq(callbackUrl));
    // Verify payment was NOT saved when gateway failed
    verify(paymentRepository, never()).save(any(Payment.class));
  }

  @Test
  void testCreatePayment_IdempotencyReplay_ShouldNotRecallGateway() {
    // Given
    String idempotencyKey = "test-key-123";
    LocalDateTime paymentDate = LocalDateTime.now();
    PaymentMethod paymentMethod = PaymentMethod.PAYSTACK;
    BigDecimal amount = new BigDecimal("100.00");
    String currencyCode = "USD";
    String description = "School fees payment";
    String callbackUrl = "https://example.com/payment/callback";

    Payment existingPayment =
        new Payment(
            PAYMENT_NUMBER,
            idempotencyKey,
            null,
            paymentDate,
            paymentMethod,
            amount,
            currencyCode,
            PAYER_NAME,
            PAYER_EMAIL,
            description,
            CREATED_BY);
    existingPayment.setPayerPhone(PAYER_PHONE);
    existingPayment.setAuthorizationUrl("https://paystack.co/checkout/REF-12345");
    existingPayment.setCallbackUrl(callbackUrl);

    when(paymentRepository.existsByPaymentNumber(PAYMENT_NUMBER)).thenReturn(false);
    when(paymentRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);
    when(paymentRepository.findByIdempotencyKey(idempotencyKey))
        .thenReturn(Optional.of(existingPayment));

    // When - replay with same idempotency key
    Payment result =
        paymentService.createPayment(
            PAYMENT_NUMBER,
            idempotencyKey,
            null,
            paymentDate,
            paymentMethod,
            amount,
            currencyCode,
            PAYER_NAME,
            PAYER_EMAIL,
            PAYER_PHONE,
            description,
            callbackUrl,
            CREATED_BY);

    // Then - This test will FAIL because current implementation doesn't call gateway at all
    // But once implemented, it should verify gateway is NOT called on replay
    assertNotNull(result);
    assertEquals(PAYMENT_NUMBER, result.getPaymentNumber());
    assertEquals(idempotencyKey, result.getIdempotencyKey());
    assertEquals("https://paystack.co/checkout/REF-12345", result.getAuthorizationUrl());
    assertEquals(callbackUrl, result.getCallbackUrl());
    
    // Verify gateway was NOT called on replay (idempotency)
    verify(paymentGatewayClient, never())
        .initiatePayment(
            any(BigDecimal.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(Map.class),
            any(String.class));
    // Verify existing payment was returned, not saved again
    verify(paymentRepository, never()).save(any(Payment.class));
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
                    null,
                    LocalDateTime.now(),
                    PaymentMethod.PAYSTACK,
                    new BigDecimal("100.00"),
                    "USD",
                    PAYER_NAME,
                    PAYER_EMAIL,
                    PAYER_PHONE,
                    "Test payment",
                    null,
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
            null,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
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
            null,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
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
            null,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
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
            null,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
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
            null,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
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
            null,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
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
            null,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
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
            null,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
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
            null,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
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
            null,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
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

  @Test
  void testCreatePayment_WithBankTransfer_Success() {
    // Given
    LocalDateTime paymentDate = LocalDateTime.now();
    PaymentMethod paymentMethod = PaymentMethod.BANK_TRANSFER;
    BigDecimal amount = new BigDecimal("500.00");
    String currencyCode = "USD";
    String description = "Bank transfer payment";

    Payment expectedPayment =
        new Payment(
            PAYMENT_NUMBER,
            null,
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
            null,
            paymentDate,
            paymentMethod,
            amount,
            currencyCode,
            PAYER_NAME,
            PAYER_EMAIL,
            PAYER_PHONE,
            description,
            null,
            CREATED_BY);

    // Then
    assertNotNull(result);
    assertEquals(PAYMENT_NUMBER, result.getPaymentNumber());
    assertEquals(PaymentStatus.PENDING, result.getStatus());
    assertEquals(PaymentMethod.BANK_TRANSFER, result.getPaymentMethod());
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
    // Verify gateway was NOT called for BANK_TRANSFER
    verify(paymentGatewayClient, never()).initiatePayment(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void testCreatePayment_CallbackUrl_PassedToPaystackGateway() {
    // Given
    LocalDateTime paymentDate = LocalDateTime.now();
    PaymentMethod paymentMethod = PaymentMethod.PAYSTACK;
    BigDecimal amount = new BigDecimal("100.00");
    String currencyCode = "USD";
    String description = "School fees payment";
    String callbackUrl = "https://custom-callback.example.com/payment/callback";

    Payment expectedPayment =
        new Payment(
            PAYMENT_NUMBER,
            null,
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

    PaystackInitiationResult gatewayResult =
        new PaystackInitiationResult("REF-12345", "https://paystack.co/checkout/REF-12345", "ACCESS_CODE_12345");

    when(paymentRepository.existsByPaymentNumber(PAYMENT_NUMBER)).thenReturn(false);
    when(paymentRepository.save(any(Payment.class))).thenReturn(expectedPayment);
    
    // Stub gateway client with specific callbackUrl assertion
    when(paymentGatewayClient.initiatePayment(
            eq(amount),
            eq(currencyCode),
            eq(description),
            eq(PAYER_EMAIL),
            eq(PAYER_NAME),
            any(),
            eq(callbackUrl)))
        .thenReturn(gatewayResult);

    // When
    Payment result =
        paymentService.createPayment(
            PAYMENT_NUMBER,
            null,
            null,
            paymentDate,
            paymentMethod,
            amount,
            currencyCode,
            PAYER_NAME,
            PAYER_EMAIL,
            PAYER_PHONE,
            description,
            callbackUrl,
            CREATED_BY);

    // Then
    assertNotNull(result);
    assertEquals(PAYMENT_NUMBER, result.getPaymentNumber());
    
    // Verify callbackUrl was passed to Paystack gateway
    verify(paymentGatewayClient, times(1))
        .initiatePayment(
            eq(amount),
            eq(currencyCode),
            eq(description),
            eq(PAYER_EMAIL),
            eq(PAYER_NAME),
            any(),
            eq(callbackUrl));
  }
}
