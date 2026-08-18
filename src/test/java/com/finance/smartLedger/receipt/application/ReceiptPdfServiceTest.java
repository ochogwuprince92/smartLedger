package com.finance.smartLedger.receipt.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import com.finance.smartLedger.payment.infrastructure.persistence.PaymentRepository;
import com.finance.smartLedger.receipt.domain.Receipt;
import com.finance.smartLedger.receipt.domain.ReceiptStatus;
import com.finance.smartLedger.receipt.infrastructure.persistence.ReceiptRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

@ExtendWith(MockitoExtension.class)
class ReceiptPdfServiceTest {

  @Mock private ReceiptRepository receiptRepository;

  @Mock private PaymentRepository paymentRepository;

  @Mock private TemplateEngine templateEngine;

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
            .paymentMethod("PAYSTACK")
            .paymentReference(PAYMENT_NUMBER)
            .build();
    receipt.setId(RECEIPT_ID);

    payment =
        new Payment(
            PAYMENT_NUMBER,
            null,
            null,
            LocalDateTime.now(),
            PaymentMethod.PAYSTACK,
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
    when(templateEngine.process(anyString(), any(IContext.class)))
        .thenReturn("<html><body>PAYMENT RECEIPT</body></html>");

    byte[] pdfBytes = receiptPdfService.generateReceiptPdf(RECEIPT_ID);

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0);
    assertEquals("%PDF", new String(pdfBytes, 0, 4));

    verify(receiptRepository).findById(RECEIPT_ID);
    verify(paymentRepository).findById(PAYMENT_ID);
    verify(templateEngine).process(anyString(), any(IContext.class));
  }

  @Test
  void testGenerateReceiptPdf_ContainsExpectedContent() throws IOException {
    // BASELINE TEST: This establishes the behavioral contract BEFORE the rewrite
    // It should PASS against the current iText-only implementation
    when(receiptRepository.findById(RECEIPT_ID)).thenReturn(Optional.of(receipt));
    when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
    
    // Mock the template to return HTML with expected content
    String htmlTemplate =
        "<!DOCTYPE html><html><head><title>Payment Receipt</title></head><body>"
            + "<div class=\"header\">"
            + "<div class=\"title\">PAYMENT RECEIPT</div>"
            + "<div class=\"subtitle\">SmartLedger Finance System</div>"
            + "</div>"
            + "<div class=\"section-title\">Receipt Information</div>"
            + "<table><tr><td class=\"label\">Receipt Number:</td><td>"
            + RECEIPT_NUMBER
            + "</td></tr></table>"
            + "<div class=\"section-title\">Payment Information</div>"
            + "<table><tr><td class=\"label\">Payment Number:</td><td>"
            + PAYMENT_NUMBER
            + "</td></tr>"
            + "<tr><td class=\"label\">Amount:</td><td>USD 100.00</td></tr>"
            + "<tr><td class=\"label\">Payment Method:</td><td>PAYSTACK</td></tr>"
            + "<tr><td class=\"label\">Payer Name:</td><td>John Doe</td></tr>"
            + "</table>"
            + "</body></html>";
    
    when(templateEngine.process(anyString(), any(IContext.class))).thenReturn(htmlTemplate);

    byte[] pdfBytes = receiptPdfService.generateReceiptPdf(RECEIPT_ID);

    // Extract text from PDF to verify content
    String pdfText = extractTextFromPdf(pdfBytes);

    // Assert key content is present
    assertTrue(pdfText.contains(RECEIPT_NUMBER), "PDF should contain receipt number");
    assertTrue(pdfText.contains(PAYMENT_NUMBER), "PDF should contain payment number");
    assertTrue(pdfText.contains("100.00"), "PDF should contain amount");
    assertTrue(pdfText.contains("USD"), "PDF should contain currency");
    assertTrue(pdfText.contains("John Doe"), "PDF should contain payer name");
    assertTrue(pdfText.contains("PAYSTACK"), "PDF should contain payment method");
    assertTrue(pdfText.contains("PAYMENT RECEIPT"), "PDF should contain title");
    assertTrue(pdfText.contains("SmartLedger Finance System"), "PDF should contain subtitle");
  }

  private String extractTextFromPdf(byte[] pdfBytes) throws IOException {
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
        PdfReader pdfReader = new PdfReader(inputStream);
        PdfDocument pdfDocument = new PdfDocument(pdfReader)) {
      return PdfTextExtractor.getTextFromPage(pdfDocument.getFirstPage());
    }
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
    verifyNoInteractions(templateEngine);
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
    verifyNoInteractions(templateEngine);
  }

  @Test
  void testGenerateReceiptPdf_WithNullPayerFields() {
    receipt.setPayerEmail(null);
    receipt.setPayerPhone(null);

    when(receiptRepository.findById(RECEIPT_ID)).thenReturn(Optional.of(receipt));
    when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
    when(templateEngine.process(anyString(), any(IContext.class)))
        .thenReturn("<html><body>PAYMENT RECEIPT</body></html>");

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
    when(templateEngine.process(anyString(), any(IContext.class)))
        .thenReturn("<html><body>PAYMENT RECEIPT</body></html>");

    byte[] pdfBytes = receiptPdfService.generateReceiptPdf(RECEIPT_ID);

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0);
  }
}
