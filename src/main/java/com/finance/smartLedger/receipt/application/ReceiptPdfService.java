package com.finance.smartLedger.receipt.application;

import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.infrastructure.persistence.PaymentRepository;
import com.finance.smartLedger.receipt.domain.Receipt;
import com.finance.smartLedger.receipt.infrastructure.persistence.ReceiptRepository;
import com.itextpdf.html2pdf.HtmlConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptPdfService {

  private final ReceiptRepository receiptRepository;
  private final PaymentRepository paymentRepository;
  private final TemplateEngine templateEngine;

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

    try {
      // Render Thymeleaf template to HTML
      String htmlContent = renderReceiptTemplate(receipt, payment);

      // Convert HTML to PDF using iText html2pdf
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        HtmlConverter.convertToPdf(
            new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8)),
            outputStream);

        byte[] pdfBytes = outputStream.toByteArray();
        log.info("Generated PDF for receipt: {}", receipt.getReceiptNumber());
        return pdfBytes;
      }

    } catch (IOException e) {
      log.error("Error generating PDF for receipt: {}", receipt.getReceiptNumber(), e);
      throw new RuntimeException("Failed to generate PDF receipt", e);
    }
  }

  private String renderReceiptTemplate(Receipt receipt, Payment payment) {
    Context context = new Context();
    context.setVariable("receipt", receipt);
    context.setVariable("payment", payment);

    return templateEngine.process("receipt", context);
  }
}
