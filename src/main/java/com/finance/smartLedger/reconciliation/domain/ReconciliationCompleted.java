package com.finance.smartLedger.reconciliation.domain;

import com.finance.smartLedger.shared.domain.DomainEvent;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReconciliationCompleted extends DomainEvent {

  private UUID reconciliationId;
  private String reconciliationNumber;
  private String sourceSystem;
  private BigDecimal totalExpectedAmount;
  private BigDecimal totalActualAmount;
  private BigDecimal varianceAmount;
  private String reconciliationStatus;
  private Integer duplicatePayments;
  private Integer missingSettlements;
  private Integer amountMismatches;
  private Integer negativeBalances;
  private Integer transactionCount;

  public ReconciliationCompleted(
      UUID reconciliationId,
      String reconciliationNumber,
      String sourceSystem,
      BigDecimal totalExpectedAmount,
      BigDecimal totalActualAmount,
      BigDecimal varianceAmount,
      String reconciliationStatus,
      Integer duplicatePayments,
      Integer missingSettlements,
      Integer amountMismatches,
      Integer negativeBalances,
      Integer transactionCount) {
    super("ReconciliationCompleted");
    this.reconciliationId = reconciliationId;
    this.reconciliationNumber = reconciliationNumber;
    this.sourceSystem = sourceSystem;
    this.totalExpectedAmount = totalExpectedAmount;
    this.totalActualAmount = totalActualAmount;
    this.varianceAmount = varianceAmount;
    this.reconciliationStatus = reconciliationStatus;
    this.duplicatePayments = duplicatePayments;
    this.missingSettlements = missingSettlements;
    this.amountMismatches = amountMismatches;
    this.negativeBalances = negativeBalances;
    this.transactionCount = transactionCount;
  }
}
