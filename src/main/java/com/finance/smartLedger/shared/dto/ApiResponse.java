package com.finance.smartLedger.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

  private boolean success;
  private String message;
  private T data;
  private List<ApiError> errors;
  private LocalDateTime timestamp;
  private String path;

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, null, data, null, LocalDateTime.now(), null);
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(true, message, data, null, LocalDateTime.now(), null);
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(false, message, null, null, LocalDateTime.now(), null);
  }

  public static <T> ApiResponse<T> error(String message, List<ApiError> errors) {
    return new ApiResponse<>(false, message, null, errors, LocalDateTime.now(), null);
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ApiError {
    private String field;
    private String message;
    private String code;
  }
}
