package com.finance.smartLedger.notification.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Notification channel")
public enum NotificationChannelDto {
  EMAIL,
  SMS,
  PUSH,
  WEBHOOK;

  @JsonCreator
  public static NotificationChannelDto fromString(String value) {
    return NotificationChannelDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.notification.domain.NotificationChannel toDomain() {
    return com.finance.smartLedger.notification.domain.NotificationChannel.valueOf(name());
  }
}
