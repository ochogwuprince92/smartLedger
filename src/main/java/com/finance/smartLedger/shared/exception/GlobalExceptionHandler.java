package com.finance.smartLedger.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ProblemDetail handleBusinessException(BusinessException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
    problemDetail.setTitle(ex.getErrorCodes().getMessage());
    problemDetail.setType(
        java.net.URI.create("https://api.smartledger.com/errors/" + ex.getErrorCodes().getCode()));
    return problemDetail;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGenericException(Exception ex) {
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
