package com.finance.smartLedger.ai.infrastructure.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInsightResponse {

  @JsonProperty("insight_type")
  private String insightType;

  @JsonProperty("title")
  private String title;

  @JsonProperty("description")
  private String description;

  @JsonProperty("severity")
  private String severity;

  @JsonProperty("recommendation")
  private String recommendation;

  @JsonProperty("confidence_score")
  private Double confidenceScore;

  @JsonProperty("root_cause")
  private String rootCause;

  @JsonProperty("affected_entities")
  private Map<String, Object> affectedEntities;

  @JsonProperty("suggested_actions")
  private java.util.List<String> suggestedActions;

  @JsonProperty("metadata")
  private Map<String, Object> metadata;

  @JsonProperty("error")
  private String error;

  @JsonProperty("success")
  private boolean success;
}
