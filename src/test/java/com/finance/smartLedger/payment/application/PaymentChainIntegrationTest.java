package com.finance.smartLedger.payment.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.fees.domain.FeeInvoice;
import com.finance.smartLedger.fees.infrastructure.persistence.FeeInvoiceRepository;
import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import com.finance.smartLedger.payment.infrastructure.external.PaymentGatewayClient;
import com.finance.smartLedger.payment.infrastructure.external.PaystackInitiationResult;
import com.finance.smartLedger.payment.infrastructure.persistence.PaymentRepository;
import com.finance.smartLedger.shared.exception.BusinessException;
import com.finance.smartLedger.shared.exception.ErrorCodes;
import com.finance.smartLedger.shared.valueobject.Money;
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
import org.mockito.Mockito;

/**
 * Unit test for payment chain validation logic.
 * 
 * This test validates the core payment validation logic without requiring full Spring context.
 */
@ExtendWith(MockitoExtension.class)
class PaymentChainIntegrationTest {

  @InjectMocks private PaymentService paymentService;
  
  @Mock private PaymentRepository paymentRepository;
  @Mock private FeeInvoiceRepository feeInvoiceRepository;
  @Mock private PaymentGatewayClient paymentGatewayClient;
  @Mock private PaymentAccountingService paymentAccountingService;
  @Mock private com.finance.smartLedger.receipt.application.ReceiptService receiptService;
  @Mock private com.finance.smartLedger.notification.application.NotificationService notificationService;
  @Mock private com.finance.smartLedger.audit.application.AuditService auditService;
  @Mock private com.finance.smartLedger.shared.domain.EventPublisher eventPublisher;

  private UUID testInvoiceId;
  private UUID testStudentId;

  @BeforeEach
  void setUp() {
    // Create test invoice
    testStudentId = UUID.randomUUID();
    testInvoiceId = UUID.randomUUID();
    
    FeeInvoice testInvoice = new FeeInvoice(
        testStudentId,
        "INV-TEST-001",
        java.time.LocalDate.now().plusDays(30)
    );
    
    // Set amounts directly for test
    testInvoice.setSubtotal(new Money(new BigDecimal("1000.00"), "USD"));
    testInvoice.setTotalAmount(new Money(new BigDecimal("1000.00"), "USD"));
    testInvoice.setPaidAmount(Money.zero("USD"));
    testInvoice.setBalanceAmount(new Money(new BigDecimal("1000.00"), "USD"));
    testInvoice.setTaxAmount(Money.zero("USD"));
    testInvoice.setDiscountAmount(Money.zero("USD"));
    testInvoice.setStatus(FeeInvoice.InvoiceStatus.ISSUED);
    
    Mockito.lenient().when(feeInvoiceRepository.findById(testInvoiceId)).thenReturn(Optional.of(testInvoice));
    Mockito.lenient().when(paymentRepository.existsByPaymentNumber(any())).thenReturn(false);
    Mockito.lenient().when(paymentRepository.existsByIdempotencyKey(any())).thenReturn(false);
  }

  @Test
  void amountExceedsOutstandingBalance_ShouldBeRejectedBeforePaystackCall() {
    // Test that payment exceeding outstanding balance is rejected BEFORE Paystack is called
    
    try {
      paymentService.createPayment(
          "PAY-CHAIN-002",
          "idempotency-key-002",
          testInvoiceId,
          LocalDateTime.now(),
          PaymentMethod.PAYSTACK,
          new BigDecimal("1500.00"), // Exceeds outstanding balance of 1000.00
          "USD",
          "Test Student",
          "student@example.com",
          "+1234567890",
          "Test payment exceeding balance",
          "https://callback.example.com",
          "test-fee-portal"
      );
      fail("Should have thrown BusinessException for exceeding balance");
    } catch (BusinessException e) {
      assertEquals(ErrorCodes.PAYMENT_EXCEEDS_INVOICE_BALANCE, e.getErrorCodes());
      assertTrue(e.getMessage().contains("exceeds outstanding balance"));
    }

    // Verify Paystack client was NEVER called
    verify(paymentGatewayClient, never()).initiatePayment(any(), any(), any(), any(), any(), any());
  }

  @Test
  void invalidNonexistentInvoiceId_ShouldBeRejectedBeforePaystackCall() {
    // Test that invalid invoice ID is rejected BEFORE Paystack is called
    
    UUID nonExistentInvoiceId = UUID.randomUUID();
    Mockito.lenient().when(feeInvoiceRepository.findById(nonExistentInvoiceId)).thenReturn(Optional.empty());
    
    try {
      paymentService.createPayment(
          "PAY-CHAIN-003",
          "idempotency-key-003",
          nonExistentInvoiceId,
          LocalDateTime.now(),
          PaymentMethod.PAYSTACK,
          new BigDecimal("500.00"),
          "USD",
          "Test Student",
          "student@example.com",
          "+1234567890",
          "Test payment for nonexistent invoice",
          "https://callback.example.com",
          "test-fee-portal"
      );
      fail("Should have thrown BusinessException for nonexistent invoice");
    } catch (BusinessException e) {
      assertEquals(ErrorCodes.NOT_FOUND, e.getErrorCodes());
      assertTrue(e.getMessage().contains("Invoice not found"));
    }

    // Verify Paystack client was NEVER called
    verify(paymentGatewayClient, never()).initiatePayment(any(), any(), any(), any(), any(), any());
  }

  @Test
  void paystackInitiationFailure_ShouldFailCleanlyWithoutAmbiguousPaymentState() {
    // Test that Paystack initiation failure is handled cleanly without leaving ambiguous PENDING state
    
    // Mock Paystack initiation to throw exception
    when(paymentGatewayClient.initiatePayment(any(), any(), any(), any(), any(), any()))
        .thenThrow(new RuntimeException("Paystack service unavailable"));

    try {
      paymentService.createPayment(
          "PAY-CHAIN-004",
          "idempotency-key-004",
          testInvoiceId,
          LocalDateTime.now(),
          PaymentMethod.PAYSTACK,
          new BigDecimal("500.00"),
          "USD",
          "Test Student",
          "student@example.com",
          "+1234567890",
          "Test payment with gateway failure",
          "https://callback.example.com",
          "test-fee-portal"
      );
      fail("Should have thrown RuntimeException for gateway failure");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Failed to initiate payment with gateway"));
    }

    // Verify no payment was saved (since gateway failed before save)
    verify(paymentRepository, never()).save(any());
  }

  @Test
  void paymentWithoutInvoiceId_Donation_ShouldSkipInvoiceValidation() {
    // Test that payment without invoiceId (e.g., donation) skips invoice validation
    
    // Mock Paystack initiation to return success
    PaystackInitiationResult mockResult = new PaystackInitiationResult(
        "paystack-reference-donation", 
        "https://paystack.co/pay/donation123",
        "ACCESS_CODE_DONATION"
    );
    when(paymentGatewayClient.initiatePayment(any(), any(), any(), any(), any(), any()))
        .thenReturn(mockResult);
    
    // Mock the payment repository to return a payment with gateway values set
    Payment mockPayment = new Payment(
        "PAY-DONATION-001",
        "idempotency-key-donation",
        null, // No invoiceId - donation case
        LocalDateTime.now(),
        PaymentMethod.PAYSTACK,
        new BigDecimal("100.00"),
        "USD",
        "Generous Donor",
        "donor@example.com",
        "Donation",
        "test-fee-portal"
    );
    mockPayment.setGatewayReference("paystack-reference-donation");
    mockPayment.setAuthorizationUrl("https://paystack.co/pay/donation123");
    
    when(paymentRepository.save(any())).thenAnswer(invocation -> {
      Payment savedPayment = invocation.getArgument(0);
      savedPayment.setGatewayReference(mockResult.reference());
      savedPayment.setAuthorizationUrl(mockResult.authorizationUrl());
      return savedPayment;
    });

    // Create payment without invoiceId
    Payment payment = paymentService.createPayment(
        "PAY-DONATION-001",
        "idempotency-key-donation",
        null, // No invoiceId - donation case
        LocalDateTime.now(),
        PaymentMethod.PAYSTACK,
        new BigDecimal("100.00"),
        "USD",
        "Generous Donor",
        "donor@example.com",
        "+1234567890",
        "Donation",
        "https://callback.example.com",
        "test-fee-portal"
    );

    // Verify payment was created successfully
    assertNotNull(payment);
    assertEquals("paystack-reference-donation", payment.getGatewayReference());
    assertEquals("https://paystack.co/pay/donation123", payment.getAuthorizationUrl());

    // Verify Paystack client was called
    verify(paymentGatewayClient, times(1)).initiatePayment(any(), any(), any(), any(), any(), any());
  }

  @Test
  void validAmountWithinBalance_ShouldProceedWithPaystackCall() {
    // Test that valid amount within outstanding balance proceeds to Paystack
    
    // Mock Paystack initiation to return success
    PaystackInitiationResult mockResult = new PaystackInitiationResult(
        "paystack-reference-valid", 
        "https://paystack.co/pay/valid123",
        "ACCESS_CODE_VALID"
    );
    when(paymentGatewayClient.initiatePayment(any(), any(), any(), any(), any(), any()))
        .thenReturn(mockResult);
    
    // Mock the payment repository to return a payment with gateway values set
    Payment mockPayment = new Payment(
        "PAY-VALID-001",
        "idempotency-key-valid",
        testInvoiceId,
        LocalDateTime.now(),
        PaymentMethod.PAYSTACK,
        new BigDecimal("500.00"),
        "USD",
        "Test Student",
        "student@example.com",
        "Valid payment",
        "test-fee-portal"
    );
    mockPayment.setGatewayReference("paystack-reference-valid");
    mockPayment.setAuthorizationUrl("https://paystack.co/pay/valid123");
    
    when(paymentRepository.save(any())).thenAnswer(invocation -> {
      Payment savedPayment = invocation.getArgument(0);
      savedPayment.setGatewayReference(mockResult.reference());
      savedPayment.setAuthorizationUrl(mockResult.authorizationUrl());
      return savedPayment;
    });

    // Create payment with valid amount within balance
    Payment payment = paymentService.createPayment(
        "PAY-VALID-001",
        "idempotency-key-valid",
        testInvoiceId,
        LocalDateTime.now(),
        PaymentMethod.PAYSTACK,
        new BigDecimal("500.00"), // Within outstanding balance of 1000.00
        "USD",
        "Test Student",
        "student@example.com",
        "+1234567890",
        "Valid payment for fee",
        "https://callback.example.com",
        "test-fee-portal"
    );

    // Verify payment was created with Paystack reference
    assertNotNull(payment);
    assertEquals("paystack-reference-valid", payment.getGatewayReference());
    assertEquals("https://paystack.co/pay/valid123", payment.getAuthorizationUrl());

    // Verify Paystack client was called exactly once
    verify(paymentGatewayClient, times(1)).initiatePayment(any(), any(), any(), any(), any(), any());
  }
}