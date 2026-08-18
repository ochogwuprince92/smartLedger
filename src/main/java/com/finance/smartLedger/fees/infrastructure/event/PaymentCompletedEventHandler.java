package com.finance.smartLedger.fees.infrastructure.event;

import com.finance.smartLedger.fees.application.FeeInvoiceService;
import com.finance.smartLedger.fees.domain.FeePayment;
import com.finance.smartLedger.fees.domain.FeeType;
import com.finance.smartLedger.fees.infrastructure.persistence.FeePaymentRepository;
import com.finance.smartLedger.payment.domain.PaymentCompleted;
import com.finance.smartLedger.shared.valueobject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCompletedEventHandler {

  private final FeeInvoiceService feeInvoiceService;
  private final FeePaymentRepository feePaymentRepository;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePaymentCompleted(PaymentCompleted event) {
    try {
      // Skip if no invoiceId (e.g., donations)
      if (event.getInvoiceId() == null) {
        log.info("Skipping PaymentCompleted event with no invoiceId: paymentId={}", event.getPaymentId());
        return;
      }

      // Idempotency guard: check if FeePayment already exists for this paymentId
      if (feePaymentRepository.findBySourcePaymentId(event.getPaymentId()).isPresent()) {
        log.info("FeePayment already exists for paymentId={}, skipping", event.getPaymentId());
        return;
      }

      log.info(
          "Handling PaymentCompleted event: paymentId={}, invoiceId={}, amount={}",
          event.getPaymentId(),
          event.getInvoiceId(),
          event.getAmount());

      // Convert BigDecimal + currencyCode to Money at the boundary
      Money amount = Money.of(event.getAmount(), event.getCurrencyCode());

      // Create FeePayment against the invoice
      // Note: We need to determine feeType. For now, use a default or derive from context
      // This is a simplification - in production, feeType should be determined from invoice context
      FeePayment feePayment =
          feeInvoiceService.recordPayment(
              event.getInvoiceId(),
              FeeType.TUITION_FEE, // Default fee type - may need adjustment based on invoice context
              amount,
              event.getPaymentMethod(),
              event.getPaymentId().toString(), // Use paymentId as reference
              event.getPaymentId(), // Pass sourcePaymentId for idempotency
              "system");

      // Complete the FeePayment with receipt number
      String receiptNumber = "RCP-" + event.getPaymentId().toString().substring(0, 8).toUpperCase();
      feeInvoiceService.completePayment(feePayment.getId(), receiptNumber, "system");

      log.info(
          "FeePayment created and completed: paymentId={}, feePaymentId={}, receiptNumber={}",
          event.getPaymentId(),
          feePayment.getId(),
          receiptNumber);

    } catch (Exception e) {
      log.error("Failed to handle PaymentCompleted event: paymentId={}", event.getPaymentId(), e);
      // Don't throw - payment completion should not fail due to fee payment issues
    }
  }
}
