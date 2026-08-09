package com.finance.smartLedger.ai.domain;

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
public class AIInsightRequested extends DomainEvent {

  private String requestId;
  private UUID reconciliationId;
  private String reconciliationNumber;
  private String sourceSystem;
  private BigDecimal totalVariance;
  private Integer duplicatePayments;
  private Integer missingSettlements;
  private Integer amountMismatches;
  private Integer negativeBalances;
  private Integer transactionCount;
  private String reconciliationStatus;

  public AIInsightRequested(
      String requestId,
      UUID reconciliationId,
      String reconciliationNumber,
      String sourceSystem,
      BigDecimal totalVariance,
      Integer duplicatePayments,
      Integer missingSettlements,
      Integer amountMismatches,
      Integer negativeBalances,
      Integer transactionCount,
      String reconciliationStatus) {
    super("AIInsightRequested");
    this.requestId = requestId;
    this.reconciliationId = reconciliationId;
    this.reconciliationNumber = reconciliationNumber;
    this.sourceSystem = sourceSystem;
    this.totalVariance = totalVariance;
    this.duplicatePayments = duplicatePayments;
    this.missingSettlements = missingSettlements;
    this.amountMismatches = amountMismatches;
    this.negativeBalances = negativeBalances;
    this.transactionCount = transactionCount;
    this.reconciliationStatus = reconciliationStatus;
  }
}
