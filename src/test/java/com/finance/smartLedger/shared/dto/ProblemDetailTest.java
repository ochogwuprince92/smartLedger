package com.finance.smartLedger.shared.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProblemDetail Tests")
class ProblemDetailTest {

  @Test
  @DisplayName("Should create ProblemDetail with status, title, and detail")
  void shouldCreateProblemDetailWithStatusTitleAndDetail() {
    int status = 400;
    String title = "Bad Request";
    String detail = "Invalid input";

    ProblemDetail problem = ProblemDetail.of(status, title, detail);

    assertEquals(status, problem.getStatus());
    assertEquals(title, problem.getTitle());
    assertEquals(detail, problem.getDetail());
    assertNotNull(problem.getTimestamp());
    assertNull(problem.getType());
    assertNull(problem.getInstance());
    assertNull(problem.getCode());
    assertNull(problem.getErrors());
    assertNull(problem.getMetadata());
  }

  @Test
  @DisplayName("Should create ProblemDetail with status, title, detail, and code")
  void shouldCreateProblemDetailWithStatusTitleDetailAndCode() {
    int status = 404;
    String title = "Not Found";
    String detail = "Resource not found";
    String code = "NOT_FOUND";

    ProblemDetail problem = ProblemDetail.of(status, title, detail, code);

    assertEquals(status, problem.getStatus());
    assertEquals(title, problem.getTitle());
    assertEquals(detail, problem.getDetail());
    assertEquals(code, problem.getCode());
    assertNotNull(problem.getTimestamp());
    assertNull(problem.getType());
    assertNull(problem.getInstance());
    assertNull(problem.getErrors());
    assertNull(problem.getMetadata());
  }

  @Test
  @DisplayName("Should create ProblemDetail with all parameters")
  void shouldCreateProblemDetailWithAllParameters() {
    int status = 500;
    String title = "Internal Server Error";
    String detail = "Server error occurred";
    String code = "INTERNAL_ERROR";
    String instance = "/api/resource/123";
    LocalDateTime timestamp = LocalDateTime.now();

    ProblemDetail problem = ProblemDetail.of(status, title, detail, code, instance, timestamp);

    assertEquals(status, problem.getStatus());
    assertEquals(title, problem.getTitle());
    assertEquals(detail, problem.getDetail());
    assertEquals(code, problem.getCode());
    assertEquals(instance, problem.getInstance());
    assertEquals(timestamp, problem.getTimestamp());
    assertNull(problem.getType());
    assertNull(problem.getErrors());
    assertNull(problem.getMetadata());
  }

  @Test
  @DisplayName("Should create ProblemDetail using constructor")
  void shouldCreateProblemDetailUsingConstructor() {
    String type = "https://example.com/errors/bad-request";
    String title = "Bad Request";
    int status = 400;
    String detail = "Invalid input";
    String instance = "/api/resource";
    LocalDateTime timestamp = LocalDateTime.now();
    String code = "BAD_REQUEST";
    List<ProblemDetail.FieldError> errors =
        List.of(new ProblemDetail.FieldError("field", "error", "value", "CODE"));
    Map<String, Object> metadata = Map.of("key", "value");

    ProblemDetail problem =
        new ProblemDetail(type, title, status, detail, instance, timestamp, code, errors, metadata);

    assertEquals(type, problem.getType());
    assertEquals(title, problem.getTitle());
    assertEquals(status, problem.getStatus());
    assertEquals(detail, problem.getDetail());
    assertEquals(instance, problem.getInstance());
    assertEquals(timestamp, problem.getTimestamp());
    assertEquals(code, problem.getCode());
    assertEquals(errors, problem.getErrors());
    assertEquals(metadata, problem.getMetadata());
  }

  @Test
  @DisplayName("Should add instance using builder pattern")
  void shouldAddInstanceUsingBuilderPattern() {
    ProblemDetail problem = ProblemDetail.of(400, "Bad Request", "Invalid input");
    String instance = "/api/resource/123";

    ProblemDetail result = problem.withInstance(instance);

    assertEquals(instance, result.getInstance());
    assertSame(problem, result);
  }

  @Test
  @DisplayName("Should add type using builder pattern")
  void shouldAddTypeUsingBuilderPattern() {
    ProblemDetail problem = ProblemDetail.of(400, "Bad Request", "Invalid input");
    String type = "https://example.com/errors/bad-request";

    ProblemDetail result = problem.withType(type);

    assertEquals(type, result.getType());
    assertSame(problem, result);
  }

  @Test
  @DisplayName("Should add errors using builder pattern")
  void shouldAddErrorsUsingBuilderPattern() {
    ProblemDetail problem = ProblemDetail.of(400, "Bad Request", "Invalid input");
    List<ProblemDetail.FieldError> errors =
        List.of(
            new ProblemDetail.FieldError("field1", "error1", "value1", "CODE1"),
            new ProblemDetail.FieldError("field2", "error2", "value2", "CODE2"));

    ProblemDetail result = problem.withErrors(errors);

    assertEquals(errors, result.getErrors());
    assertSame(problem, result);
  }

  @Test
  @DisplayName("Should add metadata using builder pattern")
  void shouldAddMetadataUsingBuilderPattern() {
    ProblemDetail problem = ProblemDetail.of(400, "Bad Request", "Invalid input");
    Map<String, Object> metadata = Map.of("key1", "value1", "key2", "value2");

    ProblemDetail result = problem.withMetadata(metadata);

    assertEquals(metadata, result.getMetadata());
    assertSame(problem, result);
  }

  @Test
  @DisplayName("Should chain builder methods")
  void shouldChainBuilderMethods() {
    ProblemDetail problem =
        ProblemDetail.of(400, "Bad Request", "Invalid input", "BAD_REQUEST")
            .withType("https://example.com/errors/bad-request")
            .withInstance("/api/resource/123")
            .withErrors(List.of(new ProblemDetail.FieldError("field", "error", "value", "CODE")))
            .withMetadata(Map.of("key", "value"));

    assertEquals("https://example.com/errors/bad-request", problem.getType());
    assertEquals("/api/resource/123", problem.getInstance());
    assertEquals("BAD_REQUEST", problem.getCode());
    assertNotNull(problem.getErrors());
    assertNotNull(problem.getMetadata());
  }

  @Test
  @DisplayName("Should set and get type")
  void shouldSetAndGetType() {
    ProblemDetail problem = new ProblemDetail();
    String type = "https://example.com/errors";

    problem.setType(type);
    assertEquals(type, problem.getType());
  }

  @Test
  @DisplayName("Should set and get title")
  void shouldSetAndGetTitle() {
    ProblemDetail problem = new ProblemDetail();
    String title = "Error Title";

    problem.setTitle(title);
    assertEquals(title, problem.getTitle());
  }

  @Test
  @DisplayName("Should set and get status")
  void shouldSetAndGetStatus() {
    ProblemDetail problem = new ProblemDetail();
    int status = 404;

    problem.setStatus(status);
    assertEquals(status, problem.getStatus());
  }

  @Test
  @DisplayName("Should set and get detail")
  void shouldSetAndGetDetail() {
    ProblemDetail problem = new ProblemDetail();
    String detail = "Error detail";

    problem.setDetail(detail);
    assertEquals(detail, problem.getDetail());
  }

  @Test
  @DisplayName("Should set and get instance")
  void shouldSetAndGetInstance() {
    ProblemDetail problem = new ProblemDetail();
    String instance = "/api/resource";

    problem.setInstance(instance);
    assertEquals(instance, problem.getInstance());
  }

  @Test
  @DisplayName("Should set and get timestamp")
  void shouldSetAndGetTimestamp() {
    ProblemDetail problem = new ProblemDetail();
    LocalDateTime timestamp = LocalDateTime.now();

    problem.setTimestamp(timestamp);
    assertEquals(timestamp, problem.getTimestamp());
  }

  @Test
  @DisplayName("Should set and get code")
  void shouldSetAndGetCode() {
    ProblemDetail problem = new ProblemDetail();
    String code = "ERROR_CODE";

    problem.setCode(code);
    assertEquals(code, problem.getCode());
  }

  @Test
  @DisplayName("Should set and get errors")
  void shouldSetAndGetErrors() {
    ProblemDetail problem = new ProblemDetail();
    List<ProblemDetail.FieldError> errors =
        List.of(new ProblemDetail.FieldError("field", "error", "value", "CODE"));

    problem.setErrors(errors);
    assertEquals(errors, problem.getErrors());
  }

  @Test
  @DisplayName("Should set and get metadata")
  void shouldSetAndGetMetadata() {
    ProblemDetail problem = new ProblemDetail();
    Map<String, Object> metadata = Map.of("key", "value");

    problem.setMetadata(metadata);
    assertEquals(metadata, problem.getMetadata());
  }

  @Test
  @DisplayName("Should create FieldError with all fields")
  void shouldCreateFieldErrorWithAllFields() {
    String field = "username";
    String message = "Username is required";
    String rejectedValue = "null";
    String code = "REQUIRED";

    ProblemDetail.FieldError fieldError =
        new ProblemDetail.FieldError(field, message, rejectedValue, code);

    assertEquals(field, fieldError.getField());
    assertEquals(message, fieldError.getMessage());
    assertEquals(rejectedValue, fieldError.getRejectedValue());
    assertEquals(code, fieldError.getCode());
  }

  @Test
  @DisplayName("Should set FieldError fields")
  void shouldSetFieldErrorFields() {
    ProblemDetail.FieldError fieldError = new ProblemDetail.FieldError();

    fieldError.setField("email");
    fieldError.setMessage("Invalid email format");
    fieldError.setRejectedValue("invalid-email");
    fieldError.setCode("INVALID_FORMAT");

    assertEquals("email", fieldError.getField());
    assertEquals("Invalid email format", fieldError.getMessage());
    assertEquals("invalid-email", fieldError.getRejectedValue());
    assertEquals("INVALID_FORMAT", fieldError.getCode());
  }

  @Test
  @DisplayName("Should handle null values in builder methods")
  void shouldHandleNullValuesInBuilderMethods() {
    ProblemDetail problem =
        ProblemDetail.of(400, "Bad Request", "Invalid input")
            .withType(null)
            .withInstance(null)
            .withErrors(null)
            .withMetadata(null);

    assertNull(problem.getType());
    assertNull(problem.getInstance());
    assertNull(problem.getErrors());
    assertNull(problem.getMetadata());
  }
}
