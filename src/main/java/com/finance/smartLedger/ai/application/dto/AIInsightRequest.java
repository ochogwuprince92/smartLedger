package com.finance.smartLedger.ai.application.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInsightRequest {

  private String requestId;
  private UUID reconciliationId;
  private String reconciliationNumber;
  private String sourceSystem;
  private Integer duplicatePayments;
  private Integer missingSettlements;
  private Integer amountMismatches;
  private Integer negativeBalances;
  private BigDecimal totalVariance;
  private Integer transactionCount;
  private String reconciliationStatus;
  private String callbackUrl;
}
