package com.finance.smartLedger.shared.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ApiResponse Tests")
class ApiResponseTest {

  @Test
  @DisplayName("Should create success response with data only")
  void shouldCreateSuccessResponseWithDataOnly() {
    String data = "test data";
    ApiResponse<String> response = ApiResponse.success(null, data);

    assertTrue(response.isSuccess());
    assertEquals(data, response.getData());
    assertNull(response.getMessage());
    assertNull(response.getErrors());
    assertNotNull(response.getTimestamp());
    assertNull(response.getPath());
  }

  @Test
  @DisplayName("Should create success response with message and data")
  void shouldCreateSuccessResponseWithMessageAndData() {
    String message = "Operation successful";
    String data = "test data";
    ApiResponse<String> response = ApiResponse.success(message, data);

    assertTrue(response.isSuccess());
    assertEquals(message, response.getMessage());
    assertEquals(data, response.getData());
    assertNull(response.getErrors());
    assertNotNull(response.getTimestamp());
    assertNull(response.getPath());
  }

  @Test
  @DisplayName("Should create error response with message only")
  void shouldCreateErrorResponseWithMessageOnly() {
    String message = "Operation failed";
    ApiResponse<String> response = ApiResponse.error(message);

    assertFalse(response.isSuccess());
    assertEquals(message, response.getMessage());
    assertNull(response.getData());
    assertNull(response.getErrors());
    assertNotNull(response.getTimestamp());
    assertNull(response.getPath());
  }

  @Test
  @DisplayName("Should create error response with message and errors")
  void shouldCreateErrorResponseWithMessageAndErrors() {
    String message = "Validation failed";
    List<ApiResponse.ApiError> errors =
        List.of(new ApiResponse.ApiError("field1", "error message", "CODE1"));
    ApiResponse<String> response = ApiResponse.error(message, errors);

    assertFalse(response.isSuccess());
    assertEquals(message, response.getMessage());
    assertNull(response.getData());
    assertEquals(errors, response.getErrors());
    assertNotNull(response.getTimestamp());
    assertNull(response.getPath());
  }

  @Test
  @DisplayName("Should create response with all fields using constructor")
  void shouldCreateResponseWithAllFieldsUsingConstructor() {
    boolean success = true;
    String message = "Test message";
    String data = "test data";
    List<ApiResponse.ApiError> errors = List.of(new ApiResponse.ApiError("field", "error", "CODE"));
    LocalDateTime timestamp = LocalDateTime.now();
    String path = "/api/test";

    ApiResponse<String> response =
        new ApiResponse<>(success, message, data, errors, timestamp, path);

    assertEquals(success, response.isSuccess());
    assertEquals(message, response.getMessage());
    assertEquals(data, response.getData());
    assertEquals(errors, response.getErrors());
    assertEquals(timestamp, response.getTimestamp());
    assertEquals(path, response.getPath());
  }

  @Test
  @DisplayName("Should set and get success flag")
  void shouldSetAndGetSuccessFlag() {
    ApiResponse<String> response = new ApiResponse<>();

    response.setSuccess(true);
    assertTrue(response.isSuccess());

    response.setSuccess(false);
    assertFalse(response.isSuccess());
  }

  @Test
  @DisplayName("Should set and get message")
  void shouldSetAndGetMessage() {
    ApiResponse<String> response = new ApiResponse<>();
    String message = "Test message";

    response.setMessage(message);
    assertEquals(message, response.getMessage());
  }

  @Test
  @DisplayName("Should set and get data")
  void shouldSetAndGetData() {
    ApiResponse<String> response = new ApiResponse<>();
    String data = "test data";

    response.setData(data);
    assertEquals(data, response.getData());
  }

  @Test
  @DisplayName("Should set and get errors")
  void shouldSetAndGetErrors() {
    ApiResponse<String> response = new ApiResponse<>();
    List<ApiResponse.ApiError> errors = List.of(new ApiResponse.ApiError("field", "error", "CODE"));

    response.setErrors(errors);
    assertEquals(errors, response.getErrors());
  }

  @Test
  @DisplayName("Should set and get timestamp")
  void shouldSetAndGetTimestamp() {
    ApiResponse<String> response = new ApiResponse<>();
    LocalDateTime timestamp = LocalDateTime.now();

    response.setTimestamp(timestamp);
    assertEquals(timestamp, response.getTimestamp());
  }

  @Test
  @DisplayName("Should set and get path")
  void shouldSetAndGetPath() {
    ApiResponse<String> response = new ApiResponse<>();
    String path = "/api/test";

    response.setPath(path);
    assertEquals(path, response.getPath());
  }

  @Test
  @DisplayName("Should handle null data in success response")
  void shouldHandleNullDataInSuccessResponse() {
    ApiResponse<String> response = ApiResponse.success(null);

    assertTrue(response.isSuccess());
    assertNull(response.getData());
  }

  @Test
  @DisplayName("Should handle empty errors list in error response")
  void shouldHandleEmptyErrorsListInErrorResponse() {
    ApiResponse<String> response = ApiResponse.error("Error", List.of());

    assertFalse(response.isSuccess());
    assertEquals(List.of(), response.getErrors());
  }

  @Test
  @DisplayName("Should create ApiError with all fields")
  void shouldCreateApiErrorWithAllFields() {
    String field = "username";
    String message = "Username is required";
    String code = "REQUIRED";

    ApiResponse.ApiError error = new ApiResponse.ApiError(field, message, code);

    assertEquals(field, error.getField());
    assertEquals(message, error.getMessage());
    assertEquals(code, error.getCode());
  }

  @Test
  @DisplayName("Should set ApiError fields")
  void shouldSetApiErrorFields() {
    ApiResponse.ApiError error = new ApiResponse.ApiError();

    error.setField("email");
    error.setMessage("Invalid email");
    error.setCode("INVALID");

    assertEquals("email", error.getField());
    assertEquals("Invalid email", error.getMessage());
    assertEquals("INVALID", error.getCode());
  }

  @Test
  @DisplayName("Should handle generic type with complex object")
  void shouldHandleGenericTypeWithComplexObject() {
    class TestData {
      String name;
      int value;

      TestData(String name, int value) {
        this.name = name;
        this.value = value;
      }
    }

    TestData data = new TestData("test", 123);
    ApiResponse<TestData> response = ApiResponse.success(data);

    assertTrue(response.isSuccess());
    assertEquals(data, response.getData());
    assertEquals("test", response.getData().name);
    assertEquals(123, response.getData().value);
  }
}
