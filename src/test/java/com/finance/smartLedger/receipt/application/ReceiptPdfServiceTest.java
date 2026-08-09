package com.finance.smartLedger.receipt.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import com.finance.smartLedger.payment.infrastructure.persistence.PaymentRepository;
import com.finance.smartLedger.receipt.domain.Receipt;
import com.finance.smartLedger.receipt.domain.ReceiptStatus;
import com.finance.smartLedger.receipt.infrastructure.persistence.ReceiptRepository;
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
class ReceiptPdfServiceTest {

  @Mock private ReceiptRepository receiptRepository;

  @Mock private PaymentRepository paymentRepository;

  @InjectMocks private ReceiptPdfService receiptPdfService;

  private static final UUID RECEIPT_ID = UUID.randomUUID();
  private static final UUID PAYMENT_ID = UUID.randomUUID();
  private static final String RECEIPT_NUMBER = "RCP-2023-000001";
  private static final String PAYMENT_NUMBER = "PAY-2023-001";

  private Receipt receipt;
  private Payment payment;

  @BeforeEach
  void setUp() {
    receipt =
        Receipt.builder()
            .receiptNumber(RECEIPT_NUMBER)
            .paymentId(PAYMENT_ID)
            .receiptDate(LocalDateTime.now())
            .status(ReceiptStatus.GENERATED)
            .amount(new BigDecimal("100.00"))
            .currencyCode("USD")
            .payerName("John Doe")
            .payerEmail("john@example.com")
            .payerPhone("+1234567890")
            .description("School fees payment")
            .paymentMethod("CREDIT_CARD")
            .paymentReference(PAYMENT_NUMBER)
            .build();
    receipt.setId(RECEIPT_ID);

    payment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            LocalDateTime.now(),
            PaymentMethod.CREDIT_CARD,
            new BigDecimal("100.00"),
            "USD",
            "John Doe",
            "john@example.com",
            "School fees payment",
            "admin");
    payment.startProcessing("admin");
    payment.complete("TXN-12345", "REF-67890", "200", "Success", "admin");
  }

  @Test
  void testGenerateReceiptPdf_Success() {
    when(receiptRepository.findById(RECEIPT_ID)).thenReturn(Optional.of(receipt));
    when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

    byte[] pdfBytes = receiptPdfService.generateReceiptPdf(RECEIPT_ID);

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0);
    assertEquals("%PDF", new String(pdfBytes, 0, 4));

    verify(receiptRepository).findById(RECEIPT_ID);
    verify(paymentRepository).findById(PAYMENT_ID);
  }

  @Test
  void testGenerateReceiptPdf_ReceiptNotFound_ThrowsException() {
    when(receiptRepository.findById(RECEIPT_ID)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> receiptPdfService.generateReceiptPdf(RECEIPT_ID));

    assertEquals("Receipt not found", exception.getMessage());
    verify(receiptRepository).findById(RECEIPT_ID);
    verify(paymentRepository, never()).findById(any());
  }

  @Test
  void testGenerateReceiptPdf_PaymentNotFound_ThrowsException() {
    when(receiptRepository.findById(RECEIPT_ID)).thenReturn(Optional.of(receipt));
    when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> receiptPdfService.generateReceiptPdf(RECEIPT_ID));

    assertEquals("Payment not found", exception.getMessage());
    verify(receiptRepository).findById(RECEIPT_ID);
    verify(paymentRepository).findById(PAYMENT_ID);
  }

  @Test
  void testGenerateReceiptPdf_WithNullPayerFields() {
    receipt.setPayerEmail(null);
    receipt.setPayerPhone(null);

    when(receiptRepository.findById(RECEIPT_ID)).thenReturn(Optional.of(receipt));
    when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

    byte[] pdfBytes = receiptPdfService.generateReceiptPdf(RECEIPT_ID);

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0);
  }

  @Test
  void testGenerateReceiptPdf_WithDifferentCurrency() {
    receipt.setCurrencyCode("EUR");
    payment.setCurrencyCode("EUR");

    when(receiptRepository.findById(RECEIPT_ID)).thenReturn(Optional.of(receipt));
    when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

    byte[] pdfBytes = receiptPdfService.generateReceiptPdf(RECEIPT_ID);

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0);
  }
}
