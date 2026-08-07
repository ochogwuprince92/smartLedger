package com.finance.smartLedger.notification.domain;

public enum NotificationType {
  PAYMENT_COMPLETED,
  PAYMENT_FAILED,
  PAYMENT_REFUNDED,
  PAYMENT_CANCELLED,
  RECEIPT_GENERATED,
  RECEIPT_SENT,
  RECEIPT_DELIVERED,
  RECEIPT_FAILED,
  SYSTEM_ALERT
}
