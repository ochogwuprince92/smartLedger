package com.finance.smartLedger.notification.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Notification type")
public enum NotificationTypeDto {
  PAYMENT_COMPLETED,
  PAYMENT_FAILED,
  PAYMENT_REFUNDED,
  PAYMENT_CANCELLED,
  RECEIPT_GENERATED,
  RECEIPT_SENT,
  RECEIPT_DELIVERED,
  RECEIPT_FAILED,
  SYSTEM_ALERT;

  @JsonCreator
  public static NotificationTypeDto fromString(String value) {
    return NotificationTypeDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.notification.domain.NotificationType toDomain() {
    return com.finance.smartLedger.notification.domain.NotificationType.valueOf(name());
  }
}
