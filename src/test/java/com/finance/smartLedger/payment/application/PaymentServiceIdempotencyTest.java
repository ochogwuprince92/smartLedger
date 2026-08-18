package com.finance.smartLedger.payment.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
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
class PaymentServiceIdempotencyTest {

  @Mock private PaymentRepository paymentRepository;

  @Mock private PaymentAccountingService paymentAccountingService;

  @Mock private com.finance.smartLedger.receipt.application.ReceiptService receiptService;

  @Mock private com.finance.smartLedger.notification.application.NotificationService
      notificationService;

  @Mock private com.finance.smartLedger.audit.application.AuditService auditService;

  @Mock private com.finance.smartLedger.shared.domain.EventPublisher eventPublisher;

  @Mock private PaymentGatewayClient paymentGatewayClient;

  @InjectMocks private PaymentService paymentService;

  private Payment existingPayment;
  private UUID paymentId;

  @BeforeEach
  void setUp() {
    paymentId = UUID.randomUUID();
    existingPayment =
        new Payment(
            "PAY-2024-001",
            "test-key-123",
            null,
            LocalDateTime.now(),
            PaymentMethod.BANK_TRANSFER, // Use BANK_TRANSFER to avoid gateway calls
            new BigDecimal("100.00"),
            "USD",
            "John Doe",
            "john@example.com",
            "Test payment",
            "system");
    existingPayment.setId(paymentId);
  }

  @Test
  void createPayment_WithSameIdempotencyKey_ShouldReturnExistingPayment() {
    // Given
    when(paymentRepository.existsByIdempotencyKey("test-key-123")).thenReturn(true);
    when(paymentRepository.findByIdempotencyKey("test-key-123"))
        .thenReturn(Optional.of(existingPayment));

    // When
    Payment result =
        paymentService.createPayment(
            "PAY-2024-001",
            "test-key-123",
            null,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
            new BigDecimal("100.00"),
            "USD",
            "John Doe",
            "john@example.com",
            "+1234567890",
            "Test payment",
            null,
            "system");

    // Then
    assertNotNull(result);
    assertEquals(paymentId, result.getId());
    assertEquals("test-key-123", result.getIdempotencyKey());
    // Verify it returned existing payment, didn't create new
    verify(paymentRepository, never()).save(any(Payment.class));
  }

  @Test
  void createPayment_WithDifferentIdempotencyKeys_ShouldCreateSeparatePayments() {
    // Given
    when(paymentRepository.existsByIdempotencyKey("test-key-123")).thenReturn(false);
    when(paymentRepository.existsByIdempotencyKey("test-key-456")).thenReturn(false);
    when(paymentRepository.existsByPaymentNumber("PAY-2024-001")).thenReturn(false);
    when(paymentRepository.existsByPaymentNumber("PAY-2024-002")).thenReturn(false);
    when(paymentRepository.save(any(Payment.class)))
        .thenReturn(existingPayment)
        .thenAnswer(invocation -> {
          Payment p = invocation.getArgument(0);
          p.setId(UUID.randomUUID());
          return p;
        });

    // When - first payment
    Payment result1 =
        paymentService.createPayment(
            "PAY-2024-001",
            "test-key-123",
            null,
            LocalDateTime.now(),
            PaymentMethod.BANK_TRANSFER, // Use BANK_TRANSFER to avoid gateway calls
            new BigDecimal("100.00"),
            "USD",
            "John Doe",
            "john@example.com",
            "+1234567890",
            "Test payment",
            null,
            "system");

    // When - second payment with different key
    Payment result2 =
        paymentService.createPayment(
            "PAY-2024-002",
            "test-key-456",
            null,
            LocalDateTime.now(),
            PaymentMethod.BANK_TRANSFER, // Use BANK_TRANSFER to avoid gateway calls
            new BigDecimal("200.00"),
            "USD",
            "Jane Doe",
            "jane@example.com",
            "+1234567890",
            "Test payment 2",
            null,
            "system");

    // Then
    assertNotNull(result1);
    assertNotNull(result2);
    assertNotEquals(result1.getId(), result2.getId());
    assertNotEquals(result1.getPaymentNumber(), result2.getPaymentNumber());
    verify(paymentRepository, times(2)).save(any(Payment.class));
  }

  @Test
  void createPayment_WithoutIdempotencyKey_ShouldSucceedWithFallbackBehavior() {
    // Given
    when(paymentRepository.existsByPaymentNumber("PAY-2024-001")).thenReturn(false);
    when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);

    // When - no idempotency key
    Payment result =
        paymentService.createPayment(
            "PAY-2024-001",
            null, // No idempotency key
            null,
            LocalDateTime.now(),
            PaymentMethod.BANK_TRANSFER, // Use BANK_TRANSFER to avoid gateway calls
            new BigDecimal("100.00"),
            "USD",
            "John Doe",
            "john@example.com",
            "+1234567890",
            "Test payment",
            null,
            "system");

    // Then
    assertNotNull(result);
    verify(paymentRepository).save(any(Payment.class));
    // Verify it didn't check idempotency key (since it was null)
    verify(paymentRepository, never()).existsByIdempotencyKey(anyString());
  }
}
