package com.finance.smartLedger.notification.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Notification status")
public enum NotificationStatusDto {
  PENDING,
  SENT,
  DELIVERED,
  FAILED,
  RETRYING;

  @JsonCreator
  public static NotificationStatusDto fromString(String value) {
    return NotificationStatusDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.notification.domain.NotificationStatus toDomain() {
    return com.finance.smartLedger.notification.domain.NotificationStatus.valueOf(name());
  }
}
