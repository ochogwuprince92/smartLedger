package com.finance.smartLedger.shared.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

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
}
