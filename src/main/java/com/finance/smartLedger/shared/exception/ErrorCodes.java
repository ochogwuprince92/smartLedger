package com.finance.smartLedger.shared.exception;

public enum ErrorCodes {

  // General errors (1000-1999)
  INTERNAL_SERVER_ERROR("ERR-1000", "Internal server error"),
  VALIDATION_ERROR("ERR-1001", "Validation error"),
  NOT_FOUND("ERR-1002", "Resource not found"),
  BAD_REQUEST("ERR-1003", "Bad request"),
  UNAUTHORIZED("ERR-1004", "Unauthorized"),
  FORBIDDEN("ERR-1005", "Forbidden"),
  CONFLICT("ERR-1006", "Resource conflict"),

  // Payment errors (2000-2999)
  PAYMENT_PROCESSING_FAILED("ERR-2000", "Payment processing failed"),
  PAYMENT_GATEWAY_ERROR("ERR-2001", "Payment gateway error"),
  INVALID_PAYMENT_AMOUNT("ERR-2002", "Invalid payment amount"),
  PAYMENT_EXPIRED("ERR-2003", "Payment expired"),
  DUPLICATE_PAYMENT("ERR-2004", "Duplicate payment"),
  PAYMENT_NOT_FOUND("ERR-2005", "Payment not found"),
  PAYMENT_EXCEEDS_INVOICE_BALANCE("ERR-2006", "Payment amount exceeds invoice outstanding balance"),

  // Ledger errors (3000-3999)
  ACCOUNT_NOT_FOUND("ERR-3000", "Account not found"),
  INVALID_ACCOUNT_TYPE("ERR-3001", "Invalid account type"),
  ACCOUNT_BALANCE_INSUFFICIENT("ERR-3002", "Insufficient account balance"),
  ACCOUNT_ALREADY_EXISTS("ERR-3003", "Account already exists"),
  INVALID_ACCOUNT_HIERARCHY("ERR-3004", "Invalid account hierarchy"),

  // Journal errors (4000-4999)
  JOURNAL_ENTRY_NOT_FOUND("ERR-4000", "Journal entry not found"),
  JOURNAL_ENTRY_ALREADY_POSTED("ERR-4001", "Journal entry already posted"),
  INVALID_JOURNAL_ENTRY("ERR-4002", "Invalid journal entry"),
  DEBIT_CREDIT_MISMATCH("ERR-4003", "Debit and credit amounts do not match"),
  JOURNAL_ENTRY_LOCKED("ERR-4004", "Journal entry is locked"),

  // Reconciliation errors (5000-5999)
  RECONCILIATION_FAILED("ERR-5000", "Reconciliation failed"),
  RECONCILIATION_NOT_FOUND("ERR-5001", "Reconciliation not found"),
  VARIANCE_DETECTED("ERR-5002", "Variance detected during reconciliation"),
  RECONCILIATION_ALREADY_COMPLETED("ERR-5003", "Reconciliation already completed"),

  // Reporting errors (6000-6999)
  REPORT_GENERATION_FAILED("ERR-6000", "Report generation failed"),
  INVALID_REPORT_PARAMETERS("ERR-6001", "Invalid report parameters"),
  REPORT_NOT_FOUND("ERR-6002", "Report not found"),

  // Notification errors (7000-7999)
  NOTIFICATION_SEND_FAILED("ERR-7000", "Failed to send notification"),
  INVALID_NOTIFICATION_TYPE("ERR-7001", "Invalid notification type"),
  NOTIFICATION_RETRY_EXHAUSTED("ERR-7002", "Notification retry exhausted"),

  // AI errors (8000-8999)
  AI_SERVICE_UNAVAILABLE("ERR-8000", "AI service unavailable"),
  AI_INSIGHT_GENERATION_FAILED("ERR-8001", "AI insight generation failed"),
  INVALID_AI_PARAMETERS("ERR-8002", "Invalid AI parameters");

  private final String code;
  private final String message;

  ErrorCodes(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
