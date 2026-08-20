package com.finance.smartLedger.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ProblemDetail handleBusinessException(BusinessException ex) {
    log.error("Business exception: {}", ex.getMessage(), ex);
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
    problemDetail.setTitle(ex.getErrorCodes().getMessage());
    problemDetail.setType(
        java.net.URI.create("https://api.smartledger.com/errors/" + ex.getErrorCodes().getCode()));
    return problemDetail;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Illegal argument: {}", ex.getMessage());
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Bad Request");
    problemDetail.setType(
        java.net.URI.create("https://api.smartledger.com/errors/ERR-1003"));
    return problemDetail;
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, org.springframework.http.HttpHeaders headers,
      HttpStatusCode status, WebRequest request) {
    String errorMessage = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .reduce((a, b) -> a + ", " + b)
        .orElse("Validation failed");
    log.warn("Validation error: {}", errorMessage);
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
    problemDetail.setTitle("Validation Failed");
    problemDetail.setType(java.net.URI.create("https://api.smartledger.com/errors/ERR-1001"));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGenericException(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred: " + ex.getMessage());
    problemDetail.setTitle("Internal Server Error");
    problemDetail.setType(
        java.net.URI.create("https://api.smartledger.com/errors/ERR-1000"));
    return problemDetail;
  }
}
