package com.finance.smartLedger.receipt.application;

import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.infrastructure.persistence.PaymentRepository;
import com.finance.smartLedger.receipt.domain.Receipt;
import com.finance.smartLedger.receipt.infrastructure.persistence.ReceiptRepository;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptPdfService {

  private final ReceiptRepository receiptRepository;
  private final PaymentRepository paymentRepository;
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Transactional(readOnly = true)
  public byte[] generateReceiptPdf(UUID receiptId) {
    Receipt receipt =
        receiptRepository
            .findById(receiptId)
            .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));

    Payment payment =
        paymentRepository
            .findById(receipt.getPaymentId())
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      PdfWriter writer = new PdfWriter(outputStream);
      PdfDocument pdfDocument = new PdfDocument(writer);
      Document document = new Document(pdfDocument);

      PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
      PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

      document.setFont(font);

      addHeader(document, boldFont);
      addReceiptDetails(document, receipt, boldFont);
      addPaymentDetails(document, payment, boldFont);
      addFooter(document, font);

      document.close();

      byte[] pdfBytes = outputStream.toByteArray();
      log.info("Generated PDF for receipt: {}", receipt.getReceiptNumber());
      return pdfBytes;

    } catch (IOException e) {
      log.error("Error generating PDF for receipt: {}", receipt.getReceiptNumber(), e);
      throw new RuntimeException("Failed to generate PDF receipt", e);
    }
  }

  private void addHeader(Document document, PdfFont boldFont) {
    Paragraph title =
        new Paragraph("PAYMENT RECEIPT")
            .setFont(boldFont)
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20);
    document.add(title);

    Paragraph subtitle =
        new Paragraph("SmartLedger Finance System")
            .setFont(boldFont)
            .setFontSize(14)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(30);
    document.add(subtitle);
  }

  private void addReceiptDetails(Document document, Receipt receipt, PdfFont boldFont) {
    Paragraph receiptTitle =
        new Paragraph("Receipt Information")
            .setFont(boldFont)
            .setFontSize(12)
            .setBold()
            .setMarginTop(10)
            .setMarginBottom(5);
    document.add(receiptTitle);

    Table receiptTable =
        new Table(UnitValue.createPercentArray(new float[] {1, 2})).useAllAvailableWidth();

    addTableRow(receiptTable, "Receipt Number:", receipt.getReceiptNumber(), boldFont);
    addTableRow(
        receiptTable, "Receipt Date:", receipt.getReceiptDate().format(DATE_FORMATTER), boldFont);
    addTableRow(receiptTable, "Status:", receipt.getStatus().name(), boldFont);

    document.add(receiptTable);
  }

  private void addPaymentDetails(Document document, Payment payment, PdfFont boldFont) {
    Paragraph paymentTitle =
        new Paragraph("Payment Information")
            .setFont(boldFont)
            .setFontSize(12)
            .setBold()
            .setMarginTop(20)
            .setMarginBottom(5);
    document.add(paymentTitle);

    Table paymentTable =
        new Table(UnitValue.createPercentArray(new float[] {1, 2})).useAllAvailableWidth();

    addTableRow(paymentTable, "Payment Number:", payment.getPaymentNumber(), boldFont);
    addTableRow(
        paymentTable,
        "Amount:",
        formatAmount(payment.getAmount(), payment.getCurrencyCode()),
        boldFont);
    addTableRow(paymentTable, "Payment Method:", payment.getPaymentMethod().name(), boldFont);
    addTableRow(
        paymentTable, "Payment Date:", payment.getPaymentDate().format(DATE_FORMATTER), boldFont);

    if (payment.getPayerName() != null) {
      addTableRow(paymentTable, "Payer Name:", payment.getPayerName(), boldFont);
    }
    if (payment.getPayerEmail() != null) {
      addTableRow(paymentTable, "Payer Email:", payment.getPayerEmail(), boldFont);
    }
    if (payment.getPayerPhone() != null) {
      addTableRow(paymentTable, "Payer Phone:", payment.getPayerPhone(), boldFont);
    }
    if (payment.getDescription() != null) {
      addTableRow(paymentTable, "Description:", payment.getDescription(), boldFont);
    }

    document.add(paymentTable);
  }

  private void addFooter(Document document, PdfFont font) {
    document.add(new Paragraph("\n"));

    Paragraph note =
        new Paragraph(
                "This is an official receipt generated by SmartLedger Finance System. "
                    + "Please keep this document for your records.")
            .setFont(font)
            .setFontSize(8)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.GRAY);
    document.add(note);
  }

  private void addTableRow(Table table, String label, String value, PdfFont boldFont) {
    Cell labelCell = new Cell().add(new Paragraph(label).setFont(boldFont)).setBorder(null);
    Cell valueCell = new Cell().add(new Paragraph(value)).setBorder(null);
    table.addCell(labelCell);
    table.addCell(valueCell);
  }

  private String formatAmount(BigDecimal amount, String currencyCode) {
    return String.format("%s %s", currencyCode, amount.toPlainString());
  }
}
