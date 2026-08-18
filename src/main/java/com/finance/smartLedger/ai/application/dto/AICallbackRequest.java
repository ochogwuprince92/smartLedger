package com.finance.smartLedger.ai.application.dto;

import com.finance.smartLedger.ai.domain.RiskLevel;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AICallbackRequest {

  private String requestId;
  private UUID reconciliationId;
  private RiskLevel riskLevel;
  private String summary;
  private String rootCause;
  private List<String> recommendations;
  private String signature;
}
