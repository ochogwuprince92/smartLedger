package com.finance.smartLedger.shared.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class GlobalExceptionHandlerTest {

  @Test
  void businessException_hasCorrectHttpStatus() {
    // RED: This test verifies BusinessException has the correct HttpStatus
    // but doesn't test the @RestControllerAdvice yet
    BusinessException exception = new BusinessException(ErrorCodes.ACCOUNT_NOT_FOUND, "Account not found");
    
    assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    assertEquals("ERR-3000", exception.getErrorCodes().getCode());
    assertEquals("Account not found", exception.getMessage());
  }

  @Test
  void businessException_withCustomMessage() {
    BusinessException exception = new BusinessException(ErrorCodes.CONFLICT, "Duplicate resource");
    
    assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
    assertEquals("ERR-1006", exception.getErrorCodes().getCode());
    assertEquals("Duplicate resource", exception.getMessage());
  }

  @Test
  void illegalArgument_returnsBadRequest() {
    // RED: This test should fail before the fix is implemented
    // IllegalArgumentException currently falls through to generic Exception handler (500)
    // After fix, it should return 400 Bad Request
    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    IllegalArgumentException ex = new IllegalArgumentException("Account number must be 8-20 digits");
    
    ProblemDetail result = handler.handleIllegalArgument(ex);
    
    assertEquals(400, result.getStatus());
    assertEquals("Bad Request", result.getTitle());
    assertEquals("Account number must be 8-20 digits", result.getDetail());
    assertEquals("https://api.smartledger.com/errors/ERR-1003", result.getType().toString());
  }

  @Test
  void illegalArgument_moneyValidation_returnsBadRequest() {
    // Test Money validation (negative amount)
    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    IllegalArgumentException ex = new IllegalArgumentException("Amount cannot be negative");
    
    ProblemDetail result = handler.handleIllegalArgument(ex);
    
    assertEquals(400, result.getStatus());
    assertEquals("Bad Request", result.getTitle());
    assertEquals("Amount cannot be negative", result.getDetail());
    assertEquals("https://api.smartledger.com/errors/ERR-1003", result.getType().toString());
  }

  @Test
  void illegalArgument_paymentService_returnsBadRequest() {
    // Test PaymentService validation (payment not found)
    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    IllegalArgumentException ex = new IllegalArgumentException("Payment not found");
    
    ProblemDetail result = handler.handleIllegalArgument(ex);
    
    assertEquals(400, result.getStatus());
    assertEquals("Bad Request", result.getTitle());
    assertEquals("Payment not found", result.getDetail());
    assertEquals("https://api.smartledger.com/errors/ERR-1003", result.getType().toString());
  }

  @Test
  void illegalArgument_reconciliationService_returnsBadRequest() {
    // Test ReconciliationService validation (reconciliation not found)
    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    IllegalArgumentException ex = new IllegalArgumentException("Reconciliation not found");
    
    ProblemDetail result = handler.handleIllegalArgument(ex);
    
    assertEquals(400, result.getStatus());
    assertEquals("Bad Request", result.getTitle());
    assertEquals("Reconciliation not found", result.getDetail());
    assertEquals("https://api.smartledger.com/errors/ERR-1003", result.getType().toString());
  }
}
