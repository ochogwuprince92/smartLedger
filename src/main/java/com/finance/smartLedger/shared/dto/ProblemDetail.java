package com.finance.smartLedger.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDetail {

  private String type;
  private String title;
  private int status;
  private String detail;
  private String instance;
  private LocalDateTime timestamp;
  private String code;
  private List<FieldError> errors;
  private Map<String, Object> metadata;

  public static ProblemDetail of(int status, String title, String detail) {
    return new ProblemDetail(
        null, title, status, detail, null, LocalDateTime.now(), null, null, null);
  }

  public static ProblemDetail of(int status, String title, String detail, String code) {
    return new ProblemDetail(
        null, title, status, detail, null, LocalDateTime.now(), code, null, null);
  }

  public static ProblemDetail of(
      int status,
      String title,
      String detail,
      String code,
      String instance,
      LocalDateTime timestamp) {
    return new ProblemDetail(null, title, status, detail, instance, timestamp, code, null, null);
  }

  public ProblemDetail withInstance(String instance) {
    this.instance = instance;
    return this;
  }

  public ProblemDetail withType(String type) {
    this.type = type;
    return this;
  }

  public ProblemDetail withErrors(List<FieldError> errors) {
    this.errors = errors;
    return this;
  }

  public ProblemDetail withMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
    return this;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FieldError {
    private String field;
    private String message;
    private String rejectedValue;
    private String code;
  }
}
