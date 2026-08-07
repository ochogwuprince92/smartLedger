package com.finance.smartLedger.shared.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ErrorCodes Enum Tests")
class ErrorCodesTest {

  @Test
  @DisplayName("Should have correct code and message for INTERNAL_SERVER_ERROR")
  void shouldHaveCorrectCodeAndMessageForInternalServerError() {
    ErrorCodes error = ErrorCodes.INTERNAL_SERVER_ERROR;

    assertEquals("ERR-1000", error.getCode());
    assertEquals("Internal server error", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for VALIDATION_ERROR")
  void shouldHaveCorrectCodeAndMessageForValidationError() {
    ErrorCodes error = ErrorCodes.VALIDATION_ERROR;

    assertEquals("ERR-1001", error.getCode());
    assertEquals("Validation error", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for NOT_FOUND")
  void shouldHaveCorrectCodeAndMessageForNotFound() {
    ErrorCodes error = ErrorCodes.NOT_FOUND;

    assertEquals("ERR-1002", error.getCode());
    assertEquals("Resource not found", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for BAD_REQUEST")
  void shouldHaveCorrectCodeAndMessageForBadRequest() {
    ErrorCodes error = ErrorCodes.BAD_REQUEST;

    assertEquals("ERR-1003", error.getCode());
    assertEquals("Bad request", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for UNAUTHORIZED")
  void shouldHaveCorrectCodeAndMessageForUnauthorized() {
    ErrorCodes error = ErrorCodes.UNAUTHORIZED;

    assertEquals("ERR-1004", error.getCode());
    assertEquals("Unauthorized", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for FORBIDDEN")
  void shouldHaveCorrectCodeAndMessageForForbidden() {
    ErrorCodes error = ErrorCodes.FORBIDDEN;

    assertEquals("ERR-1005", error.getCode());
    assertEquals("Forbidden", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for CONFLICT")
  void shouldHaveCorrectCodeAndMessageForConflict() {
    ErrorCodes error = ErrorCodes.CONFLICT;

    assertEquals("ERR-1006", error.getCode());
    assertEquals("Resource conflict", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for PAYMENT_PROCESSING_FAILED")
  void shouldHaveCorrectCodeAndMessageForPaymentProcessingFailed() {
    ErrorCodes error = ErrorCodes.PAYMENT_PROCESSING_FAILED;

    assertEquals("ERR-2000", error.getCode());
    assertEquals("Payment processing failed", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for PAYMENT_GATEWAY_ERROR")
  void shouldHaveCorrectCodeAndMessageForPaymentGatewayError() {
    ErrorCodes error = ErrorCodes.PAYMENT_GATEWAY_ERROR;

    assertEquals("ERR-2001", error.getCode());
    assertEquals("Payment gateway error", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for INVALID_PAYMENT_AMOUNT")
  void shouldHaveCorrectCodeAndMessageForInvalidPaymentAmount() {
    ErrorCodes error = ErrorCodes.INVALID_PAYMENT_AMOUNT;

    assertEquals("ERR-2002", error.getCode());
    assertEquals("Invalid payment amount", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for PAYMENT_EXPIRED")
  void shouldHaveCorrectCodeAndMessageForPaymentExpired() {
    ErrorCodes error = ErrorCodes.PAYMENT_EXPIRED;

    assertEquals("ERR-2003", error.getCode());
    assertEquals("Payment expired", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for DUPLICATE_PAYMENT")
  void shouldHaveCorrectCodeAndMessageForDuplicatePayment() {
    ErrorCodes error = ErrorCodes.DUPLICATE_PAYMENT;

    assertEquals("ERR-2004", error.getCode());
    assertEquals("Duplicate payment", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for PAYMENT_NOT_FOUND")
  void shouldHaveCorrectCodeAndMessageForPaymentNotFound() {
    ErrorCodes error = ErrorCodes.PAYMENT_NOT_FOUND;

    assertEquals("ERR-2005", error.getCode());
    assertEquals("Payment not found", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for ACCOUNT_NOT_FOUND")
  void shouldHaveCorrectCodeAndMessageForAccountNotFound() {
    ErrorCodes error = ErrorCodes.ACCOUNT_NOT_FOUND;

    assertEquals("ERR-3000", error.getCode());
    assertEquals("Account not found", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for INVALID_ACCOUNT_TYPE")
  void shouldHaveCorrectCodeAndMessageForInvalidAccountType() {
    ErrorCodes error = ErrorCodes.INVALID_ACCOUNT_TYPE;

    assertEquals("ERR-3001", error.getCode());
    assertEquals("Invalid account type", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for ACCOUNT_BALANCE_INSUFFICIENT")
  void shouldHaveCorrectCodeAndMessageForAccountBalanceInsufficient() {
    ErrorCodes error = ErrorCodes.ACCOUNT_BALANCE_INSUFFICIENT;

    assertEquals("ERR-3002", error.getCode());
    assertEquals("Insufficient account balance", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for ACCOUNT_ALREADY_EXISTS")
  void shouldHaveCorrectCodeAndMessageForAccountAlreadyExists() {
    ErrorCodes error = ErrorCodes.ACCOUNT_ALREADY_EXISTS;

    assertEquals("ERR-3003", error.getCode());
    assertEquals("Account already exists", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for INVALID_ACCOUNT_HIERARCHY")
  void shouldHaveCorrectCodeAndMessageForInvalidAccountHierarchy() {
    ErrorCodes error = ErrorCodes.INVALID_ACCOUNT_HIERARCHY;

    assertEquals("ERR-3004", error.getCode());
    assertEquals("Invalid account hierarchy", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for JOURNAL_ENTRY_NOT_FOUND")
  void shouldHaveCorrectCodeAndMessageForJournalEntryNotFound() {
    ErrorCodes error = ErrorCodes.JOURNAL_ENTRY_NOT_FOUND;

    assertEquals("ERR-4000", error.getCode());
    assertEquals("Journal entry not found", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for JOURNAL_ENTRY_ALREADY_POSTED")
  void shouldHaveCorrectCodeAndMessageForJournalEntryAlreadyPosted() {
    ErrorCodes error = ErrorCodes.JOURNAL_ENTRY_ALREADY_POSTED;

    assertEquals("ERR-4001", error.getCode());
    assertEquals("Journal entry already posted", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for INVALID_JOURNAL_ENTRY")
  void shouldHaveCorrectCodeAndMessageForInvalidJournalEntry() {
    ErrorCodes error = ErrorCodes.INVALID_JOURNAL_ENTRY;

    assertEquals("ERR-4002", error.getCode());
    assertEquals("Invalid journal entry", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for DEBIT_CREDIT_MISMATCH")
  void shouldHaveCorrectCodeAndMessageForDebitCreditMismatch() {
    ErrorCodes error = ErrorCodes.DEBIT_CREDIT_MISMATCH;

    assertEquals("ERR-4003", error.getCode());
    assertEquals("Debit and credit amounts do not match", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for JOURNAL_ENTRY_LOCKED")
  void shouldHaveCorrectCodeAndMessageForJournalEntryLocked() {
    ErrorCodes error = ErrorCodes.JOURNAL_ENTRY_LOCKED;

    assertEquals("ERR-4004", error.getCode());
    assertEquals("Journal entry is locked", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for RECONCILIATION_FAILED")
  void shouldHaveCorrectCodeAndMessageForReconciliationFailed() {
    ErrorCodes error = ErrorCodes.RECONCILIATION_FAILED;

    assertEquals("ERR-5000", error.getCode());
    assertEquals("Reconciliation failed", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for RECONCILIATION_NOT_FOUND")
  void shouldHaveCorrectCodeAndMessageForReconciliationNotFound() {
    ErrorCodes error = ErrorCodes.RECONCILIATION_NOT_FOUND;

    assertEquals("ERR-5001", error.getCode());
    assertEquals("Reconciliation not found", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for VARIANCE_DETECTED")
  void shouldHaveCorrectCodeAndMessageForVarianceDetected() {
    ErrorCodes error = ErrorCodes.VARIANCE_DETECTED;

    assertEquals("ERR-5002", error.getCode());
    assertEquals("Variance detected during reconciliation", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for RECONCILIATION_ALREADY_COMPLETED")
  void shouldHaveCorrectCodeAndMessageForReconciliationAlreadyCompleted() {
    ErrorCodes error = ErrorCodes.RECONCILIATION_ALREADY_COMPLETED;

    assertEquals("ERR-5003", error.getCode());
    assertEquals("Reconciliation already completed", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for REPORT_GENERATION_FAILED")
  void shouldHaveCorrectCodeAndMessageForReportGenerationFailed() {
    ErrorCodes error = ErrorCodes.REPORT_GENERATION_FAILED;

    assertEquals("ERR-6000", error.getCode());
    assertEquals("Report generation failed", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for INVALID_REPORT_PARAMETERS")
  void shouldHaveCorrectCodeAndMessageForInvalidReportParameters() {
    ErrorCodes error = ErrorCodes.INVALID_REPORT_PARAMETERS;

    assertEquals("ERR-6001", error.getCode());
    assertEquals("Invalid report parameters", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for REPORT_NOT_FOUND")
  void shouldHaveCorrectCodeAndMessageForReportNotFound() {
    ErrorCodes error = ErrorCodes.REPORT_NOT_FOUND;

    assertEquals("ERR-6002", error.getCode());
    assertEquals("Report not found", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for NOTIFICATION_SEND_FAILED")
  void shouldHaveCorrectCodeAndMessageForNotificationSendFailed() {
    ErrorCodes error = ErrorCodes.NOTIFICATION_SEND_FAILED;

    assertEquals("ERR-7000", error.getCode());
    assertEquals("Failed to send notification", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for INVALID_NOTIFICATION_TYPE")
  void shouldHaveCorrectCodeAndMessageForInvalidNotificationType() {
    ErrorCodes error = ErrorCodes.INVALID_NOTIFICATION_TYPE;

    assertEquals("ERR-7001", error.getCode());
    assertEquals("Invalid notification type", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for NOTIFICATION_RETRY_EXHAUSTED")
  void shouldHaveCorrectCodeAndMessageForNotificationRetryExhausted() {
    ErrorCodes error = ErrorCodes.NOTIFICATION_RETRY_EXHAUSTED;

    assertEquals("ERR-7002", error.getCode());
    assertEquals("Notification retry exhausted", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for AI_SERVICE_UNAVAILABLE")
  void shouldHaveCorrectCodeAndMessageForAiServiceUnavailable() {
    ErrorCodes error = ErrorCodes.AI_SERVICE_UNAVAILABLE;

    assertEquals("ERR-8000", error.getCode());
    assertEquals("AI service unavailable", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for AI_INSIGHT_GENERATION_FAILED")
  void shouldHaveCorrectCodeAndMessageForAiInsightGenerationFailed() {
    ErrorCodes error = ErrorCodes.AI_INSIGHT_GENERATION_FAILED;

    assertEquals("ERR-8001", error.getCode());
    assertEquals("AI insight generation failed", error.getMessage());
  }

  @Test
  @DisplayName("Should have correct code and message for INVALID_AI_PARAMETERS")
  void shouldHaveCorrectCodeAndMessageForInvalidAiParameters() {
    ErrorCodes error = ErrorCodes.INVALID_AI_PARAMETERS;

    assertEquals("ERR-8002", error.getCode());
    assertEquals("Invalid AI parameters", error.getMessage());
  }

  @Test
  @DisplayName("Should have all error codes in correct ranges")
  void shouldHaveAllErrorCodesInCorrectRanges() {
    assertEquals("ERR-1000", ErrorCodes.INTERNAL_SERVER_ERROR.getCode());
    assertEquals("ERR-2005", ErrorCodes.PAYMENT_NOT_FOUND.getCode());
    assertEquals("ERR-3004", ErrorCodes.INVALID_ACCOUNT_HIERARCHY.getCode());
    assertEquals("ERR-4004", ErrorCodes.JOURNAL_ENTRY_LOCKED.getCode());
    assertEquals("ERR-5003", ErrorCodes.RECONCILIATION_ALREADY_COMPLETED.getCode());
    assertEquals("ERR-6002", ErrorCodes.REPORT_NOT_FOUND.getCode());
    assertEquals("ERR-7002", ErrorCodes.NOTIFICATION_RETRY_EXHAUSTED.getCode());
    assertEquals("ERR-8002", ErrorCodes.INVALID_AI_PARAMETERS.getCode());
  }

  @Test
  @DisplayName("Should return correct code for all enum values")
  void shouldReturnCorrectCodeForAllEnumValues() {
    for (ErrorCodes error : ErrorCodes.values()) {
      assertNotNull(error.getCode());
      assertFalse(error.getCode().isEmpty());
      assertTrue(error.getCode().startsWith("ERR-"));
    }
  }

  @Test
  @DisplayName("Should return correct message for all enum values")
  void shouldReturnCorrectMessageForAllEnumValues() {
    for (ErrorCodes error : ErrorCodes.values()) {
      assertNotNull(error.getMessage());
      assertFalse(error.getMessage().isEmpty());
    }
  }
}
