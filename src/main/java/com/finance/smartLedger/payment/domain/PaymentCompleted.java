package com.finance.smartLedger.payment.domain;

import com.finance.smartLedger.shared.domain.DomainEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentCompleted extends DomainEvent {

  private UUID paymentId;
  private UUID invoiceId;
  private BigDecimal amount;
  private String currencyCode;
  private String paymentMethod;
  private LocalDateTime paymentDate;
  private String payerName;

  public PaymentCompleted(
      UUID paymentId,
      UUID invoiceId,
      BigDecimal amount,
      String currencyCode,
      String paymentMethod,
      LocalDateTime paymentDate,
      String payerName) {
    super("PaymentCompleted");
    this.paymentId = paymentId;
    this.invoiceId = invoiceId;
    this.amount = amount;
    this.currencyCode = currencyCode;
    this.paymentMethod = paymentMethod;
    this.paymentDate = paymentDate;
    this.payerName = payerName;
  }
}
